/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.oglimmer.start_renovate.entity.AppUser;
import com.oglimmer.start_renovate.repository.AppUserRepository;
import com.oglimmer.start_renovate.security.ProviderUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the local {@link AppUser} row for an authenticated OAuth user, keyed by the {@code
 * (provider, providerUserId)} business identity. Used both at login (to upsert the profile) and on
 * dashboard requests (to map the principal to our surrogate user id).
 */
@Service
@RequiredArgsConstructor
public class AppUserService {

  private final AppUserRepository appUserRepository;

  /** Upserts the profile from a fresh login, refreshing mutable fields (login, name, avatar). */
  @Transactional
  public AppUser upsert(ProviderUser user) {
    AppUser appUser =
        appUserRepository
            .findByProviderAndProviderUserId(user.provider(), user.providerUserId())
            .orElseGet(AppUser::new);
    appUser.setProvider(user.provider());
    appUser.setProviderUserId(user.providerUserId());
    appUser.setLogin(user.login());
    appUser.setName(user.name());
    appUser.setAvatarUrl(user.avatarUrl());
    return appUserRepository.save(appUser);
  }

  /**
   * Returns the surrogate id for the authenticated user, creating the row if it does not yet exist.
   * Login normally creates it first via {@link #upsert}; the create-if-missing path is a defensive
   * fallback (and keeps request handling independent of login ordering).
   */
  @Transactional
  public Long resolveId(ProviderUser user) {
    return appUserRepository
        .findByProviderAndProviderUserId(user.provider(), user.providerUserId())
        .map(AppUser::getId)
        .orElseGet(() -> upsert(user).getId());
  }
}
