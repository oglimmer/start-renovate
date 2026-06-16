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

/** A repository (owner/name) a user has chosen to track on the dashboard. */
@Entity
@Table(
    name = "enabled_repo",
    uniqueConstraints =
        @UniqueConstraint(
            name = "uq_enabled_repo_user_repo",
            columnNames = {"user_id", "repo_full_name"}))
@Getter
@Setter
@NoArgsConstructor
public class EnabledRepo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "repo_full_name", nullable = false)
  private String repoFullName;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public EnabledRepo(Long userId, String repoFullName) {
    this.userId = userId;
    this.repoFullName = repoFullName;
  }
}
