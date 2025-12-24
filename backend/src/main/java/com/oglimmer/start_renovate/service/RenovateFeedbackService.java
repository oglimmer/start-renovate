/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.oglimmer.start_renovate.config.OpenAIProperties;
import com.oglimmer.start_renovate.dto.RenovateFeedbackResponse;
import com.openai.client.OpenAIClient;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import com.openai.models.responses.Response.IncompleteDetails;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenovateFeedbackService {

  private final OpenAIClient openAIClient;
  private final OpenAIProperties props;

  public Mono<RenovateFeedbackResponse> getFeedback(String json) {
    String prompt = buildPrompt(json);

    long initialTokens = Math.max(1200L, props.getMaxOutputTokens());

    return requestStructured(prompt, initialTokens)
        .flatMap(
            res -> {
              var status = res.status().orElse(null);
              var reason = res.incompleteDetails().flatMap(IncompleteDetails::reason).orElse(null);
              if (status == ResponseStatus.INCOMPLETE) {
                String reasonStr = reason != null ? reason.asString() : "unknown";
                // If truncated or unknown cause, retry with a much larger token budget once.
                if (!"CONTENT_FILTER".equalsIgnoreCase(reasonStr)) {
                  long retryTokens = Math.min(Math.max(initialTokens * 2, 4096), 8192);
                  log.warn(
                      "Retrying due to incomplete response (reason={}). prevTokens={}, retryTokens={}",
                      reasonStr,
                      initialTokens,
                      retryTokens);
                  return requestStructured(prompt, retryTokens);
                }
              }
              return Mono.just(res);
            })
        .map(res -> toDto(res, json))
        .onErrorResume(
            ex -> {
              log.error("Failed to get feedback from OpenAI SDK: {}", ex.getMessage(), ex);
              return Mono.just(
                  new RenovateFeedbackResponse(
                      "Failed to generate feedback: " + ex.getMessage(),
                      java.util.List.of(),
                      json));
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  private Mono<StructuredResponse<RenovateFeedbackResponse>> requestStructured(
      String prompt, long maxOutputTokens) {
    return Mono.fromCallable(
            () -> {
              ResponseCreateParams.Builder base =
                  ResponseCreateParams.builder()
                      .model(props.getModel())
                      .input(prompt)
                      .instructions(
                          "Return a JSON object with fields: summary (string), issues (array of {severity: info|warning|error, jsonPath: string, message: string, suggestion: string}), improvedRenovateJson (string). No extra commentary. Limit issues to at most 8 items. Keep summary to 1-2 sentences.")
                      .maxOutputTokens(maxOutputTokens);

              // Try to minimize reasoning output to conserve tokens. Some models don't accept
              // 'NONE'.
              try {
                base.reasoning(Reasoning.builder().effort(ReasoningEffort.MINIMAL).build());
              } catch (Exception ignored) {
                // If this model doesn't support 'reasoning', ignore silently
              }

              StructuredResponseCreateParams<RenovateFeedbackResponse> params =
                  base.text(RenovateFeedbackResponse.class).build();
              return openAIClient.responses().create(params);
            })
        .subscribeOn(Schedulers.boundedElastic());
  }

  private RenovateFeedbackResponse toDto(
      StructuredResponse<RenovateFeedbackResponse> response, String originalJson) {
    if (response.error().isPresent()) {
      String summary = "OpenAI returned an error";
      try {
        summary = summary + ": " + response.error().get().message();
      } catch (Exception ignored) {
      }
      try {
        log.error(
            "OpenAI response error. id={}, model={}, status={}, error={}",
            response.id(),
            response.model(),
            response.status().orElse(null),
            response.error().get());
      } catch (Exception logEx) {
        log.error(
            "OpenAI response error (logging failed extracting fields): {}", logEx.getMessage());
      }
      return new RenovateFeedbackResponse(summary, java.util.List.of(), originalJson);
    }

    // Try extracting the typed output
    var maybe =
        response.output().stream()
            .map(item -> item.message().orElse(null))
            .filter(Objects::nonNull)
            .flatMap(msg -> msg.content().stream())
            .map(content -> content.outputText().orElse(null))
            .filter(Objects::nonNull)
            .findFirst();

    if (maybe.isPresent()) {
      return maybe.get();
    }

    // Check for safety refusal and provide a meaningful fallback
    boolean refused =
        response.output().stream()
            .map(item -> item.message().orElse(null))
            .filter(Objects::nonNull)
            .flatMap(msg -> msg.content().stream())
            .anyMatch(content -> content.refusal().isPresent());

    if (refused) {
      try {
        log.warn(
            "OpenAI refused to provide structured output. id={}, model={}, status={}",
            response.id(),
            response.model(),
            response.status().orElse(null));
      } catch (Exception logEx) {
        log.warn("OpenAI refusal (logging failed extracting fields): {}", logEx.getMessage());
      }
      return new RenovateFeedbackResponse(
          "Model refused to generate structured output due to safety policy.",
          java.util.List.of(),
          originalJson);
    }

    // Generic graceful fallback
    try {
      log.warn(
          "No structured output returned by model. id={}, model={}, status={}, reason={}, outputs={}",
          response.id(),
          response.model(),
          response.status().orElse(null),
          response
              .incompleteDetails()
              .flatMap(d -> d.reason())
              .map(r -> r.asString())
              .orElse("unknown"),
          response.output());
    } catch (Exception logEx) {
      log.warn("No structured output (logging failed extracting fields): {}", logEx.getMessage());
    }
    return new RenovateFeedbackResponse(
        "No structured output returned by the model.", java.util.List.of(), originalJson);
  }

  private String buildPrompt(String renovateJson) {
    return String.join(
        "\n",
        "You are an expert on Renovate configuration and best practices.",
        "Analyze the provided Renovate JSON config and produce:",
        "- A short summary of the configuration and its quality.",
        "- A list of concrete issues with fields: severity (info|warning|error), jsonPath, message, suggestion.",
        "- An improvedRenovateJson containing a corrected/optimized config (valid JSON).",
        "Respond strictly as a JSON object matching the specified fields. Do not include any extra text.",
        "",
        "Renovate JSON:",
        renovateJson);
  }
}
