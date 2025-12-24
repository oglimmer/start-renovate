/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RenovateSchemaValidationService {

  private final ObjectMapper objectMapper;
  private JsonSchema schema;

  @PostConstruct
  public void init() {
    try {
      JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
      InputStream schemaStream =
          getClass().getResourceAsStream("/renovate-schema.json");
      if (schemaStream == null) {
        throw new IllegalStateException("Could not load renovate-schema.json from resources");
      }
      schema = factory.getSchema(schemaStream);
      log.info("Renovate JSON schema loaded successfully");
    } catch (Exception e) {
      log.error("Failed to load Renovate JSON schema", e);
      throw new IllegalStateException("Failed to initialize schema validator", e);
    }
  }

  public ValidationResult validate(String jsonString) {
    try {
      JsonNode jsonNode = objectMapper.readTree(jsonString);
      Set<ValidationMessage> errors = schema.validate(jsonNode);

      if (errors.isEmpty()) {
        return ValidationResult.success();
      } else {
        return ValidationResult.failure(errors);
      }
    } catch (Exception e) {
      log.error("Error parsing or validating JSON", e);
      return ValidationResult.failure("Invalid JSON: " + e.getMessage());
    }
  }

  public record ValidationResult(boolean valid, String errorMessage) {
    public static ValidationResult success() {
      return new ValidationResult(true, null);
    }

    public static ValidationResult failure(Set<ValidationMessage> errors) {
      StringBuilder sb = new StringBuilder("JSON validation failed:\n");
      errors.forEach(error -> sb.append("- ").append(error.getMessage()).append("\n"));
      return new ValidationResult(false, sb.toString().trim());
    }

    public static ValidationResult failure(String message) {
      return new ValidationResult(false, message);
    }
  }
}
