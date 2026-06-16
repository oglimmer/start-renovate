/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.repository;

import com.oglimmer.start_renovate.entity.EnabledRepo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface EnabledRepoRepository extends JpaRepository<EnabledRepo, Long> {

  List<EnabledRepo> findByUserId(Long userId);

  boolean existsByUserIdAndRepoFullName(Long userId, String repoFullName);

  @Transactional
  long deleteByUserIdAndRepoFullName(Long userId, String repoFullName);
}
