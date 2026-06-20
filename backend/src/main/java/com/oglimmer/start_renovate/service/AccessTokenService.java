/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Resolves the currently authenticated user's provider access token (GitHub or GitLab), refreshing
 * it first if it has expired. Must be called on the servlet request thread (the resolved token
 * string is then passed down into the reactive fan-out, which runs on threads that do not carry the
 * security context).
 */
@Service
@RequiredArgsConstructor
public class AccessTokenService {

  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public String currentAccessToken(OAuth2AuthenticationToken authentication) {
    OAuth2AuthorizeRequest request =
        OAuth2AuthorizeRequest.withClientRegistrationId(
                authentication.getAuthorizedClientRegistrationId())
            .principal(authentication)
            .build();
    OAuth2AuthorizedClient client = authorizedClientManager.authorize(request);
    if (client == null) {
      throw new IllegalStateException("No authorized client for the current user");
    }
    return client.getAccessToken().getTokenValue();
  }
}
