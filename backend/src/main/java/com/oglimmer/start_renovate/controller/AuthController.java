/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import com.oglimmer.start_renovate.dto.ProviderInfo;
import com.oglimmer.start_renovate.service.repo.RepoProviderRegistry;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for the login screen. {@link #providers()} lists the OAuth providers that are
 * actually configured (credentials present) AND backed by a {@code RepoProvider}, so the SPA only
 * offers login buttons that will work.
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

  private final ClientRegistrationRepository clientRegistrationRepository;
  private final RepoProviderRegistry providerRegistry;

  @GetMapping("/providers")
  public List<ProviderInfo> providers() {
    List<ProviderInfo> available = new ArrayList<>();
    // InMemoryClientRegistrationRepository is iterable; only it can enumerate registrations.
    if (clientRegistrationRepository
        instanceof InMemoryClientRegistrationRepository registrations) {
      for (ClientRegistration registration : registrations) {
        String id = registration.getRegistrationId();
        if (providerRegistry.isSupported(id)) {
          available.add(new ProviderInfo(id, registration.getClientName()));
        }
      }
    }
    return available;
  }
}
