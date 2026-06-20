/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service.repo;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Result of probing a repository for a Renovate config. {@code config} is null when {@code error}
 * is set (the intended config file exists but could not be parsed).
 */
public record RenovateConfigResult(String path, JsonNode config, String error) {

  public static RenovateConfigResult found(String path, JsonNode config) {
    return new RenovateConfigResult(path, config, null);
  }

  public static RenovateConfigResult parseError(String path, String error) {
    return new RenovateConfigResult(path, null, error);
  }
}
