/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Builds the shared {@link WebClient} used to call the GitLab REST API. Mirrors {@link
 * GitHubWebClientConfig}: no default Authorization header, since the bearer token is per-user and
 * applied per request.
 */
@Configuration
@EnableConfigurationProperties(GitLabProperties.class)
public class GitLabWebClientConfig {

  @Bean
  public WebClient gitLabWebClient(GitLabProperties props) {
    // Project listings for users in many groups can be large; allow generous buffering.
    int maxInMemorySizeBytes = 8 * 1024 * 1024;

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(maxInMemorySizeBytes))
            .build();

    // Bound each upstream call so one hung connection fails fast instead of consuming the whole
    // request's async budget and stalling the fan-out.
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000)
            .responseTimeout(Duration.ofSeconds(15));

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl(props.getApiBaseUrl())
        .defaultHeader(HttpHeaders.ACCEPT, "application/json")
        .exchangeStrategies(strategies)
        .build();
  }
}
