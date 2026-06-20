/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

/** Both providers are configured in application-test.yaml, so both are offered (and public). */
@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

  @Autowired private WebApplicationContext context;

  @Test
  void providersEndpointListsBothConfiguredProvidersWithoutAuth() throws Exception {
    MockMvc mvc = webAppContextSetup(context).build();
    mvc.perform(MockMvcRequestBuilders.get("/providers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == 'github')]").exists())
        .andExpect(jsonPath("$[?(@.id == 'gitlab')]").exists());
  }
}
