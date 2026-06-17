/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

/**
 * One repository the user can see. {@code enabled} indicates whether it is tracked on the
 * dashboard; {@code hasRenovateConfig} whether a dedicated Renovate config file already exists at
 * the default-branch tip (detected cheaply during listing — an embedded {@code package.json}
 * "renovate" key is not counted here).
 */
public record RepoSummary(
    String fullName,
    String name,
    String owner,
    boolean isPrivate,
    String defaultBranch,
    boolean enabled,
    boolean hasRenovateConfig) {}
