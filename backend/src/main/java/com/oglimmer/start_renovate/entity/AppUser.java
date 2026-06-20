/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * A user who has logged in through one of the supported OAuth providers. The primary key is a
 * surrogate id; a user's real identity is the {@code (provider, providerUserId)} pair, since the
 * provider's numeric user id is only unique within that provider.
 */
@Entity
@Table(
    name = "app_user",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_app_user_provider_user",
            columnNames = {"provider", "provider_user_id"}))
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** The OAuth provider this user authenticated with (e.g. {@code github}, {@code gitlab}). */
  @Column(nullable = false, length = 32)
  private String provider;

  /** The provider's own (numeric) user id, stable and unique within that provider. */
  @Column(name = "provider_user_id", nullable = false)
  private String providerUserId;

  @Column(nullable = false)
  private String login;

  private String name;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public AppUser(String provider, String providerUserId, String login, String name, String avatarUrl) {
    this.provider = provider;
    this.providerUserId = providerUserId;
    this.login = login;
    this.name = name;
    this.avatarUrl = avatarUrl;
  }
}
