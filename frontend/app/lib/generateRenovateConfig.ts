export interface RenovateConfig {
  semanticCommits: boolean
  timezone: string
  schedule: 'at-any-time' | 'weekly' | 'monthly' | 'weekends' | 'outside-business-hours'
  prLimitStrategy: 'default' | 'conservative' | 'active'
  rebaseWhen: 'auto' | 'never' | 'conflicted' | 'behind-base-branch'
  rangeStrategy: 'auto' | 'pin' | 'bump' | 'replace' | 'widen' | 'update-lockfile'
  automergeType: 'pr' | 'branch' | 'pr-comment'
  automergeLevel: 'disabled' | 'patch' | 'minor' | 'all'
  automergeDevDependencies: boolean
  ignoreTests: boolean
  disablePreOneAutomerge: boolean
  minimumReleaseAge: 'never' | '3-days' | '7-days' | '14-days'
  lockFileMaintenance: {
    enabled: boolean
    schedule: 'at-any-time' | 'weekly' | 'monthly' | 'weekends' | 'outside-business-hours'
    automerge: boolean
  }
  vulnerabilityAlerts: {
    labels: string
    scheduleOverride: boolean
    automerge: boolean
  }
  grouping: {
    npm: boolean
    docker: boolean
    maven: boolean
    gradle: boolean
    pip: boolean
    composer: boolean
    helm: boolean
    githubActions: boolean
    terraform: boolean
    gomod: boolean
    cargo: boolean
    bundler: boolean
    nuget: boolean
  }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function buildRenovateConfig(config: RenovateConfig): Record<string, any> {
  const extends_array = [
    'config:recommended',
    ':enableVulnerabilityAlerts',
    ':dependencyDashboard'
  ]

  if (config.semanticCommits) {
    extends_array.push(':semanticCommits')
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const configObject: Record<string, any> = {
    $schema: 'https://docs.renovatebot.com/renovate-schema.json',
    extends: extends_array
  }

  if (config.timezone) {
    configObject.timezone = config.timezone
  }

  if (config.schedule !== 'at-any-time') {
    const scheduleMap: Record<string, string[]> = {
      'weekly': ['before 5am on monday'],
      'monthly': ['before 5am on the first day of the month'],
      'weekends': ['every weekend'],
      'outside-business-hours': ['after 6pm every weekday', 'before 9am every weekday', 'every weekend']
    }
    configObject.schedule = scheduleMap[config.schedule]
  }

  if (config.prLimitStrategy === 'conservative') {
    configObject.prHourlyLimit = 1
    configObject.prConcurrentLimit = 3
  } else if (config.prLimitStrategy === 'active') {
    configObject.prHourlyLimit = 10
    configObject.prConcurrentLimit = 20
  }

  if (config.rebaseWhen !== 'auto') {
    configObject.rebaseWhen = config.rebaseWhen
  }

  if (config.rangeStrategy !== 'auto') {
    configObject.rangeStrategy = config.rangeStrategy
  }

  if (config.automergeType !== 'pr') {
    configObject.automergeType = config.automergeType
  }

  if (config.automergeLevel === 'all') {
    configObject.automerge = true
  } else if (config.automergeLevel !== 'disabled' || config.automergeDevDependencies) {
    configObject.packageRules = configObject.packageRules || []

    if (config.automergeLevel !== 'disabled') {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const rule: Record<string, any> = {
        automerge: true
      }

      if (config.automergeLevel === 'patch') {
        rule.matchUpdateTypes = ['patch']
      } else if (config.automergeLevel === 'minor') {
        rule.matchUpdateTypes = ['minor', 'patch']
      }

      configObject.packageRules.push(rule)
    }

    if (config.automergeDevDependencies) {
      configObject.packageRules.push({
        matchDepTypes: ['devDependencies'],
        automerge: true
      })
      configObject.packageRules.push({
        matchManagers: ['maven-wrapper', 'gradle-wrapper'],
        automerge: true
      })
    }
  }

  if (config.disablePreOneAutomerge && config.automergeLevel !== 'disabled') {
    configObject.packageRules = configObject.packageRules || []
    configObject.packageRules.push({
      matchCurrentVersion: '/^0\\./',
      automerge: false
    })
  }

  if (config.ignoreTests) {
    configObject.ignoreTests = true
  }

  if (config.minimumReleaseAge !== 'never') {
    const ageMap: Record<string, string> = {
      '3-days': '3 days',
      '7-days': '7 days',
      '14-days': '14 days'
    }
    configObject.minimumReleaseAge = ageMap[config.minimumReleaseAge]
  }

  if (config.lockFileMaintenance.enabled) {
    configObject.lockFileMaintenance = {
      enabled: true
    }

    if (config.lockFileMaintenance.schedule !== 'at-any-time') {
      const lockFileScheduleMap: Record<string, string[]> = {
        'weekly': ['before 5am on monday'],
        'monthly': ['before 5am on the first day of the month'],
        'weekends': ['every weekend'],
        'outside-business-hours': ['after 6pm every weekday', 'before 9am every weekday', 'every weekend']
      }
      configObject.lockFileMaintenance.schedule = lockFileScheduleMap[config.lockFileMaintenance.schedule]
    } else {
      configObject.lockFileMaintenance.schedule = ['at any time']
    }

    if (config.lockFileMaintenance.automerge) {
      configObject.lockFileMaintenance.automerge = true
    }
  }

  // Always emit a vulnerabilityAlerts block with minimumReleaseAge: "0 days".
  // Defense in depth: even if no global minimumReleaseAge is configured today,
  // a later manual edit (or a future generator option) that adds one must not
  // silently delay security patches. `isVulnerabilityAlert` is NOT a valid
  // packageRules matcher and is silently ignored, so the top-level
  // vulnerabilityAlerts object is the only correct place for this override.
  configObject.vulnerabilityAlerts = {
    minimumReleaseAge: '0 days'
  }

  if (config.vulnerabilityAlerts.labels) {
    const labelsArray = config.vulnerabilityAlerts.labels
      .split(',')
      .map(label => label.trim())
      .filter(label => label.length > 0)

    if (labelsArray.length > 0) {
      configObject.vulnerabilityAlerts.labels = labelsArray
    }
  }

  if (config.vulnerabilityAlerts.scheduleOverride) {
    configObject.vulnerabilityAlerts.schedule = ['at any time']
  }

  if (config.vulnerabilityAlerts.automerge) {
    configObject.vulnerabilityAlerts.automerge = true
  }

  const groupingMap: Record<string, { managers: string[]; groupName: string }> = {
    npm: { managers: ['npm'], groupName: 'npm dependencies' },
    docker: { managers: ['dockerfile', 'docker-compose'], groupName: 'Docker dependencies' },
    maven: { managers: ['maven'], groupName: 'Maven dependencies' },
    gradle: { managers: ['gradle', 'gradle-wrapper'], groupName: 'Gradle dependencies' },
    pip: { managers: ['pip_requirements', 'pip_setup', 'pipenv'], groupName: 'pip dependencies' },
    composer: { managers: ['composer'], groupName: 'Composer dependencies' },
    helm: { managers: ['helmv3', 'helmfile'], groupName: 'Helm dependencies' },
    githubActions: { managers: ['github-actions'], groupName: 'GitHub Actions' },
    terraform: { managers: ['terraform', 'terragrunt'], groupName: 'Terraform dependencies' },
    gomod: { managers: ['gomod'], groupName: 'Go dependencies' },
    cargo: { managers: ['cargo'], groupName: 'Cargo dependencies' },
    bundler: { managers: ['bundler'], groupName: 'Bundler dependencies' },
    nuget: { managers: ['nuget'], groupName: 'NuGet dependencies' }
  }

  const enabledGroups = Object.entries(config.grouping)
    .filter(([_, enabled]) => enabled)
    .map(([key, _]) => key)

  if (enabledGroups.length > 0) {
    configObject.packageRules = configObject.packageRules || []

    enabledGroups.forEach(groupKey => {
      const group = groupingMap[groupKey as keyof typeof groupingMap]
      if (group) {
        configObject.packageRules.push({
          matchManagers: group.managers,
          groupName: group.groupName
        })
      }
    })
  }

  return configObject
}

export function generateRenovateConfigJson(config: RenovateConfig): string {
  return JSON.stringify(buildRenovateConfig(config), null, 2)
}
