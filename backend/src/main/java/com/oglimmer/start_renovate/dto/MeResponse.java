/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

/**
 * The currently authenticated user. {@code provider} is the OAuth provider id (e.g. {@code github},
 * {@code gitlab}) and {@code webBaseUrl} is the base for building links to that user's repositories
 * ({@code <webBaseUrl>/<fullName>}). {@code id} is the provider's own user id.
 */
public record MeResponse(
    String id, String login, String name, String avatarUrl, String provider, String webBaseUrl) {}
