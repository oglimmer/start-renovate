/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
@Getter
@Setter
public class GitHubProperties {

  /** Base URL of the GitHub REST API. */
  private String apiBaseUrl = "https://api.github.com";

  /** Base URL used to build human-facing links to a repository ({@code <webBaseUrl>/owner/repo}). */
  private String webBaseUrl = "https://github.com";
}
