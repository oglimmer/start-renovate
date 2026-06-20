/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * GitLab is wired in only when configured; an unset client id leaves GitHub as the sole provider.
 */
class ClientRegistrationConfigTest {

  private final ClientRegistrationConfig config = new ClientRegistrationConfig();

  private static OAuth2ClientProperties gitHubOnly() {
    OAuth2ClientProperties properties = new OAuth2ClientProperties();
    OAuth2ClientProperties.Registration github = new OAuth2ClientProperties.Registration();
    github.setClientId("gh-id");
    github.setClientSecret("gh-secret");
    github.setScope(Set.of("repo"));
    properties.getRegistration().put("github", github);
    return properties;
  }

  @Test
  void registersGitlabWhenClientIdPresent() {
    GitLabProperties gitlab = new GitLabProperties();
    gitlab.setClientId("gl-id");
    gitlab.setClientSecret("gl-secret");

    ClientRegistrationRepository repo = config.clientRegistrationRepository(gitHubOnly(), gitlab);

    assertThat(repo.findByRegistrationId("github")).isNotNull();
    assertThat(repo.findByRegistrationId("gitlab")).isNotNull();
    assertThat(repo.findByRegistrationId("gitlab").getScopes()).contains("read_api");
  }

  @Test
  void omitsGitlabWhenClientIdAbsent() {
    GitLabProperties gitlab = new GitLabProperties(); // clientId unset -> disabled

    ClientRegistrationRepository repo = config.clientRegistrationRepository(gitHubOnly(), gitlab);

    assertThat(repo.findByRegistrationId("github")).isNotNull();
    assertThat(repo.findByRegistrationId("gitlab")).isNull();
  }
}
