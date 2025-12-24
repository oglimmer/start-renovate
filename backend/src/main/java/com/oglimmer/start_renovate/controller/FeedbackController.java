/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import com.oglimmer.start_renovate.dto.RenovateFeedbackRequest;
import com.oglimmer.start_renovate.dto.RenovateFeedbackResponse;
import com.oglimmer.start_renovate.service.RenovateFeedbackService;
import com.oglimmer.start_renovate.service.RenovateSchemaValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FeedbackController {

  private final RenovateFeedbackService service;
  private final RenovateSchemaValidationService validationService;

  @PostMapping
  public Mono<RenovateFeedbackResponse> getFeedback(@RequestBody RenovateFeedbackRequest request) {
    var validationResult = validationService.validate(request.renovateJson());
    if (!validationResult.valid()) {
      return Mono.error(
          new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Invalid Renovate configuration: " + validationResult.errorMessage()));
    }
    return service.getFeedback(request.renovateJson());
  }
}
