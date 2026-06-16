/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class StartRenovateApplication {

  public static void main(String[] args) {
    // Pin the servlet (Spring MVC) stack explicitly. Both spring-boot-starter-webmvc and
    // spring-boot-starter-webflux are on the classpath (the latter only to use WebClient as an
    // outbound HTTP client); without pinning, a future dependency change could flip the
    // auto-detected WebApplicationType and silently swap Spring Security between its servlet and
    // reactive variants. The whole app — security, HttpSession, JPA — assumes the servlet stack.
    new SpringApplicationBuilder(StartRenovateApplication.class)
        .web(WebApplicationType.SERVLET)
        .run(args);
  }
}
