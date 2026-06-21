/* Copyright (c) 2025 by oglimmer.com / Oliver Zimpasser. All rights reserved. */
package com.oglimmer.start_renovate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.oglimmer.start_renovate.dto.CellState;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * Best-effort reverse-mapping of an arbitrary {@code renovate.json} onto this tool's option set —
 * the inverse of the frontend's {@code buildRenovateConfig}
 * (frontend/app/lib/generateRenovateConfig.ts).
 *
 * <p>The returned map keys are the FE/BE contract: they are exactly the dotted field paths of the
 * frontend {@code RenovateConfig} interface, so the dashboard can render them directly. Each value
 * is a {@link CellState} of SET_ON / SET_OFF / UNSET / CUSTOM. When a config is present but in a
 * shape we cannot confidently interpret, we return CUSTOM rather than guessing.
 */
@Service
public class RenovateConfigAnalyzer {

  /** Renovate manager groups, identical to the frontend {@code groupingMap}. */
  private static final Map<String, Set<String>> GROUPING =
      new LinkedHashMap<>() {
        {
          put("grouping.npm", Set.of("npm"));
          put("grouping.docker", Set.of("dockerfile", "docker-compose"));
          put("grouping.maven", Set.of("maven"));
          put("grouping.gradle", Set.of("gradle", "gradle-wrapper"));
          put("grouping.pip", Set.of("pip_requirements", "pip_setup", "pipenv"));
          put("grouping.composer", Set.of("composer"));
          put("grouping.helm", Set.of("helmv3", "helmfile"));
          put("grouping.githubActions", Set.of("github-actions"));
          put("grouping.terraform", Set.of("terraform", "terragrunt"));
          put("grouping.gomod", Set.of("gomod"));
          put("grouping.cargo", Set.of("cargo"));
          put("grouping.bundler", Set.of("bundler"));
          put("grouping.nuget", Set.of("nuget"));
        }
      };

  public Map<String, CellState> analyze(JsonNode root) {
    Map<String, CellState> out = new LinkedHashMap<>();
    if (root == null || !root.isObject()) {
      return out;
    }

    Set<String> ext = textSet(root.get("extends"));
    List<JsonNode> rules = arrayElements(root.get("packageRules"));
    // config:best-practices bundles the digest-pinning + abandonment presets, so treat its presence
    // as implying those options even when the specific preset string is absent.
    boolean bestPractices = ext.contains("config:best-practices");

    out.put("semanticCommits", semanticCommits(root, ext));
    out.put("timezone", stringOption(root, "timezone"));
    out.put("schedule", scheduleOption(root.get("schedule")));
    out.put("prLimitStrategy", prLimitStrategy(root));
    out.put("rebaseWhen", stringOption(root, "rebaseWhen"));
    out.put("rangeStrategy", stringOption(root, "rangeStrategy"));
    out.put("automergeType", stringOption(root, "automergeType"));
    out.put("automergeLevel", automergeLevel(root, rules));
    out.put("automergeDevDependencies", automergeDevDependencies(rules));
    out.put("ignoreTests", booleanFlag(root, "ignoreTests"));
    out.put("disablePreOneAutomerge", disablePreOneAutomerge(rules));
    out.put("requireMajorApproval", requireMajorApproval(rules));
    out.put("minimumReleaseAge", minimumReleaseAge(root));
    out.put(
        "flagAbandonedPackages",
        CellState.bool(bestPractices || ext.contains("abandonments:recommended")));

    out.put(
        "pinning.dockerDigests",
        CellState.bool(bestPractices || ext.contains("docker:pinDigests")));
    out.put(
        "pinning.githubActionDigests",
        CellState.bool(bestPractices || ext.contains("helpers:pinGitHubActionDigests")));
    out.put(
        "pinning.devDependencies",
        CellState.bool(bestPractices || ext.contains(":pinDevDependencies")));

    JsonNode lfm = root.get("lockFileMaintenance");
    boolean lfmEnabled = lfm != null && lfm.path("enabled").asBoolean(false);
    out.put("lockFileMaintenance.enabled", CellState.bool(lfmEnabled));
    out.put(
        "lockFileMaintenance.schedule",
        lfmEnabled ? scheduleOption(lfm.get("schedule")) : CellState.unset());
    out.put(
        "lockFileMaintenance.automerge",
        CellState.bool(lfm != null && lfm.path("automerge").asBoolean(false)));

    JsonNode va = root.get("vulnerabilityAlerts");
    out.put("vulnerabilityAlerts.labels", vulnLabels(va));
    out.put("vulnerabilityAlerts.scheduleOverride", vulnScheduleOverride(va));
    out.put(
        "vulnerabilityAlerts.automerge",
        CellState.bool(va != null && va.path("automerge").asBoolean(false)));

    out.put("groupAllNonMajor", groupAllNonMajor(rules));
    for (Map.Entry<String, Set<String>> group : GROUPING.entrySet()) {
      out.put(group.getKey(), CellState.bool(hasGrouping(rules, group.getValue())));
    }

    return out;
  }

  // --- option detectors ---------------------------------------------------

  private CellState semanticCommits(JsonNode root, Set<String> ext) {
    JsonNode sc = root.get("semanticCommits");
    if (sc != null && sc.isTextual()) {
      return switch (sc.asText()) {
        case "enabled" -> CellState.on();
        case "disabled" -> CellState.off();
        default -> CellState.custom(sc.asText());
      };
    }
    return CellState.bool(ext.contains(":semanticCommits"));
  }

  private CellState prLimitStrategy(JsonNode root) {
    Integer hourly = intOrNull(root, "prHourlyLimit");
    Integer concurrent = intOrNull(root, "prConcurrentLimit");
    if (hourly == null && concurrent == null) {
      return CellState.unset();
    }
    if (Integer.valueOf(1).equals(hourly) && Integer.valueOf(3).equals(concurrent)) {
      return CellState.on("conservative");
    }
    if (Integer.valueOf(10).equals(hourly) && Integer.valueOf(20).equals(concurrent)) {
      return CellState.on("active");
    }
    return CellState.custom("prHourlyLimit=" + hourly + ", prConcurrentLimit=" + concurrent);
  }

  private CellState automergeLevel(JsonNode root, List<JsonNode> rules) {
    JsonNode topAutomerge = root.get("automerge");
    if (topAutomerge != null && topAutomerge.asBoolean(false)) {
      return CellState.on("all");
    }
    boolean sawCustomAutomergeRule = false;
    for (JsonNode rule : rules) {
      if (!rule.path("automerge").asBoolean(false)) {
        continue;
      }
      Set<String> updateTypes = textSet(rule.get("matchUpdateTypes"));
      if (updateTypes.isEmpty()) {
        // automerge rule without update types (e.g. the devDependencies rule) — not a "level".
        continue;
      }
      if (updateTypes.contains("minor")) {
        return CellState.on("minor");
      }
      if (updateTypes.contains("patch")) {
        return CellState.on("patch");
      }
      sawCustomAutomergeRule = true;
    }
    return sawCustomAutomergeRule ? CellState.custom("automerge rule") : CellState.on("disabled");
  }

  private CellState automergeDevDependencies(List<JsonNode> rules) {
    for (JsonNode rule : rules) {
      if (rule.path("automerge").asBoolean(false)
          && textSet(rule.get("matchDepTypes")).contains("devDependencies")) {
        return CellState.on();
      }
    }
    return CellState.off();
  }

  private CellState disablePreOneAutomerge(List<JsonNode> rules) {
    for (JsonNode rule : rules) {
      if ("/^0\\./".equals(rule.path("matchCurrentVersion").asText(null))
          && rule.has("automerge")
          && !rule.path("automerge").asBoolean(true)) {
        return CellState.on();
      }
    }
    return CellState.off();
  }

  private CellState requireMajorApproval(List<JsonNode> rules) {
    for (JsonNode rule : rules) {
      if (textSet(rule.get("matchUpdateTypes")).contains("major")
          && rule.path("dependencyDashboardApproval").asBoolean(false)) {
        return CellState.on();
      }
    }
    return CellState.off();
  }

  private CellState minimumReleaseAge(JsonNode root) {
    JsonNode node = root.get("minimumReleaseAge");
    if (node == null || !node.isTextual()) {
      return CellState.unset();
    }
    return switch (node.asText()) {
      case "3 days" -> CellState.on("3-days");
      case "7 days" -> CellState.on("7-days");
      case "14 days" -> CellState.on("14-days");
      default -> CellState.custom(node.asText());
    };
  }

  private CellState vulnLabels(JsonNode va) {
    if (va == null) {
      return CellState.unset();
    }
    Set<String> labels = textSet(va.get("labels"));
    return labels.isEmpty() ? CellState.unset() : CellState.on(String.join(", ", labels));
  }

  private CellState vulnScheduleOverride(JsonNode va) {
    if (va == null) {
      return CellState.off();
    }
    JsonNode sched = va.get("schedule");
    if (sched == null) {
      return CellState.off();
    }
    List<String> values = textList(sched);
    if (values.equals(List.of("at any time"))) {
      return CellState.on();
    }
    return CellState.custom(String.join(", ", values));
  }

  /**
   * Detects the "fast lane" auto-group rule: a non-major-scoped group whose {@code groupName} uses
   * the {@code {{manager}}} template and is not pinned to specific managers — the inverse of {@code
   * buildRenovateConfig}'s {@code groupAllNonMajor} branch.
   */
  private CellState groupAllNonMajor(List<JsonNode> rules) {
    for (JsonNode rule : rules) {
      JsonNode groupName = rule.get("groupName");
      if (groupName != null
          && groupName.isTextual()
          && groupName.asText().contains("{{manager}}")
          && rule.get("matchManagers") == null
          && textSet(rule.get("matchUpdateTypes")).contains("minor")) {
        return CellState.on();
      }
    }
    return CellState.off();
  }

  private boolean hasGrouping(List<JsonNode> rules, Set<String> managers) {
    for (JsonNode rule : rules) {
      if (rule.hasNonNull("groupName") && textSet(rule.get("matchManagers")).equals(managers)) {
        return true;
      }
    }
    return false;
  }

  // --- generic helpers ----------------------------------------------------

  /** Reverse-maps a Renovate schedule array onto this tool's schedule enum. */
  private CellState scheduleOption(JsonNode node) {
    if (node == null) {
      return CellState.unset();
    }
    List<String> values = textList(node);
    String mapped =
        switch (values.toString()) {
          case "[before 5am on monday]" -> "weekly";
          case "[before 5am on the first day of the month]" -> "monthly";
          case "[every weekend]" -> "weekends";
          case "[after 6pm every weekday, before 9am every weekday, every weekend]" ->
              "outside-business-hours";
          case "[at any time]" -> "at-any-time";
          default -> null;
        };
    if (mapped == null) {
      return CellState.custom(String.join("; ", values));
    }
    return "at-any-time".equals(mapped) ? CellState.unset() : CellState.on(mapped);
  }

  private CellState stringOption(JsonNode root, String field) {
    JsonNode node = root.get(field);
    if (node == null || node.isNull()) {
      return CellState.unset();
    }
    return node.isTextual() ? CellState.on(node.asText()) : CellState.custom(node.toString());
  }

  private CellState booleanFlag(JsonNode root, String field) {
    JsonNode node = root.get(field);
    return CellState.bool(node != null && node.asBoolean(false));
  }

  private Integer intOrNull(JsonNode root, String field) {
    JsonNode node = root.get(field);
    if (node != null && node.isNumber()) {
      return node.asInt();
    }
    return null;
  }

  /** Collects the textual elements of an array (or a single textual node) into a set. */
  private Set<String> textSet(JsonNode node) {
    return new LinkedHashSet<>(textList(node));
  }

  private List<String> textList(JsonNode node) {
    List<String> values = new java.util.ArrayList<>();
    if (node == null) {
      return values;
    }
    if (node.isArray()) {
      for (JsonNode el : node) {
        if (el.isTextual()) {
          values.add(el.asText());
        }
      }
    } else if (node.isTextual()) {
      values.add(node.asText());
    }
    return values;
  }

  private List<JsonNode> arrayElements(JsonNode node) {
    List<JsonNode> elements = new java.util.ArrayList<>();
    if (node != null && node.isArray()) {
      node.forEach(elements::add);
    }
    return elements;
  }
}
