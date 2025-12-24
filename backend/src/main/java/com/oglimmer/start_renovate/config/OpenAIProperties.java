/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
@Getter
@Setter
public class OpenAIProperties {

  private String apiKey;
  private String baseUrl;
  private String model;
  private int maxOutputTokens = 800;
}
