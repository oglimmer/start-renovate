/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import com.oglimmer.start_renovate.dto.DashboardResponse;
import com.oglimmer.start_renovate.dto.MeResponse;
import com.oglimmer.start_renovate.dto.RepoDashboardEntry;
import com.oglimmer.start_renovate.dto.RepoSummary;
import com.oglimmer.start_renovate.entity.EnabledRepo;
import com.oglimmer.start_renovate.repository.EnabledRepoRepository;
import com.oglimmer.start_renovate.service.GitHubApiService;
import com.oglimmer.start_renovate.service.GitHubApiService.RenovateConfigResult;
import com.oglimmer.start_renovate.service.GitHubTokenService;
import com.oglimmer.start_renovate.service.RenovateConfigAnalyzer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Endpoints powering the GitHub dashboard. All require an authenticated session (enforced by {@code
 * SecurityConfig}). The GitHub token and user id are resolved on the servlet thread and passed into
 * the reactive fan-out, which runs on threads without a security context.
 */
@RestController
@RequiredArgsConstructor
public class DashboardController {

  /** Bound concurrency of the per-repo GitHub fetches to stay well within rate limits. */
  private static final int DASHBOARD_CONCURRENCY = 6;

  private final GitHubTokenService tokenService;
  private final GitHubApiService gitHubApiService;
  private final RenovateConfigAnalyzer analyzer;
  private final EnabledRepoRepository enabledRepoRepository;

  @GetMapping("/me")
  public MeResponse me(@AuthenticationPrincipal OAuth2User principal) {
    Object rawId = principal.getAttribute("id");
    Long id = rawId instanceof Number number ? number.longValue() : null;
    return new MeResponse(
        id,
        principal.getAttribute("login"),
        principal.getAttribute("name"),
        principal.getAttribute("avatar_url"));
  }

  @GetMapping("/repos")
  public Mono<List<RepoSummary>> repos(@AuthenticationPrincipal OAuth2User principal) {
    String token = tokenService.currentAccessToken();
    Set<String> enabled = enabledFullNames(currentUserId(principal));
    return gitHubApiService
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

  @PutMapping("/repos/{owner}/{name}/enabled")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void enableRepo(
      @AuthenticationPrincipal OAuth2User principal,
      @PathVariable String owner,
      @PathVariable String name) {
    Long userId = currentUserId(principal);
    String fullName = owner + "/" + name;
    if (!enabledRepoRepository.existsByUserIdAndRepoFullName(userId, fullName)) {
      enabledRepoRepository.save(new EnabledRepo(userId, fullName));
    }
  }

  @DeleteMapping("/repos/{owner}/{name}/enabled")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void disableRepo(
      @AuthenticationPrincipal OAuth2User principal,
      @PathVariable String owner,
      @PathVariable String name) {
    enabledRepoRepository.deleteByUserIdAndRepoFullName(
        currentUserId(principal), owner + "/" + name);
  }

  @GetMapping("/dashboard")
  public Mono<DashboardResponse> dashboard(@AuthenticationPrincipal OAuth2User principal) {
    String token = tokenService.currentAccessToken();
    List<String> enabled =
        enabledRepoRepository.findByUserId(currentUserId(principal)).stream()
            .map(EnabledRepo::getRepoFullName)
            .sorted()
            .toList();

    return Flux.fromIterable(enabled)
        .flatMapSequential(fullName -> dashboardEntry(token, fullName), DASHBOARD_CONCURRENCY)
        .collectList()
        .map(DashboardResponse::new);
  }

  private Mono<RepoDashboardEntry> dashboardEntry(String token, String fullName) {
    return gitHubApiService
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

  private Long currentUserId(OAuth2User principal) {
    // GitHub's provider defaults use the numeric "id" as the principal name.
    return Long.valueOf(principal.getName());
  }

  private String describe(Throwable ex) {
    String msg = ex.getMessage();
    return msg != null ? msg : ex.getClass().getSimpleName();
  }
}
