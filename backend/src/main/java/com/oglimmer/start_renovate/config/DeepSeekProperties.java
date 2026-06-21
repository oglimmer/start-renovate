/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
@Getter
@Setter
public class DeepSeekProperties {

  private String apiKey;
  private String baseUrl;
  private String model;
  private int maxOutputTokens = 800;

  /** TCP connect timeout for the DeepSeek client. */
  private int connectTimeoutMillis = 10_000;

  /**
   * Response timeout for a single DeepSeek call. Kept comfortably under {@code
   * spring.mvc.async.request-timeout} (120s) so even the token-budget retry (a second call) cannot
   * exhaust the async request budget on a hung upstream.
   */
  private int responseTimeoutSeconds = 45;
}
