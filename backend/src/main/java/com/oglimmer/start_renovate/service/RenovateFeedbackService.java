/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oglimmer.start_renovate.config.DeepSeekProperties;
import com.oglimmer.start_renovate.dto.RenovateFeedbackResponse;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenovateFeedbackService {

  private static final String INSTRUCTIONS =
      "You are an expert on Renovate configuration and best practices. "
          + "Return a JSON object with fields: summary (string), issues (array of "
          + "{severity: info|warning|error, jsonPath: string, message: string, suggestion: string}), "
          + "improvedRenovateJson (string). No extra commentary. Limit issues to at most 8 items. "
          + "Keep summary to 1-2 sentences. "
          + "Example response: {\"summary\": \"...\", \"issues\": [{\"severity\": \"warning\", "
          + "\"jsonPath\": \"$.extends\", \"message\": \"...\", \"suggestion\": \"...\"}], "
          + "\"improvedRenovateJson\": \"{...}\"}";

  private final WebClient deepSeekWebClient;
  private final DeepSeekProperties props;
  private final ObjectMapper objectMapper;

  public Mono<RenovateFeedbackResponse> getFeedback(String json) {
    String prompt = buildPrompt(json);

    long initialTokens = Math.max(1200L, props.getMaxOutputTokens());

    return requestChat(prompt, initialTokens)
        .flatMap(
            res -> {
              // DeepSeek returns finish_reason "length" when the answer was truncated against the
              // token budget. Retry once with a much larger budget before giving up.
              if ("length".equalsIgnoreCase(finishReason(res))) {
                long retryTokens = Math.min(Math.max(initialTokens * 2, 4096), 8192);
                log.warn(
                    "Retrying due to truncated response (finish_reason=length). prevTokens={},"
                        + " retryTokens={}",
                    initialTokens,
                    retryTokens);
                return requestChat(prompt, retryTokens);
              }
              return Mono.just(res);
            })
        .map(res -> toDto(res, json))
        .onErrorResume(
            ex -> {
              log.error("Failed to get feedback from DeepSeek: {}", ex.getMessage(), ex);
              return Mono.just(
                  new RenovateFeedbackResponse(
                      "Failed to generate feedback: " + ex.getMessage(), List.of(), json));
            });
  }

  private Mono<JsonNode> requestChat(String prompt, long maxOutputTokens) {
    Map<String, Object> body =
        Map.of(
            "model",
            props.getModel(),
            "messages",
            List.of(
                Map.of("role", "system", "content", INSTRUCTIONS),
                Map.of("role", "user", "content", prompt)),
            "response_format",
            Map.of("type", "json_object"),
            "max_tokens",
            maxOutputTokens,
            "stream",
            false);

    // Decode as String and parse with our Jackson 2 mapper: Spring 7's default WebClient codec is
    // Jackson 3 (tools.jackson), which can't construct a Jackson 2 com.fasterxml JsonNode.
    return deepSeekWebClient
        .post()
        .uri("/chat/completions")
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .map(this::parseTree);
  }

  private JsonNode parseTree(String body) {
    try {
      return objectMapper.readTree(body);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse DeepSeek response body: " + ex.getMessage());
    }
  }

  private String finishReason(JsonNode response) {
    return response.path("choices").path(0).path("finish_reason").asText("");
  }

  private RenovateFeedbackResponse toDto(JsonNode response, String originalJson) {
    String content = response.path("choices").path(0).path("message").path("content").asText(null);

    if (content == null || content.isBlank()) {
      log.warn("No content returned by DeepSeek. response={}", response);
      return new RenovateFeedbackResponse(
          "No structured output returned by the model.", List.of(), originalJson);
    }

    try {
      // The model occasionally emits non-standard backslash escapes (e.g. "\." in a regex or
      // path) inside string fields, which strict JSON rejects. Parse leniently so these survive.
      return objectMapper
          .reader()
          .with(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
          .readValue(content, RenovateFeedbackResponse.class);
    } catch (Exception ex) {
      log.error("Failed to parse DeepSeek JSON content: {}. content={}", ex.getMessage(), content);
      return new RenovateFeedbackResponse(
          "Failed to parse model output: " + ex.getMessage(), List.of(), originalJson);
    }
  }

  private String buildPrompt(String renovateJson) {
    return String.join(
        "\n",
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
