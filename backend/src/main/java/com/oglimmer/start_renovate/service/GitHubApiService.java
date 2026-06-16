/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.oglimmer.start_renovate.config.GitHubProperties;
import com.oglimmer.start_renovate.dto.RepoSummary;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Talks to the GitHub REST API on behalf of the authenticated user. */
@Slf4j
@Service
public class GitHubApiService {

  /** Defensive cap on repo-list pagination to bound memory and rate-limit usage. */
  private static final int MAX_REPO_PAGES = 30;

  /** GitHub's maximum page size for list endpoints. */
  private static final int PER_PAGE = 100;

  private final WebClient gitHubWebClient;
  private final GitHubProperties props;
  // Tolerant parser so hand-written .json5 / commented configs still map onto the matrix.
  private final ObjectMapper lenientMapper =
      JsonMapper.builder()
          .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
          .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
          .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
          .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
          .build();

  public GitHubApiService(WebClient gitHubWebClient, GitHubProperties props) {
    this.gitHubWebClient = gitHubWebClient;
    this.props = props;
  }

  /**
   * Result of probing a repo for a Renovate config. {@code config} is null when {@code error} set.
   */
  public record RenovateConfigResult(String path, JsonNode config, String error) {
    static RenovateConfigResult found(String path, JsonNode config) {
      return new RenovateConfigResult(path, config, null);
    }

    static RenovateConfigResult parseError(String path, String error) {
      return new RenovateConfigResult(path, null, error);
    }
  }

  /**
   * Lists every repository the user can access. Uses page-number pagination (more robust than
   * re-feeding GitHub's pre-encoded {@code Link} URLs back through the WebClient, which
   * double-encodes them) and the broad default affiliation — owner + collaborator + organization
   * member — so nothing the user can see is missed. Stops when a page returns fewer than {@link
   * #PER_PAGE} repos.
   */
  public Mono<List<RepoSummary>> listAllRepos(String token) {
    return fetchRepoPage(token, 1)
        .expand(
            page -> page.size() < PER_PAGE ? Mono.empty() : fetchRepoPage(token, page.page() + 1))
        .take(MAX_REPO_PAGES)
        .concatMapIterable(RepoPage::repos)
        .collectList();
  }

  private record RepoPage(List<RepoSummary> repos, int page, int size) {}

  private Mono<RepoPage> fetchRepoPage(String token, int page) {
    // Decode as String and parse with our Jackson 2 mapper: Spring 7's default WebClient codec is
    // Jackson 3 (tools.jackson), which can't construct a Jackson 2 com.fasterxml JsonNode.
    return gitHubWebClient
        .get()
        .uri(
            b ->
                b.path("/user/repos")
                    .queryParam("per_page", PER_PAGE)
                    .queryParam("page", page)
                    .queryParam("affiliation", "owner,collaborator,organization_member")
                    .queryParam("sort", "full_name")
                    .build())
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .toEntity(String.class)
        .map(
            response -> {
              List<RepoSummary> repos = parseRepos(parseJsonOrNull(response.getBody()));
              return new RepoPage(repos, page, repos.size());
            });
  }

  private JsonNode parseJsonOrNull(String body) {
    if (body == null || body.isBlank()) {
      return null;
    }
    try {
      return lenientMapper.readTree(body);
    } catch (Exception ex) {
      log.warn("Failed to parse GitHub /user/repos response: {}", ex.getMessage());
      return null;
    }
  }

  private List<RepoSummary> parseRepos(JsonNode body) {
    List<RepoSummary> repos = new ArrayList<>();
    if (body == null || !body.isArray()) {
      return repos;
    }
    for (JsonNode node : body) {
      String fullName = node.path("full_name").asText(null);
      if (fullName == null) {
        continue;
      }
      repos.add(
          new RepoSummary(
              fullName,
              node.path("name").asText(null),
              node.path("owner").path("login").asText(null),
              node.path("private").asBoolean(false),
              node.path("default_branch").asText(null),
              false));
    }
    return repos;
  }

  /**
   * Probes the repo's known Renovate config paths in order (then {@code package.json}'s "renovate"
   * key). Returns the first config found; empty if the repo has no Renovate config. A file that
   * exists but fails to parse yields a result with {@code error} set (we do not fall through to the
   * next path, since the repo clearly intended that file to be its config).
   */
  public Mono<RenovateConfigResult> fetchRenovateConfig(String token, String fullName) {
    List<Mono<RenovateConfigResult>> attempts = new ArrayList<>();
    for (String path : props.getConfigPaths()) {
      attempts.add(
          fetchRawFile(token, fullName, path).map(content -> parseConfigFile(content, path)));
    }
    attempts.add(fetchPackageJsonRenovate(token, fullName));
    return Flux.concat(attempts).next();
  }

  /** GET the raw bytes of a file; empty Mono on 404 (file absent). */
  private Mono<String> fetchRawFile(String token, String fullName, String path) {
    // Build the path segment-by-segment. Passing fullName ("owner/repo") or a nested config path
    // (".github/renovate.json") as a single {template} variable encodes the slashes to %2F, which
    // GitHub 404s on — making every repo look config-less. pathSegment() keeps the separators.
    String[] ownerRepo = fullName.split("/", 2);
    return gitHubWebClient
        .get()
        .uri(
            b -> {
              UriBuilder ub = b.path("/repos").pathSegment(ownerRepo[0], ownerRepo[1], "contents");
              for (String seg : path.split("/")) {
                if (!seg.isEmpty()) {
                  ub = ub.pathSegment(seg);
                }
              }
              return ub.build();
            })
        .headers(h -> h.setBearerAuth(token))
        .header(HttpHeaders.ACCEPT, "application/vnd.github.raw+json")
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty());
  }

  private RenovateConfigResult parseConfigFile(String content, String path) {
    try {
      return RenovateConfigResult.found(path, lenientMapper.readTree(content));
    } catch (Exception ex) {
      log.warn("Failed to parse Renovate config {} : {}", path, ex.getMessage());
      return RenovateConfigResult.parseError(
          path, "Failed to parse " + path + ": " + ex.getMessage());
    }
  }

  private Mono<RenovateConfigResult> fetchPackageJsonRenovate(String token, String fullName) {
    return fetchRawFile(token, fullName, props.getPackageJsonPath())
        .flatMap(
            content -> {
              try {
                JsonNode pkg = lenientMapper.readTree(content);
                JsonNode renovate = pkg.get("renovate");
                if (renovate != null && renovate.isObject()) {
                  return Mono.just(
                      RenovateConfigResult.found(props.getPackageJsonPath(), renovate));
                }
              } catch (Exception ex) {
                log.warn("Failed to parse package.json for {}: {}", fullName, ex.getMessage());
              }
              return Mono.empty();
            });
  }
}
