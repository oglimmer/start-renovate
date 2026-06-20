/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service.repo;

import com.oglimmer.start_renovate.dto.RepoSummary;
import java.util.List;
import reactor.core.publisher.Mono;

/**
 * A source-hosting platform (GitHub, GitLab, ...) the dashboard can talk to on behalf of the
 * authenticated user. One implementation per platform; {@link #id()} matches the Spring Security
 * OAuth registration id, which is how a request is routed to the right provider.
 */
public interface RepoProvider {

  /** The OAuth registration id this provider serves (e.g. {@code github}, {@code gitlab}). */
  String id();

  /** Base URL for building human-facing links to a repository: {@code <webBaseUrl>/<fullName>}. */
  String webBaseUrl();

  /**
   * Lists every repository the user can access, flagging which already carry a dedicated Renovate
   * config file at the default-branch tip.
   */
  Mono<List<RepoSummary>> listAllRepos(String token);

  /**
   * Fetches the repository's Renovate config (the first known config path that exists, then a
   * {@code package.json} "renovate" key). Empty when the repo has no Renovate config.
   */
  Mono<RenovateConfigResult> fetchRenovateConfig(String token, String fullName);
}
