/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@EnableConfigurationProperties(DeepSeekProperties.class)
public class DeepSeekWebClientConfig {

  @Bean
  public WebClient deepSeekWebClient(DeepSeekProperties props) {
    int maxInMemorySizeBytes = 2 * 1024 * 1024;

    ExchangeStrategies strategies =
        ExchangeStrategies.builder()
            .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(maxInMemorySizeBytes))
            .build();

    // Bound the upstream LLM call so a hung/slow DeepSeek response fails fast instead of pinning
    // the
    // servlet async request for the full spring.mvc.async.request-timeout (120s). Mirrors the
    // GitHub/GitLab clients; the longer response timeout reflects normal LLM latency.
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMillis())
            .responseTimeout(Duration.ofSeconds(props.getResponseTimeoutSeconds()));

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .baseUrl(props.getBaseUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .exchangeStrategies(strategies)
        .build();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
