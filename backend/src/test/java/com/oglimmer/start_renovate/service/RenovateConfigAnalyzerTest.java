/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oglimmer.start_renovate.dto.CellState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RenovateConfigAnalyzerTest {

  private final RenovateConfigAnalyzer analyzer = new RenovateConfigAnalyzer();
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void reverseMapsTheDefaultGeneratedConfig() throws Exception {
    // Loads the SHARED corpus fixture generated from the frontend's
    // buildRenovateConfig(defaultRenovateConfig) — see
    // frontend/app/lib/renovateFixtures.spec.ts, which keeps the file in sync with
    // the generator. This is the strongest FE/BE-contract guard: both inverse
    // mappings are now exercised against the exact same input bytes.
    Map<String, CellState> r = analyzer.analyze(loadFixture("default-oglimmer"));

    assertCell(r, "semanticCommits", CellState.SET_ON, null);
    assertCell(r, "timezone", CellState.SET_ON, "Europe/Berlin");
    assertCell(r, "schedule", CellState.UNSET, null);
    assertCell(r, "prLimitStrategy", CellState.SET_ON, "active");
    assertCell(r, "rebaseWhen", CellState.SET_ON, "behind-base-branch");
    assertCell(r, "rangeStrategy", CellState.SET_ON, "bump");
    assertCell(r, "automergeType", CellState.SET_ON, "branch");
    assertCell(r, "automergeLevel", CellState.SET_ON, "minor");
    // The hardened default sets automergeDevDependencies: true, so the generated
    // config carries the devDependencies automerge rule. (The earlier hand-copied
    // snapshot omitted it and wrongly asserted SET_OFF — exactly the drift the
    // shared fixture eliminates.)
    assertCell(r, "automergeDevDependencies", CellState.SET_ON, null);
    assertCell(r, "ignoreTests", CellState.SET_OFF, null);
    assertCell(r, "disablePreOneAutomerge", CellState.SET_ON, null);
    assertCell(r, "requireMajorApproval", CellState.SET_ON, null);
    assertCell(r, "minimumReleaseAge", CellState.SET_ON, "7-days");
    assertCell(r, "flagAbandonedPackages", CellState.SET_ON, null);
    assertCell(r, "pinning.dockerDigests", CellState.SET_ON, null);
    assertCell(r, "pinning.githubActionDigests", CellState.SET_ON, null);
    assertCell(r, "pinning.devDependencies", CellState.SET_ON, null);
    assertCell(r, "lockFileMaintenance.enabled", CellState.SET_ON, null);
    assertCell(r, "lockFileMaintenance.schedule", CellState.SET_ON, "weekly");
    assertCell(r, "lockFileMaintenance.automerge", CellState.SET_ON, null);
    assertCell(r, "vulnerabilityAlerts.labels", CellState.SET_ON, "security");
    assertCell(r, "vulnerabilityAlerts.scheduleOverride", CellState.SET_ON, null);
    assertCell(r, "vulnerabilityAlerts.automerge", CellState.SET_ON, null);
    // fast-lane auto-group on; per-manager toggles all off (the {{manager}} rule
    // has no matchManagers, so it must not false-trigger any specific manager).
    assertCell(r, "groupAllNonMajor", CellState.SET_ON, null);
    assertCell(r, "grouping.npm", CellState.SET_OFF, null);
    assertCell(r, "grouping.docker", CellState.SET_OFF, null);
    assertCell(r, "grouping.nuget", CellState.SET_OFF, null);
  }

  @Test
  void emptyConfigYieldsDefaultsAndOffs() throws Exception {
    Map<String, CellState> r = analyzer.analyze(mapper.readTree("{}"));

    assertCell(r, "semanticCommits", CellState.SET_OFF, null);
    assertCell(r, "timezone", CellState.UNSET, null);
    assertCell(r, "schedule", CellState.UNSET, null);
    assertCell(r, "prLimitStrategy", CellState.UNSET, null);
    assertCell(r, "rebaseWhen", CellState.UNSET, null);
    assertCell(r, "automergeLevel", CellState.SET_ON, "disabled");
    assertCell(r, "minimumReleaseAge", CellState.UNSET, null);
    assertCell(r, "flagAbandonedPackages", CellState.SET_OFF, null);
    assertCell(r, "pinning.dockerDigests", CellState.SET_OFF, null);
    assertCell(r, "lockFileMaintenance.enabled", CellState.SET_OFF, null);
    assertCell(r, "lockFileMaintenance.schedule", CellState.UNSET, null);
    assertCell(r, "vulnerabilityAlerts.labels", CellState.UNSET, null);
    assertCell(r, "vulnerabilityAlerts.scheduleOverride", CellState.SET_OFF, null);
    assertCell(r, "grouping.maven", CellState.SET_OFF, null);
  }

  @Test
  void bestPracticesImpliesPinningAndAbandonments() throws Exception {
    String json = "{ \"extends\": [\"config:best-practices\"] }";
    Map<String, CellState> r = analyzer.analyze(mapper.readTree(json));

    assertCell(r, "pinning.dockerDigests", CellState.SET_ON, null);
    assertCell(r, "pinning.githubActionDigests", CellState.SET_ON, null);
    assertCell(r, "pinning.devDependencies", CellState.SET_ON, null);
    assertCell(r, "flagAbandonedPackages", CellState.SET_ON, null);
    // best-practices does not imply semantic commits
    assertCell(r, "semanticCommits", CellState.SET_OFF, null);
  }

  @Test
  void unrecognizedShapesAreCustom() throws Exception {
    String json =
        """
        {
          "schedule": ["after 10pm every weekday"],
          "prHourlyLimit": 5,
          "minimumReleaseAge": "30 days",
          "semanticCommits": "enabled"
        }
        """;
    Map<String, CellState> r = analyzer.analyze(mapper.readTree(json));

    assertCell(r, "schedule", CellState.CUSTOM, "after 10pm every weekday");
    assertEquals(CellState.CUSTOM, r.get("prLimitStrategy").state());
    assertCell(r, "minimumReleaseAge", CellState.CUSTOM, "30 days");
    assertCell(r, "semanticCommits", CellState.SET_ON, null);
  }

  @Test
  void detectsGroupingFromMatchManagers() throws Exception {
    String json =
        """
        {
          "packageRules": [
            { "matchManagers": ["npm"], "groupName": "npm dependencies" },
            { "matchManagers": ["gradle", "gradle-wrapper"], "groupName": "Gradle dependencies" }
          ]
        }
        """;
    Map<String, CellState> r = analyzer.analyze(mapper.readTree(json));

    assertCell(r, "grouping.npm", CellState.SET_ON, null);
    assertCell(r, "grouping.gradle", CellState.SET_ON, null);
    assertCell(r, "grouping.docker", CellState.SET_OFF, null);
    // manager-pinned group rules must not be read as the {{manager}} auto-group.
    assertCell(r, "groupAllNonMajor", CellState.SET_OFF, null);
  }

  @Test
  void detectsGroupAllNonMajorFromManagerTemplate() throws Exception {
    String json =
        """
        {
          "packageRules": [
            { "matchUpdateTypes": ["minor", "patch", "pin", "digest"], "groupName": "{{manager}} non-major dependencies" }
          ]
        }
        """;
    Map<String, CellState> r = analyzer.analyze(mapper.readTree(json));

    assertCell(r, "groupAllNonMajor", CellState.SET_ON, null);
    // it is not pinned to any manager, so no per-manager cell should light up.
    assertCell(r, "grouping.npm", CellState.SET_OFF, null);
  }

  @Test
  void reverseMapsThePerManagerGroupingFixture() throws Exception {
    // Shared corpus fixture: groupAllNonMajor off, with npm/docker/maven/githubActions
    // grouped per-manager. Exercises the analyzer's manager detection against the
    // exact rules the frontend emits for that combination.
    Map<String, CellState> r = analyzer.analyze(loadFixture("per-manager-grouping"));

    assertCell(r, "groupAllNonMajor", CellState.SET_OFF, null);
    assertCell(r, "grouping.npm", CellState.SET_ON, null);
    assertCell(r, "grouping.docker", CellState.SET_ON, null);
    assertCell(r, "grouping.maven", CellState.SET_ON, null);
    assertCell(r, "grouping.githubActions", CellState.SET_ON, null);
    assertCell(r, "grouping.gradle", CellState.SET_OFF, null);
    assertCell(r, "grouping.nuget", CellState.SET_OFF, null);
  }

  private static void assertCell(
      Map<String, CellState> result, String key, String expectedState, String expectedValue) {
    CellState cell = result.get(key);
    assertEquals(expectedState, cell == null ? null : cell.state(), "state for " + key);
    assertEquals(expectedValue, cell == null ? null : cell.value(), "value for " + key);
  }

  /** Reads a document from the shared cross-language corpus at {@code <repo>/fixtures/renovate}. */
  private JsonNode loadFixture(String name) throws IOException {
    return mapper.readTree(fixtureDir().resolve(name + ".json").toFile());
  }

  /**
   * Resolves the repo-root {@code fixtures/renovate} dir by walking up from the working directory,
   * so the test passes whether run from the {@code backend} module or the repository root.
   */
  private static Path fixtureDir() {
    Path p = Path.of("").toAbsolutePath();
    for (int i = 0; i < 6 && p != null; i++, p = p.getParent()) {
      Path candidate = p.resolve("fixtures").resolve("renovate");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException(
        "shared fixtures/renovate dir not found from " + Path.of("").toAbsolutePath());
  }
}
