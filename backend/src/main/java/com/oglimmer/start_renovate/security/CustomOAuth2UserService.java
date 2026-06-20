/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.security;

import com.oglimmer.start_renovate.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Loads the provider profile on login and upserts the corresponding {@code AppUser} row, so the
 * user always exists in our database before any dashboard call. Works for any configured provider
 * (GitHub, GitLab, ...): the registration id selects how the profile attributes are interpreted
 * (see {@link ProviderUser}). Both providers are configured with {@code id} as the user-name
 * attribute, so {@link OAuth2User#getName()} yields the provider's numeric user id.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final AppUserService appUserService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();
    if (user.getName() == null) {
      throw new OAuth2AuthenticationException(
          provider + " user response is missing a user id attribute");
    }
    appUserService.upsert(ProviderUser.from(provider, user));

    return user;
  }
}
