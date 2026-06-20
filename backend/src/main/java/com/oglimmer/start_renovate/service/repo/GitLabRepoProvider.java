/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service.repo;

import com.fasterxml.jackson.databind.JsonNode;
import com.oglimmer.start_renovate.config.GitLabProperties;
import com.oglimmer.start_renovate.config.RenovateDetectionProperties;
import com.oglimmer.start_renovate.dto.RepoSummary;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Talks to the GitLab REST API (v4) on behalf of the authenticated user. The shape mirrors {@link
 * GitHubRepoProvider}, but a few GitLab specifics drive the differences:
 *
 * <ul>
 *   <li>A project is addressed by its URL-encoded {@code path_with_namespace} (e.g. {@code
 *       group%2Fsub%2Frepo}); subgroups mean this "full name" can have more than two segments.
 *   <li>The repository tree endpoint returns a flat JSON array (not a {@code {"tree": [...]}}
 *       object) and lists a single directory level, selected with the {@code path} query param.
 *   <li>File contents come from {@code /repository/files/:file_path/raw}; both the project id and
 *       file path are URL-encoded. The ref defaults to the project's default branch when omitted.
 * </ul>
 */
@Slf4j
@Service
public class GitLabRepoProvider implements RepoProvider {

  private static final int MAX_REPO_PAGES = 30;
  private static final int PER_PAGE = 100;
  private static final int CONFIG_DETECTION_CONCURRENCY = 16;

  private final WebClient gitLabWebClient;
  private final GitLabProperties props;
  private final RenovateDetectionProperties detection;
  private final RenovateConfigParser parser;

  public GitLabRepoProvider(
      WebClient gitLabWebClient,
      GitLabProperties props,
      RenovateDetectionProperties detection,
      RenovateConfigParser parser) {
    this.gitLabWebClient = gitLabWebClient;
    this.props = props;
    this.detection = detection;
    this.parser = parser;
  }

  @Override
  public String id() {
    return "gitlab";
  }

  @Override
  public String webBaseUrl() {
    return props.getWebBaseUrl();
  }

  @Override
  public Mono<List<RepoSummary>> listAllRepos(String token) {
    return listProjectMetadata(token)
        .flatMapMany(Flux::fromIterable)
        .flatMapSequential(
            repo ->
                hasRenovateConfigFile(token, repo.fullName())
                    .map(hasConfig -> withRenovateConfig(repo, hasConfig)),
            CONFIG_DETECTION_CONCURRENCY)
        .collectSortedList(Comparator.comparing(RepoSummary::fullName));
  }

  private static RepoSummary withRenovateConfig(RepoSummary repo, boolean hasConfig) {
    return new RepoSummary(
        repo.fullName(),
        repo.name(),
        repo.owner(),
        repo.isPrivate(),
        repo.defaultBranch(),
        repo.enabled(),
        hasConfig);
  }

  // --- Renovate config detection (one tree call per repo, plus per-subdir when needed) ----------

  private Mono<Boolean> hasRenovateConfigFile(String token, String fullName) {
    Set<String> rootNames = new HashSet<>();
    Map<String, Set<String>> subdirNames = new HashMap<>(); // first path segment -> filenames
    for (String path : detection.getConfigPaths()) {
      int slash = path.indexOf('/');
      if (slash < 0) {
        rootNames.add(path);
      } else {
        subdirNames
            .computeIfAbsent(path.substring(0, slash), k -> new HashSet<>())
            .add(path.substring(slash + 1));
      }
    }
    return fetchTree(token, fullName, null)
        .flatMap(
            rootTree -> {
              if (treeHasBlob(rootTree, rootNames)) {
                return Mono.just(true);
              }
              List<Mono<Boolean>> subChecks = new ArrayList<>();
              for (Map.Entry<String, Set<String>> dir : subdirNames.entrySet()) {
                if (treeHasSubdir(rootTree, dir.getKey())) {
                  subChecks.add(
                      fetchTree(token, fullName, dir.getKey())
                          .map(subtree -> treeHasBlob(subtree, dir.getValue()))
                          .defaultIfEmpty(false));
                }
              }
              return subChecks.isEmpty()
                  ? Mono.just(false)
                  : Flux.merge(subChecks).any(Boolean::booleanValue);
            })
        .defaultIfEmpty(false);
  }

  /**
   * GET one directory level of a project's tree (root when {@code dir} is null); empty Mono on any
   * error (no commits yet, missing path, transient). The ref is omitted so GitLab uses the default
   * branch.
   */
  private Mono<JsonNode> fetchTree(String token, String fullName, String dir) {
    StringBuilder uri =
        new StringBuilder(props.getApiBaseUrl())
            .append("/projects/")
            .append(enc(fullName))
            .append("/repository/tree?per_page=")
            .append(PER_PAGE);
    if (dir != null) {
      uri.append("&path=").append(enc(dir));
    }
    return gitLabWebClient
        .get()
        .uri(URI.create(uri.toString()))
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .toEntity(String.class)
        .flatMap(response -> Mono.justOrEmpty(parser.parseOrNull(response.getBody())))
        // Best-effort detection probe: any failure (HTTP error, connect/response timeout, no
        // commits yet) resolves to "no config" so a single bad project never sinks the listing.
        .onErrorResume(ex -> Mono.empty());
  }

  /** True if the flat tree array holds a blob whose basename is in {@code names}. */
  private static boolean treeHasBlob(JsonNode tree, Set<String> names) {
    if (names.isEmpty() || !tree.isArray()) {
      return false;
    }
    for (JsonNode entry : tree) {
      if ("blob".equals(entry.path("type").asText())
          && names.contains(entry.path("name").asText())) {
        return true;
      }
    }
    return false;
  }

  /** True if the flat tree array holds a directory with the given basename. */
  private static boolean treeHasSubdir(JsonNode tree, String dir) {
    if (!tree.isArray()) {
      return false;
    }
    for (JsonNode entry : tree) {
      if ("tree".equals(entry.path("type").asText()) && dir.equals(entry.path("name").asText())) {
        return true;
      }
    }
    return false;
  }

  // --- REST listing --------------------------------------------------------------------------

  private Mono<List<RepoSummary>> listProjectMetadata(String token) {
    return fetchProjectPage(token, 1)
        .expand(
            page ->
                page.size() < PER_PAGE ? Mono.empty() : fetchProjectPage(token, page.page() + 1))
        .take(MAX_REPO_PAGES)
        .concatMapIterable(ProjectPage::repos)
        .collectList();
  }

  private record ProjectPage(List<RepoSummary> repos, int page, int size) {}

  private Mono<ProjectPage> fetchProjectPage(String token, int page) {
    String uri =
        props.getApiBaseUrl()
            + "/projects?membership=true&per_page="
            + PER_PAGE
            + "&page="
            + page
            + "&order_by=path&sort=asc";
    return gitLabWebClient
        .get()
        .uri(URI.create(uri))
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .toEntity(String.class)
        .map(
            response -> {
              List<RepoSummary> repos = parseProjects(parser.parseOrNull(response.getBody()));
              return new ProjectPage(repos, page, repos.size());
            });
  }

  private List<RepoSummary> parseProjects(JsonNode body) {
    List<RepoSummary> repos = new ArrayList<>();
    if (body == null || !body.isArray()) {
      return repos;
    }
    for (JsonNode node : body) {
      String fullName = node.path("path_with_namespace").asText(null);
      if (fullName == null) {
        continue;
      }
      String visibility = node.path("visibility").asText("private");
      repos.add(
          new RepoSummary(
              fullName,
              node.path("path").asText(null),
              node.path("namespace").path("full_path").asText(null),
              !"public".equals(visibility),
              node.path("default_branch").asText(null),
              false,
              false));
    }
    return repos;
  }

  @Override
  public Mono<RenovateConfigResult> fetchRenovateConfig(String token, String fullName) {
    List<Mono<RenovateConfigResult>> attempts = new ArrayList<>();
    for (String path : detection.getConfigPaths()) {
      attempts.add(
          fetchRawFile(token, fullName, path).map(content -> parser.parseConfigFile(content, path)));
    }
    attempts.add(fetchPackageJsonRenovate(token, fullName));
    return Flux.concat(attempts).next();
  }

  /** GET the raw bytes of a file; empty Mono on 404 (file absent). Ref defaults to default branch. */
  private Mono<String> fetchRawFile(String token, String fullName, String path) {
    String uri =
        props.getApiBaseUrl()
            + "/projects/"
            + enc(fullName)
            + "/repository/files/"
            + enc(path)
            + "/raw";
    return gitLabWebClient
        .get()
        .uri(URI.create(uri))
        .headers(h -> h.setBearerAuth(token))
        .retrieve()
        .bodyToMono(String.class)
        .onErrorResume(WebClientResponseException.NotFound.class, ex -> Mono.empty());
  }

  private Mono<RenovateConfigResult> fetchPackageJsonRenovate(String token, String fullName) {
    return fetchRawFile(token, fullName, detection.getPackageJsonPath())
        .flatMap(
            content ->
                Mono.justOrEmpty(
                    parser
                        .extractPackageJsonRenovate(content)
                        .map(
                            renovate ->
                                RenovateConfigResult.found(
                                    detection.getPackageJsonPath(), renovate))));
  }

  /** URL-encodes a path component, turning {@code /} into {@code %2F} for GitLab's encoded ids. */
  private static String enc(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
