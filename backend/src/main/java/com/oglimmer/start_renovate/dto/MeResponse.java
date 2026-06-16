/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

/** The currently authenticated GitHub user. */
public record MeResponse(Long id, String login, String name, String avatarUrl) {}
