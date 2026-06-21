// Best-effort inverse of buildRenovateConfig (see generateRenovateConfig.ts):
// read an arbitrary renovate.json and map it back onto this tool's option set so
// the form can be pre-populated from an imported config.
//
// This is the FRONTEND twin of the backend's RenovateConfigAnalyzer.java; the
// magic strings it matches on (schedule cron text, manager names, PR-limit
// numbers, extends preset ids) are exactly what buildRenovateConfig emits. The
// round-trip test (parseRenovateConfig.spec.ts) pins the two together: any change
// to the strings buildRenovateConfig produces must keep build → parse stable.
import type { RenovateConfig } from './generateRenovateConfig'

// Parse renovate.json and map it back to our config structure
export const parseRenovateJson = (jsonString: string): Partial<RenovateConfig> => {
  const parsed = JSON.parse(jsonString)
  const result: Partial<RenovateConfig> = {}

  // Parse extends for semantic commits and supply-chain hardening presets
  if (parsed.extends && Array.isArray(parsed.extends)) {
    result.semanticCommits = parsed.extends.includes(':semanticCommits')
    result.pinning = {
      dockerDigests: parsed.extends.includes('docker:pinDigests'),
      githubActionDigests: parsed.extends.includes('helpers:pinGitHubActionDigests'),
      devDependencies: parsed.extends.includes(':pinDevDependencies')
    }
    result.flagAbandonedPackages = parsed.extends.includes('abandonments:recommended')
  }

  // Parse timezone
  if (parsed.timezone) {
    result.timezone = parsed.timezone
  }

  // Parse schedule
  if (parsed.schedule) {
    const scheduleStr = Array.isArray(parsed.schedule) ? parsed.schedule.join(', ') : parsed.schedule
    if (scheduleStr.includes('monday') && scheduleStr.includes('5am')) {
      result.schedule = 'weekly'
    } else if (scheduleStr.includes('first day of the month')) {
      result.schedule = 'monthly'
    } else if (scheduleStr.includes('weekend') && !scheduleStr.includes('weekday')) {
      result.schedule = 'weekends'
    } else if (scheduleStr.includes('6pm') || scheduleStr.includes('9am')) {
      result.schedule = 'outside-business-hours'
    } else {
      result.schedule = 'at-any-time'
    }
  }

  // Parse PR limits
  if (parsed.prHourlyLimit === 1 && parsed.prConcurrentLimit === 3) {
    result.prLimitStrategy = 'conservative'
  } else if (parsed.prHourlyLimit === 10 && parsed.prConcurrentLimit === 20) {
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

  // Parse minimumReleaseAge
  if (parsed.minimumReleaseAge) {
    const age = parsed.minimumReleaseAge.toLowerCase()
    if (age.includes('3')) {
      result.minimumReleaseAge = '3-days'
    } else if (age.includes('7') || age.includes('1 week')) {
      result.minimumReleaseAge = '7-days'
    } else if (age.includes('14') || age.includes('2 week')) {
      result.minimumReleaseAge = '14-days'
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
    // Initialize grouping
    result.grouping = {
      npm: false,
      docker: false,
      maven: false,
      gradle: false,
      pip: false,
      composer: false,
      helm: false,
      githubActions: false,
      terraform: false,
      gomod: false,
      cargo: false,
      bundler: false,
      nuget: false
    }

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

      // Check for grouping rules
      if (rule.matchManagers && rule.groupName) {
        const managers = rule.matchManagers
        if (managers.includes('npm')) result.grouping!.npm = true
        if (managers.includes('dockerfile') || managers.includes('docker-compose')) result.grouping!.docker = true
        if (managers.includes('maven')) result.grouping!.maven = true
        if (managers.includes('gradle') || managers.includes('gradle-wrapper')) result.grouping!.gradle = true
        if (managers.includes('pip_requirements') || managers.includes('pip_setup') || managers.includes('pipenv')) result.grouping!.pip = true
        if (managers.includes('composer')) result.grouping!.composer = true
        if (managers.includes('helmv3') || managers.includes('helmfile')) result.grouping!.helm = true
        if (managers.includes('github-actions')) result.grouping!.githubActions = true
        if (managers.includes('terraform') || managers.includes('terragrunt')) result.grouping!.terraform = true
        if (managers.includes('gomod')) result.grouping!.gomod = true
        if (managers.includes('cargo')) result.grouping!.cargo = true
        if (managers.includes('bundler')) result.grouping!.bundler = true
        if (managers.includes('nuget')) result.grouping!.nuget = true
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
      const scheduleStr = Array.isArray(parsed.lockFileMaintenance.schedule)
        ? parsed.lockFileMaintenance.schedule.join(', ')
        : parsed.lockFileMaintenance.schedule
      if (scheduleStr.includes('monday') && scheduleStr.includes('5am')) {
        result.lockFileMaintenance.schedule = 'weekly'
      } else if (scheduleStr.includes('first day of the month')) {
        result.lockFileMaintenance.schedule = 'monthly'
      } else if (scheduleStr.includes('weekend') && !scheduleStr.includes('weekday')) {
        result.lockFileMaintenance.schedule = 'weekends'
      } else if (scheduleStr.includes('6pm') || scheduleStr.includes('9am')) {
        result.lockFileMaintenance.schedule = 'outside-business-hours'
      }
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
