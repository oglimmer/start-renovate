/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.security;

import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * A normalised view of an authenticated OAuth user, flattening the per-provider attribute naming
 * (GitHub exposes {@code login}; GitLab exposes {@code username}) into one shape the rest of the
 * app can consume. The provider's numeric user id is used as {@code providerUserId}; both GitHub
 * and GitLab registrations are configured with {@code id} as the user-name attribute, so {@link
 * OAuth2User#getName()} yields it directly.
 */
public record ProviderUser(
    String provider, String providerUserId, String login, String name, String avatarUrl) {

  public static ProviderUser from(String provider, OAuth2User principal) {
    String providerUserId = principal.getName();
    String login =
        switch (provider) {
          case "gitlab" -> stringAttr(principal, "username");
          default -> stringAttr(principal, "login");
        };
    String name = stringAttr(principal, "name");
    String avatarUrl = stringAttr(principal, "avatar_url");
    return new ProviderUser(
        provider, providerUserId, login != null ? login : providerUserId, name, avatarUrl);
  }

  private static String stringAttr(OAuth2User principal, String key) {
    Object value = principal.getAttribute(key);
    return value != null ? value.toString() : null;
  }
}
