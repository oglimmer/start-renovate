/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.config;

import com.oglimmer.start_renovate.security.CsrfCookieFilter;
import com.oglimmer.start_renovate.security.CustomOAuth2UserService;
import com.oglimmer.start_renovate.security.SpaCsrfTokenRequestHandler;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Servlet-stack Spring Security for the GitHub-authenticated dashboard.
 *
 * <ul>
 *   <li>{@code POST /feedback} and health stay public (matchers are relative to the {@code /api}
 *       context-path, which the servlet strips before matching).
 *   <li>Everything else requires a GitHub-authenticated session.
 *   <li>Unauthenticated XHR (marked with {@code X-Requested-With: XMLHttpRequest}) gets a 401
 *       instead of a 302-to-GitHub, so the SPA can react; real browser navigations still redirect
 *       into the OAuth flow.
 *   <li>CSRF stays on (cookie + SPA header recipe) for the state-changing endpoints; the existing
 *       cross-origin, rate-limited {@code /feedback} call is exempted.
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService oAuth2UserService;

  @Value("${app.cors.allowed-origin}")
  private String allowedOrigin;

  @Value("${app.frontend-redirect-uri}")
  private String frontendRedirectUri;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/feedback")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/providers")
                    .permitAll()
                    .requestMatchers("/actuator/health", "/actuator/health/**")
                    .permitAll()
                    .requestMatchers("/oauth2/**", "/login/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .csrf(
            csrf -> {
              CookieCsrfTokenRepository tokenRepository =
                  CookieCsrfTokenRepository.withHttpOnlyFalse();
              // The backend runs under the /api context-path, so the cookie would default to
              // Path=/api and be invisible to document.cookie on the SPA (served from /). Scope it
              // to / so the dashboard JS can read the token and echo it in the X-XSRF-TOKEN header.
              tokenRepository.setCookiePath("/");
              csrf.csrfTokenRepository(tokenRepository)
                  .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                  // /feedback is called cross-origin and is rate-limited; keep it CSRF-exempt so
                  // its existing behaviour is unchanged. It only accepts POST.
                  .ignoringRequestMatchers("/feedback");
            })
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .oauth2Login(
            oauth ->
                oauth
                    .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                    // sendRedirect with a root-relative path is resolved against the host root (not
                    // the /api context-path), so the browser lands on the SPA, not /api/dashboard.
                    .successHandler(
                        (req, res, authentication) -> res.sendRedirect(frontendRedirectUri)))
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(
                        (req, res, authentication) ->
                            res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
        .exceptionHandling(
            ex ->
                ex.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest")));
    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of(allowedOrigin));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
