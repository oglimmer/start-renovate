/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/feedback}.
 *
 * <p>{@code renovateJson} is bounded before it is schema-validated and embedded into the LLM
 * prompt: a real renovate.json is a few KB, so a 64 KB cap rejects oversized payloads (token-cost /
 * memory abuse) while leaving generous headroom.
 */
public record RenovateFeedbackRequest(
    @NotBlank(message = "renovateJson must not be blank")
        @Size(max = 65536, message = "renovateJson must not exceed 64 KB")
        String renovateJson) {}
