/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Provider-independent settings for locating a repository's Renovate config. These file locations
 * are identical across hosting platforms, so both the GitHub and GitLab providers share them.
 */
@ConfigurationProperties(prefix = "renovate")
@Getter
@Setter
public class RenovateDetectionProperties {

  /**
   * Renovate config file paths to probe, in order, within a repository. The first one that exists
   * wins. See https://docs.renovatebot.com/configuration-options/ for the supported file names. The
   * {@code .gitlab/} variants only ever resolve on GitLab and the {@code .github/} variants on
   * GitHub, but probing all of them everywhere is harmless and keeps the list provider-agnostic.
   */
  private List<String> configPaths =
      List.of(
          "renovate.json",
          "renovate.json5",
          ".github/renovate.json",
          ".github/renovate.json5",
          ".gitlab/renovate.json",
          ".gitlab/renovate.json5",
          ".renovaterc.json",
          ".renovaterc");

  /** Fallback file whose "renovate" key may hold an embedded config. */
  private String packageJsonPath = "package.json";
}
