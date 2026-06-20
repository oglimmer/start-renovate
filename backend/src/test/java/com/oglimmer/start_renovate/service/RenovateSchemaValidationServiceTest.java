/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.oglimmer.start_renovate.service.RenovateSchemaValidationService.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RenovateSchemaValidationServiceTest {

  private RenovateSchemaValidationService service;

  @BeforeEach
  void setUp() {
    service = new RenovateSchemaValidationService();
    service.init();
  }

  @Test
  void acceptsValidConfig() {
    ValidationResult result = service.validate("{\"addLabels\": [\"dependencies\"]}");
    assertTrue(result.valid());
    assertNull(result.errorMessage());
  }

  @Test
  void rejectsSchemaViolation() {
    // addLabels must be an array of strings, not a plain string.
    ValidationResult result = service.validate("{\"addLabels\": \"dependencies\"}");
    assertFalse(result.valid());
    assertNotNull(result.errorMessage());
    assertTrue(result.errorMessage().contains("JSON validation failed"));
  }

  @Test
  void rejectsMalformedJson() {
    ValidationResult result = service.validate("{ not json");
    assertFalse(result.valid());
    assertNotNull(result.errorMessage());
  }
}
