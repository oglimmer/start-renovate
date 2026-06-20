/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service.repo;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Provider-independent parsing of Renovate config payloads. Uses a tolerant JSON reader so
 * hand-written {@code .json5} / commented configs still map onto the comparison matrix. Shared by
 * every {@link RepoProvider} so the parsing rules never drift between platforms.
 */
@Slf4j
@Component
public class RenovateConfigParser {

  private final ObjectMapper lenientMapper =
      JsonMapper.builder()
          .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
          .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
          .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
          .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
          .build();

  /** Parses an arbitrary JSON document, returning null on blank input or a parse failure. */
  public JsonNode parseOrNull(String body) {
    if (body == null || body.isBlank()) {
      return null;
    }
    try {
      return lenientMapper.readTree(body);
    } catch (Exception ex) {
      log.warn("Failed to parse JSON response: {}", ex.getMessage());
      return null;
    }
  }

  /** Parses a dedicated Renovate config file, surfacing a parse failure as an error result. */
  public RenovateConfigResult parseConfigFile(String content, String path) {
    try {
      return RenovateConfigResult.found(path, lenientMapper.readTree(content));
    } catch (Exception ex) {
      log.warn("Failed to parse Renovate config {} : {}", path, ex.getMessage());
      return RenovateConfigResult.parseError(
          path, "Failed to parse " + path + ": " + ex.getMessage());
    }
  }

  /** Extracts the embedded {@code "renovate"} object from a {@code package.json} body, if present. */
  public Optional<JsonNode> extractPackageJsonRenovate(String packageJsonContent) {
    try {
      JsonNode pkg = lenientMapper.readTree(packageJsonContent);
      JsonNode renovate = pkg.get("renovate");
      if (renovate != null && renovate.isObject()) {
        return Optional.of(renovate);
      }
    } catch (Exception ex) {
      log.warn("Failed to parse package.json: {}", ex.getMessage());
    }
    return Optional.empty();
  }
}
