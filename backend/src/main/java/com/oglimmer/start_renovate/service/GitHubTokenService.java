/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Resolves the currently authenticated user's GitHub access token from the stored {@link
 * OAuth2AuthorizedClient}. Must be called on the servlet request thread (where the {@link
 * SecurityContextHolder} is populated) — the resolved token string is then passed down into the
 * reactive fan-out, which runs on threads that do not carry the security context.
 *
 * <p>GitHub OAuth-App user tokens do not expire and have no refresh token, so using the stored
 * access token directly is safe long-term.
 */
@Service
@RequiredArgsConstructor
public class GitHubTokenService {

  private final OAuth2AuthorizedClientService authorizedClientService;

  public String currentAccessToken() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
      throw new IllegalStateException("No GitHub-authenticated user in the security context");
    }
    OAuth2AuthorizedClient client =
        authorizedClientService.loadAuthorizedClient(
            oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
    if (client == null) {
      throw new IllegalStateException("No authorized GitHub client for the current user");
    }
    return client.getAccessToken().getTokenValue();
  }
}
