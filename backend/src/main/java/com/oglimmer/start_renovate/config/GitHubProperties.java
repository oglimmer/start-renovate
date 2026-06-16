/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github")
@Getter
@Setter
public class GitHubProperties {

  /** Base URL of the GitHub REST API. */
  private String apiBaseUrl = "https://api.github.com";

  /**
   * Renovate config file paths to probe, in order, within a repository. The first one that exists
   * wins. See https://docs.renovatebot.com/configuration-options/ for the supported file names.
   */
  private List<String> configPaths =
      List.of(
          "renovate.json",
          ".github/renovate.json",
          ".renovaterc.json",
          ".renovaterc",
          "renovate.json5",
          ".github/renovate.json5");

  /** Fallback file whose "renovate" key may hold an embedded config. */
  private String packageJsonPath = "package.json";
}
