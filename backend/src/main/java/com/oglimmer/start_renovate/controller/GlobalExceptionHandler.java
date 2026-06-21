/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Central error contract for the API. Extends {@link ResponseEntityExceptionHandler} so the
 * standard Spring MVC exceptions (validation, malformed body, 404/405/415, …) keep their correct
 * status codes and {@link ProblemDetail} bodies — we only add handling for our own {@link
 * ResponseStatusException} (preserving its intended status) and a sanitizing catch-all so
 * unexpected failures never leak internal/upstream detail to clients (the real cause is logged
 * server-side instead).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ProblemDetail> handleResponseStatus(ResponseStatusException ex) {
    HttpStatusCode status = ex.getStatusCode();
    String detail = ex.getReason() != null ? ex.getReason() : "Request failed.";
    return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(status, detail));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
    // Log the full cause server-side; return a generic body so upstream/internal messages and stack
    // detail never reach the client.
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An internal error occurred."));
  }
}
