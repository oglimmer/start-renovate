/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Builds the shared {@link WebClient} used to call the GitHub REST API. Mirrors {@link
 * OpenAIWebClientConfig}. Deliberately carries NO default Authorization header — each request sets
 * the current user's bearer token, since the token is per-user and resolved at request time.
 */
@Configuration
@EnableConfigurationProperties(GitHubProperties.class)
public class GitHubWebClientConfig {

  @Bean
  public WebClient gitHubWebClient(GitHubProperties props) {
    // Repo listings for users with many repos can be large; allow generous buffering.
    int maxInMemorySizeBytes = 8 * 1024 * 1024;

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(maxInMemorySizeBytes))
            .build();

    return WebClient.builder()
        .baseUrl(props.getApiBaseUrl())
        .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
        .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
        .exchangeStrategies(strategies)
        .build();
  }
}
