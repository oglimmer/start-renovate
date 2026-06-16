/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/** A GitHub user who has logged in. The primary key is GitHub's numeric user id. */
@Entity
@Table(name = "app_user")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

  @Id private Long id;

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

  public AppUser(Long id, String login, String name, String avatarUrl) {
    this.id = id;
    this.login = login;
    this.name = name;
    this.avatarUrl = avatarUrl;
  }
}
