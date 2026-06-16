/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

import java.util.Map;

/**
 * One repository's column in the comparison matrix.
 *
 * @param fullName owner/name
 * @param hasRenovate whether a Renovate config file was found
 * @param configFilePath which file the config came from (null when none/error)
 * @param options optionId → {@link CellState}; empty when no config or on error
 * @param error a per-repo error message (fetch/parse failure), else null
 */
public record RepoDashboardEntry(
    String fullName,
    boolean hasRenovate,
    String configFilePath,
    Map<String, CellState> options,
    String error) {}
