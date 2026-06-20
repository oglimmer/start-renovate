/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings for GitLab: both the OAuth login and the REST API. Defaults target gitlab.com; point the
 * URLs at a self-hosted instance to use one instead. GitLab login is enabled only when {@code
 * clientId} is set — this is what keeps the provider optional, since (unlike Spring's {@code
 * spring.security.oauth2.client.registration.*}) a blank value here is simply treated as "off"
 * rather than failing startup.
 */
@ConfigurationProperties(prefix = "gitlab")
@Getter
@Setter
public class GitLabProperties {

  // --- OAuth login -----------------------------------------------------------------------------

  /** OAuth application id. Blank disables GitLab login entirely. */
  private String clientId;

  /** OAuth application secret. */
  private String clientSecret;

  /** OAuth scope; {@code read_api} covers listing projects and reading repository files. */
  private String scope = "read_api";

  private String authorizationUri = "https://gitlab.com/oauth/authorize";
  private String tokenUri = "https://gitlab.com/oauth/token";
  private String userInfoUri = "https://gitlab.com/api/v4/user";

  // --- REST API --------------------------------------------------------------------------------

  /** Base URL of the GitLab REST API (v4). */
  private String apiBaseUrl = "https://gitlab.com/api/v4";

  /**
   * Base URL used to build human-facing links to a project ({@code <webBaseUrl>/group/sub/repo}).
   */
  private String webBaseUrl = "https://gitlab.com";

  public boolean isEnabled() {
    return clientId != null && !clientId.isBlank();
  }
}
