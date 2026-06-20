/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oglimmer.start_renovate.dto.DashboardResponse;
import com.oglimmer.start_renovate.dto.MeResponse;
import com.oglimmer.start_renovate.dto.RepoConfigResponse;
import com.oglimmer.start_renovate.dto.RepoDashboardEntry;
import com.oglimmer.start_renovate.dto.RepoSummary;
import com.oglimmer.start_renovate.entity.EnabledRepo;
import com.oglimmer.start_renovate.repository.EnabledRepoRepository;
import com.oglimmer.start_renovate.security.ProviderUser;
import com.oglimmer.start_renovate.service.AccessTokenService;
import com.oglimmer.start_renovate.service.AppUserService;
import com.oglimmer.start_renovate.service.RenovateConfigAnalyzer;
import com.oglimmer.start_renovate.service.repo.RenovateConfigResult;
import com.oglimmer.start_renovate.service.repo.RepoProvider;
import com.oglimmer.start_renovate.service.repo.RepoProviderRegistry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoints powering the dashboard. All require an authenticated session (enforced by {@code
 * SecurityConfig}). The provider is selected from the OAuth registration id on the authentication;
 * the access token and local user id are resolved on the servlet thread and passed into the
 * reactive fan-out, which runs on threads without a security context.
 */
@RestController
@RequiredArgsConstructor
public class DashboardController {

  /** Bound concurrency of the per-repo provider fetches to stay well within rate limits. */
  private static final int DASHBOARD_CONCURRENCY = 6;

  private final AccessTokenService tokenService;
  private final RepoProviderRegistry providerRegistry;
  private final AppUserService appUserService;
  private final RenovateConfigAnalyzer analyzer;
  private final EnabledRepoRepository enabledRepoRepository;
  private final ObjectMapper objectMapper;

  @GetMapping("/me")
  public MeResponse me(OAuth2AuthenticationToken authentication) {
    ProviderUser user = providerUser(authentication);
    return new MeResponse(
        user.providerUserId(),
        user.login(),
        user.name(),
        user.avatarUrl(),
        user.provider(),
        provider(authentication).webBaseUrl());
  }

  @GetMapping("/repos")
  public Mono<List<RepoSummary>> repos(OAuth2AuthenticationToken authentication) {
    String token = tokenService.currentAccessToken(authentication);
    Set<String> enabled = enabledFullNames(currentUserId(authentication));
    return provider(authentication)
        .listAllRepos(token)
        .map(
            repos ->
                repos.stream()
                    .map(
                        r ->
                            new RepoSummary(
                                r.fullName(),
                                r.name(),
                                r.owner(),
                                r.isPrivate(),
                                r.defaultBranch(),
                                enabled.contains(r.fullName()),
                                r.hasRenovateConfig()))
                    .toList());
  }

  @PutMapping("/repos/enabled/{*fullName}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void enableRepo(OAuth2AuthenticationToken authentication, @PathVariable String fullName) {
    String repo = normalizeFullName(fullName);
    Long userId = currentUserId(authentication);
    if (!enabledRepoRepository.existsByUserIdAndRepoFullName(userId, repo)) {
      enabledRepoRepository.save(new EnabledRepo(userId, repo));
    }
  }

  @DeleteMapping("/repos/enabled/{*fullName}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void disableRepo(OAuth2AuthenticationToken authentication, @PathVariable String fullName) {
    enabledRepoRepository.deleteByUserIdAndRepoFullName(
        currentUserId(authentication), normalizeFullName(fullName));
  }

  @GetMapping("/dashboard")
  public Mono<DashboardResponse> dashboard(OAuth2AuthenticationToken authentication) {
    RepoProvider provider = provider(authentication);
    String token = tokenService.currentAccessToken(authentication);
    List<String> enabled =
        enabledRepoRepository.findByUserId(currentUserId(authentication)).stream()
            .map(EnabledRepo::getRepoFullName)
            .sorted()
            .toList();

    return Flux.fromIterable(enabled)
        .flatMapSequential(
            fullName -> dashboardEntry(provider, token, fullName), DASHBOARD_CONCURRENCY)
        .collectList()
        .map(DashboardResponse::new);
  }

  /**
   * Returns a single repo's Renovate config as pretty-printed JSON so the SPA can load it into the
   * generator's editor. Uses the user's OAuth token, so unlike the public URL import this works for
   * private repos and for GitLab (whose raw endpoint blocks browser CORS).
   */
  @GetMapping("/repos/config/{*fullName}")
  public Mono<RepoConfigResponse> repoConfig(
      OAuth2AuthenticationToken authentication, @PathVariable String fullName) {
    String repo = normalizeFullName(fullName);
    String token = tokenService.currentAccessToken(authentication);
    return provider(authentication)
        .fetchRenovateConfig(token, repo)
        .switchIfEmpty(
            Mono.error(
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No Renovate config found for " + repo)))
        .map(
            result -> {
              if (result.error() != null) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, result.error());
              }
              return new RepoConfigResponse(repo, result.path(), writeJson(result.config()));
            });
  }

  private String writeJson(JsonNode node) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Could not serialize Renovate config");
    }
  }

  private Mono<RepoDashboardEntry> dashboardEntry(
      RepoProvider provider, String token, String fullName) {
    return provider
        .fetchRenovateConfig(token, fullName)
        .map(result -> toEntry(fullName, result))
        .defaultIfEmpty(new RepoDashboardEntry(fullName, false, null, Map.of(), null))
        .onErrorResume(
            ex -> Mono.just(new RepoDashboardEntry(fullName, false, null, Map.of(), describe(ex))));
  }

  private RepoDashboardEntry toEntry(String fullName, RenovateConfigResult result) {
    if (result.error() != null) {
      return new RepoDashboardEntry(fullName, true, result.path(), Map.of(), result.error());
    }
    return new RepoDashboardEntry(
        fullName, true, result.path(), analyzer.analyze(result.config()), null);
  }

  private Set<String> enabledFullNames(Long userId) {
    return enabledRepoRepository.findByUserId(userId).stream()
        .map(EnabledRepo::getRepoFullName)
        .collect(Collectors.toSet());
  }

  private RepoProvider provider(OAuth2AuthenticationToken authentication) {
    return providerRegistry.get(authentication.getAuthorizedClientRegistrationId());
  }

  private ProviderUser providerUser(OAuth2AuthenticationToken authentication) {
    return ProviderUser.from(
        authentication.getAuthorizedClientRegistrationId(), authentication.getPrincipal());
  }

  private Long currentUserId(OAuth2AuthenticationToken authentication) {
    return appUserService.resolveId(providerUser(authentication));
  }

  /** The {@code {*fullName}} capture includes a leading slash; strip it to get "owner/repo". */
  private static String normalizeFullName(String captured) {
    return captured.startsWith("/") ? captured.substring(1) : captured;
  }

  private String describe(Throwable ex) {
    String msg = ex.getMessage();
    return msg != null ? msg : ex.getClass().getSimpleName();
  }
}
