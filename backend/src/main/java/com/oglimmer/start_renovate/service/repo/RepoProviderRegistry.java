/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service.repo;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Routes an OAuth registration id to the matching {@link RepoProvider}. */
@Component
public class RepoProviderRegistry {

  private final Map<String, RepoProvider> byId;

  public RepoProviderRegistry(List<RepoProvider> providers) {
    this.byId = providers.stream().collect(Collectors.toMap(RepoProvider::id, Function.identity()));
  }

  public boolean isSupported(String providerId) {
    return byId.containsKey(providerId);
  }

  public RepoProvider get(String providerId) {
    RepoProvider provider = byId.get(providerId);
    if (provider == null) {
      throw new IllegalStateException("Unsupported repo provider: " + providerId);
    }
    return provider;
  }
}
