/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.security;

import com.oglimmer.start_renovate.entity.AppUser;
import com.oglimmer.start_renovate.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

/**
 * Loads the GitHub profile on login and upserts the corresponding {@link AppUser} row, so the user
 * always exists in our database before any dashboard call. GitHub's provider defaults use the
 * numeric {@code id} as the principal name, which we reuse as the {@code app_user} primary key.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final AppUserRepository appUserRepository;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(userRequest);

    Object rawId = user.getAttribute("id");
    if (!(rawId instanceof Number number)) {
      throw new OAuth2AuthenticationException("GitHub user response is missing a numeric 'id'");
    }
    Long id = number.longValue();
    String login = user.getAttribute("login");
    String name = user.getAttribute("name");
    String avatarUrl = user.getAttribute("avatar_url");

    AppUser appUser = appUserRepository.findById(id).orElseGet(() -> new AppUser());
    appUser.setId(id);
    appUser.setLogin(login != null ? login : String.valueOf(id));
    appUser.setName(name);
    appUser.setAvatarUrl(avatarUrl);
    appUserRepository.save(appUser);

    return user;
  }
}
