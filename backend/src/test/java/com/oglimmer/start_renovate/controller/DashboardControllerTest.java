/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.oglimmer.start_renovate.repository.EnabledRepoRepository;
import com.oglimmer.start_renovate.service.GitHubApiService;
import com.oglimmer.start_renovate.service.GitHubTokenService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardControllerTest {

  private MockMvc mvc;

  @Autowired private WebApplicationContext context;
  @Autowired private EnabledRepoRepository enabledRepoRepository;

  // External calls are mocked; the security, JPA and OAuth2 wiring is real.
  @MockitoBean private GitHubTokenService tokenService;
  @MockitoBean private GitHubApiService gitHubApiService;

  private void setupMvc() {
    mvc =
        MockMvcBuilders.webAppContextSetup(context)
            .apply(
                org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
                    .springSecurity())
            .build();
  }

  private static OAuth2User ghUser(long id) {
    return new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), Map.of("id", id, "login", "octo"), "id");
  }

  @Test
  void anonymousXhrGetsUnauthorized() throws Exception {
    setupMvc();
    mvc.perform(get("/me").header("X-Requested-With", "XMLHttpRequest"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void putWithoutCsrfIsForbidden() throws Exception {
    setupMvc();
    mvc.perform(put("/repos/octo/hello/enabled").with(oauth2Login().oauth2User(ghUser(123))))
        .andExpect(status().isForbidden());
  }

  @Test
  void enableRepoPersistsSelection() throws Exception {
    setupMvc();
    mvc.perform(
            put("/repos/octo/hello/enabled")
                .with(oauth2Login().oauth2User(ghUser(123)))
                .with(csrf()))
        .andExpect(status().isNoContent());

    assertThat(enabledRepoRepository.existsByUserIdAndRepoFullName(123L, "octo/hello")).isTrue();
  }
}
