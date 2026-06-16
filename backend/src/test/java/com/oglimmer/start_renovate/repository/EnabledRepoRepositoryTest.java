/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.oglimmer.start_renovate.entity.EnabledRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EnabledRepoRepositoryTest {

  @Autowired private EnabledRepoRepository repository;

  @Test
  void enforcesUniqueUserRepoPair() {
    repository.saveAndFlush(new EnabledRepo(1L, "octocat/hello"));
    assertThatThrownBy(() -> repository.saveAndFlush(new EnabledRepo(1L, "octocat/hello")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void findsAndDeletesByUserAndRepo() {
    repository.save(new EnabledRepo(1L, "octocat/hello"));
    repository.save(new EnabledRepo(1L, "octocat/world"));
    repository.save(new EnabledRepo(2L, "other/repo"));

    assertThat(repository.findByUserId(1L)).hasSize(2);
    assertThat(repository.existsByUserIdAndRepoFullName(1L, "octocat/hello")).isTrue();
    assertThat(repository.existsByUserIdAndRepoFullName(1L, "missing/repo")).isFalse();

    assertThat(repository.deleteByUserIdAndRepoFullName(1L, "octocat/hello")).isEqualTo(1L);
    assertThat(repository.findByUserId(1L)).hasSize(1);
  }
}
