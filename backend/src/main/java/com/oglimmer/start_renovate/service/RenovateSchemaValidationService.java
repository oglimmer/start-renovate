/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RenovateSchemaValidationService {

  private Schema schema;

  @PostConstruct
  public void init() {
    try {
      SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
      InputStream schemaStream = getClass().getResourceAsStream("/renovate-schema.json");
      if (schemaStream == null) {
        throw new IllegalStateException("Could not load renovate-schema.json from resources");
      }
      schema = registry.getSchema(schemaStream);
      log.info("Renovate JSON schema loaded successfully");
    } catch (Exception e) {
      log.error("Failed to load Renovate JSON schema", e);
      throw new IllegalStateException("Failed to initialize schema validator", e);
    }
  }

  public ValidationResult validate(String jsonString) {
    try {
      List<Error> errors = schema.validate(jsonString, InputFormat.JSON);

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

    public static ValidationResult failure(List<Error> errors) {
      StringBuilder sb = new StringBuilder("JSON validation failed:\n");
      errors.forEach(error -> sb.append("- ").append(error.getMessage()).append("\n"));
      return new ValidationResult(false, sb.toString().trim());
    }

    public static ValidationResult failure(String message) {
      return new ValidationResult(false, message);
    }
  }
}
