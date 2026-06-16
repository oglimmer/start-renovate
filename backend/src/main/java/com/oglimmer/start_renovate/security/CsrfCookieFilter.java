/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Forces the deferred {@link CsrfToken} to be resolved on every request, which in turn causes the
 * {@code CookieCsrfTokenRepository} to (re)issue the {@code XSRF-TOKEN} cookie. Without this, the
 * cookie is only written on the first request that actually accesses the token, so a freshly loaded
 * SPA would have no token to echo back on its first mutating call.
 */
public final class CsrfCookieFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
    if (csrfToken != null) {
      // Render the token, triggering the cookie to be written.
      csrfToken.getToken();
    }
    filterChain.doFilter(request, response);
  }
}
