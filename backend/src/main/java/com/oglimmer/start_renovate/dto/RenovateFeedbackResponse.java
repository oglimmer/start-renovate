/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.dto;

import java.util.List;

public record RenovateFeedbackResponse(
    String summary, List<Issue> issues, String improvedRenovateJson) {

  public record Issue(String severity, String jsonPath, String message, String suggestion) {}
}
