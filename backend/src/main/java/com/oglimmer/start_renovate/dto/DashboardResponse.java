/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

import java.util.List;

/** The full comparison matrix: one entry per enabled repository. */
public record DashboardResponse(List<RepoDashboardEntry> repos) {}
