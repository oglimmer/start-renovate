/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

/**
 * One repository the user can see, with a flag indicating whether it is tracked on the dashboard.
 */
public record RepoSummary(
    String fullName,
    String name,
    String owner,
    boolean isPrivate,
    String defaultBranch,
    boolean enabled) {}
