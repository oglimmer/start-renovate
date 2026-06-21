// Best-effort inverse of buildRenovateConfig (see generateRenovateConfig.ts):
// read an arbitrary renovate.json and map it back onto this tool's option set so
// the form can be pre-populated from an imported config.
//
// This is the FRONTEND twin of the backend's RenovateConfigAnalyzer.java; the
// magic strings it matches on (schedule cron text, manager names, PR-limit
// numbers, extends preset ids) are exactly what buildRenovateConfig emits. The
// round-trip test (parseRenovateConfig.spec.ts) pins the two together: any change
// to the strings buildRenovateConfig produces must keep build → parse stable.
import {
  EXTENDS_PRESETS,
  GROUPING_MANAGERS,
  PR_LIMITS,
  releaseAgePresetFromValue,
  schedulePresetFromValue,
  type RenovateConfig
} from './generateRenovateConfig'

// Parse renovate.json and map it back to our config structure
export const parseRenovateJson = (jsonString: string): Partial<RenovateConfig> => {
  const parsed = JSON.parse(jsonString)
  const result: Partial<RenovateConfig> = {}

  // Parse extends for semantic commits and supply-chain hardening presets
  if (parsed.extends && Array.isArray(parsed.extends)) {
    result.semanticCommits = parsed.extends.includes(EXTENDS_PRESETS.semanticCommits)
    result.pinning = {
      dockerDigests: parsed.extends.includes(EXTENDS_PRESETS.dockerDigests),
      githubActionDigests: parsed.extends.includes(EXTENDS_PRESETS.githubActionDigests),
      devDependencies: parsed.extends.includes(EXTENDS_PRESETS.pinDevDependencies)
    }
    result.flagAbandonedPackages = parsed.extends.includes(EXTENDS_PRESETS.flagAbandonedPackages)
  }

  // Parse timezone
  if (parsed.timezone) {
    result.timezone = parsed.timezone
  }

  // Parse schedule (exact-match-then-fuzzy, shared with the forward generator).
  if (parsed.schedule) {
    result.schedule = schedulePresetFromValue(parsed.schedule) ?? 'at-any-time'
  }

  // Parse PR limits
  if (parsed.prHourlyLimit === PR_LIMITS.conservative.prHourlyLimit && parsed.prConcurrentLimit === PR_LIMITS.conservative.prConcurrentLimit) {
    result.prLimitStrategy = 'conservative'
  } else if (parsed.prHourlyLimit === PR_LIMITS.active.prHourlyLimit && parsed.prConcurrentLimit === PR_LIMITS.active.prConcurrentLimit) {
    result.prLimitStrategy = 'active'
  } else if (parsed.prHourlyLimit !== undefined || parsed.prConcurrentLimit !== undefined) {
    // Custom values - pick closest match
    if ((parsed.prHourlyLimit || 2) <= 2 && (parsed.prConcurrentLimit || 10) <= 5) {
      result.prLimitStrategy = 'conservative'
    } else if ((parsed.prHourlyLimit || 2) >= 5) {
      result.prLimitStrategy = 'active'
    } else {
      result.prLimitStrategy = 'default'
    }
  }

  // Parse rebaseWhen
  if (parsed.rebaseWhen) {
    result.rebaseWhen = parsed.rebaseWhen
  }

  // Parse rangeStrategy
  if (parsed.rangeStrategy) {
    result.rangeStrategy = parsed.rangeStrategy
  }

  // Parse automergeType
  if (parsed.automergeType) {
    result.automergeType = parsed.automergeType
  }

  // Parse ignoreTests
  if (parsed.ignoreTests) {
    result.ignoreTests = parsed.ignoreTests
  }

  // Parse minimumReleaseAge (exact-match-then-fuzzy, shared with the forward generator).
  if (parsed.minimumReleaseAge) {
    const age = releaseAgePresetFromValue(parsed.minimumReleaseAge)
    if (age) {
      result.minimumReleaseAge = age
    }
  }

  // Parse automerge settings
  if (parsed.automerge === true) {
    result.automergeLevel = 'all'
  }

  // Parse packageRules for automerge settings and grouping
  if (parsed.packageRules && Array.isArray(parsed.packageRules)) {
    // Default these off; the loop below flips them on if a matching rule exists.
    result.requireMajorApproval = false
    // Initialize grouping from the shared manager map (every key off).
    result.grouping = Object.fromEntries(
      Object.keys(GROUPING_MANAGERS).map(key => [key, false])
    ) as RenovateConfig['grouping']

    for (const rule of parsed.packageRules) {
      // Check for automerge rules
      if (rule.automerge === true) {
        if (rule.matchUpdateTypes) {
          const types = rule.matchUpdateTypes
          if (types.includes('minor') && types.includes('patch')) {
            result.automergeLevel = 'minor'
          } else if (types.includes('patch') && !types.includes('minor')) {
            result.automergeLevel = 'patch'
          }
        }
        if (rule.matchDepTypes && rule.matchDepTypes.includes('devDependencies')) {
          result.automergeDevDependencies = true
        }
      }

      // Check for pre-1.0.0 disable rule
      if (rule.matchCurrentVersion && rule.matchCurrentVersion.includes('^0\\.') && rule.automerge === false) {
        result.disablePreOneAutomerge = true
      }

      // Check for major-update dashboard-approval gate
      if (rule.matchUpdateTypes && rule.matchUpdateTypes.includes('major') && rule.dependencyDashboardApproval === true) {
        result.requireMajorApproval = true
      }

      // Check for grouping rules: a manager group is "on" if the rule lists any of
      // its managers (shared GROUPING_MANAGERS map — the same one the forward
      // generator emits from, so detection can't drift from emission).
      if (rule.matchManagers && rule.groupName) {
        const managers: string[] = rule.matchManagers
        for (const [key, group] of Object.entries(GROUPING_MANAGERS)) {
          if (group.managers.some(m => managers.includes(m))) {
            result.grouping![key as keyof RenovateConfig['grouping']] = true
          }
        }
      }
    }
  }

  // Parse lockFileMaintenance
  if (parsed.lockFileMaintenance) {
    result.lockFileMaintenance = {
      enabled: parsed.lockFileMaintenance.enabled !== false,
      schedule: 'at-any-time',
      automerge: parsed.lockFileMaintenance.automerge === true
    }

    if (parsed.lockFileMaintenance.schedule) {
      // Same shared schedule vocabulary as the top-level schedule; default stays
      // 'at-any-time' when nothing recognizable matches.
      result.lockFileMaintenance.schedule =
        schedulePresetFromValue(parsed.lockFileMaintenance.schedule) ?? 'at-any-time'
    }
  }

  // Parse vulnerabilityAlerts
  if (parsed.vulnerabilityAlerts) {
    result.vulnerabilityAlerts = {
      labels: '',
      scheduleOverride: false,
      automerge: false
    }

    if (parsed.vulnerabilityAlerts.labels && Array.isArray(parsed.vulnerabilityAlerts.labels)) {
      result.vulnerabilityAlerts.labels = parsed.vulnerabilityAlerts.labels.join(', ')
    }

    if (parsed.vulnerabilityAlerts.schedule) {
      const scheduleStr = Array.isArray(parsed.vulnerabilityAlerts.schedule)
        ? parsed.vulnerabilityAlerts.schedule.join(', ')
        : parsed.vulnerabilityAlerts.schedule
      if (scheduleStr.includes('any time')) {
        result.vulnerabilityAlerts.scheduleOverride = true
      }
    }

    if (parsed.vulnerabilityAlerts.automerge === true) {
      result.vulnerabilityAlerts.automerge = true
    }
  }

  return result
}
