/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

/**
 * Assembles the {@link ClientRegistrationRepository}. GitHub is configured the standard Spring Boot
 * way (under {@code spring.security.oauth2.client}); GitLab is added programmatically from {@link
 * GitLabProperties} and only when it is enabled (client id present). Building GitLab outside the
 * {@code OAuth2ClientProperties} binding is deliberate: that binding rejects a blank client id, so
 * the only way to make GitLab truly optional — present when configured, absent otherwise, without a
 * profile switch — is to construct its registration by hand.
 *
 * <p>Replaces Spring Boot's auto-configured repository (which is
 * {@code @ConditionalOnMissingBean}).
 */
@Configuration
@EnableConfigurationProperties({OAuth2ClientProperties.class, GitLabProperties.class})
public class ClientRegistrationConfig {

  @Bean
  public ClientRegistrationRepository clientRegistrationRepository(
      OAuth2ClientProperties oauthProperties, GitLabProperties gitLabProperties) {
    List<ClientRegistration> registrations =
        new ArrayList<>(
            new OAuth2ClientPropertiesMapper(oauthProperties).asClientRegistrations().values());
    if (gitLabProperties.isEnabled()) {
      registrations.add(gitLabRegistration(gitLabProperties));
    }
    return new InMemoryClientRegistrationRepository(registrations);
  }

  private ClientRegistration gitLabRegistration(GitLabProperties props) {
    return ClientRegistration.withRegistrationId("gitlab")
        .clientName("GitLab")
        .clientId(props.getClientId())
        .clientSecret(props.getClientSecret())
        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
        .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
        .scope(props.getScope().split(","))
        .authorizationUri(props.getAuthorizationUri())
        .tokenUri(props.getTokenUri())
        .userInfoUri(props.getUserInfoUri())
        .userNameAttributeName("id")
        .build();
  }
}
