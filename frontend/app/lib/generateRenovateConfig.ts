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
  requireMajorApproval: boolean
  minimumReleaseAge: 'never' | '3-days' | '7-days' | '14-days'
  pinning: {
    dockerDigests: boolean
    githubActionDigests: boolean
    devDependencies: boolean
  }
  flagAbandonedPackages: boolean
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
  // "Fast lane" auto-grouping: emit a single {{manager}} template rule that groups
  // every detected manager's non-major updates into one PR per manager — no need to
  // enumerate managers below. Supersedes the per-manager `grouping` toggles when on.
  groupAllNonMajor: boolean
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

// The canonical default configuration. This is the single source of truth for
// the initial form state (see pages/index.vue) AND the baseline that the
// query-param "pseudo API" (see pages/generate.vue) merges overrides onto.
export const defaultRenovateConfig: RenovateConfig = {
  semanticCommits: true,
  timezone: 'Europe/Berlin',
  schedule: 'at-any-time',
  prLimitStrategy: 'active',
  rebaseWhen: 'behind-base-branch',
  rangeStrategy: 'bump',
  // 'branch' keeps the git history clean: Renovate commits automerged updates
  // straight to the base branch with no PR/merge-commit trace. This is a
  // deliberate default — see the platformAutomerge note in buildRenovateConfig.
  automergeType: 'branch',
  automergeLevel: 'minor',
  // devDependencies (build tools, linters, test frameworks) aren't shipped to
  // consumers, so automerging all of them — regardless of update type — is a
  // safe way to cut review noise. On by default in the hardened profile.
  automergeDevDependencies: true,
  ignoreTests: false,
  disablePreOneAutomerge: true,
  // Gate major updates behind a Dependency Dashboard checkbox: no PR is created
  // until a human opts in. Majors are never automerged anyway, so this just
  // keeps unrequested major PRs from piling up (guide §8/§9). On by default,
  // consistent with the hardened, low-noise posture.
  requireMajorApproval: true,
  // Default to a stabilization window rather than 'never'. The default profile
  // automerges minor+patch silently onto the base branch, so adopting a release
  // the instant it is published would commit unvetted third-party code to the
  // mainline with no chance for the registry to pull a yanked/compromised
  // version. 7 days is the minimum the Renovate maintainers endorse for
  // automerged deps; users can dial this to 'never' or '14-days' in the form.
  minimumReleaseAge: '7-days',
  // Supply-chain hardening: pin mutable Docker tags / GitHub Action refs to
  // immutable digests/SHAs (the two pinning presets from config:best-practices).
  // On by default; Renovate keeps the pinned digests updated, and under the
  // branch-automerge default those bumps land cleanly with no PR trail.
  pinning: {
    dockerDigests: true,
    githubActionDigests: true,
    // :pinDevDependencies — pin devDependencies (build tools, linters, test
    // frameworks) to exact versions for reproducible builds. Safe to default on
    // even for published libraries: devDependencies aren't installed by your
    // consumers, so this never over-constrains them (guide §7).
    devDependencies: true
  },
  // abandonments:recommended — flag dependencies that are no longer maintained
  // and surface suggested replacements. Part of config:best-practices.
  flagAbandonedPackages: true,
  lockFileMaintenance: {
    enabled: true,
    // Weekly cadence mirrors config:best-practices' :maintainLockFilesWeekly —
    // a full lockfile refresh once a week rather than continuously. Keeps churn
    // predictable; automerge + branch automerge keep these off the PR/history trail.
    schedule: 'weekly',
    automerge: true
  },
  vulnerabilityAlerts: {
    labels: 'security',
    scheduleOverride: true,
    automerge: true
  },
  // On by default in the hardened profile: one {{manager}} group rule collapses
  // every manager's minor/patch churn into a single automergeable PR per manager,
  // matching the low-noise posture. Majors stay individual under requireMajorApproval.
  groupAllNonMajor: true,
  grouping: {
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
}

// Neutral baseline: every opinionated knob parked at its "let Renovate decide"
// position. This is the closest the form can get to plain Renovate defaults —
// `config:recommended`, `:enableVulnerabilityAlerts` and `:dependencyDashboard`
// are always emitted by buildRenovateConfig (they are not form-configurable), so
// this preset just turns OFF every extra opinion the tool layers on top:
// no forced semantic commits, no pinning, no automerge, no release-age delay,
// no lock-file maintenance, no grouping.
export const renovateDefaultsConfig: RenovateConfig = {
  semanticCommits: false,
  timezone: '',
  schedule: 'at-any-time',
  prLimitStrategy: 'default',
  rebaseWhen: 'auto',
  rangeStrategy: 'auto',
  automergeType: 'pr',
  automergeLevel: 'disabled',
  automergeDevDependencies: false,
  ignoreTests: false,
  disablePreOneAutomerge: false,
  requireMajorApproval: false,
  minimumReleaseAge: 'never',
  pinning: {
    dockerDigests: false,
    githubActionDigests: false,
    devDependencies: false
  },
  flagAbandonedPackages: false,
  lockFileMaintenance: {
    enabled: false,
    schedule: 'at-any-time',
    automerge: false
  },
  vulnerabilityAlerts: {
    labels: '',
    scheduleOverride: false,
    automerge: false
  },
  groupAllNonMajor: false,
  grouping: {
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
}

// Selectable starting points for the form. Each preset is a full RenovateConfig
// the user can load and then tweak. `oglimmer` is the opinionated, hardened
// default (== defaultRenovateConfig, the single source of truth shared with the
// /generate pseudo-API); `renovate-defaults` is the neutral baseline above.
export interface ConfigPreset {
  id: string
  label: string
  description: string
  config: RenovateConfig
}

export const configPresets: ConfigPreset[] = [
  {
    id: 'renovate-defaults',
    label: 'Renovate defaults',
    description:
      'A minimal, neutral baseline — config:recommended plus the dependency dashboard and ' +
      'vulnerability alerts, with every extra opinion (pinning, automerge, schedules, grouping) left off.',
    config: renovateDefaultsConfig
  },
  {
    id: 'oglimmer',
    label: 'oglimmer (hardened defaults)',
    description:
      'Opinionated, security-hardened profile: digest/SHA pinning, 7-day release-age window, ' +
      'branch automerge for a clean git history, weekly lock-file maintenance and major-update gating.',
    config: defaultRenovateConfig
  }
]

// The preset the form starts on. Kept separate from defaultRenovateConfig (the
// /generate pseudo-API baseline) — that stays the opinionated profile, while the
// interactive form opens on the neutral Renovate defaults.
export const defaultPresetId = 'renovate-defaults'

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

  // Digest/SHA pinning presets — supply-chain hardening (see RenovateConfig.pinning).
  if (config.pinning.dockerDigests) {
    extends_array.push('docker:pinDigests')
  }
  if (config.pinning.githubActionDigests) {
    extends_array.push('helpers:pinGitHubActionDigests')
  }
  if (config.pinning.devDependencies) {
    extends_array.push(':pinDevDependencies')
  }

  // Flag abandoned/unmaintained dependencies (config:best-practices component).
  if (config.flagAbandonedPackages) {
    extends_array.push('abandonments:recommended')
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const configObject: Record<string, any> = {
    $schema: 'https://docs.renovatebot.com/renovate-schema.json',
    extends: extends_array,
    // Keep deprecated/renamed options auto-migrating via a Renovate-raised PR
    // instead of silently breaking. Recommended best practice to always enable.
    configMigration: true
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

  // === packageRules are emitted in a deliberate top-to-bottom order so the file
  // reads like a lesson: (1) WHO gets grouped, (2) WHAT automerges, (3) automerge
  // SAFETY blocks, (4) the major slow-lane gate. Renovate merges EVERY matching
  // rule (a later rule wins only on a shared key), so order is significant solely
  // between rules touching the same key — the 0.x `automerge:false` block (3) must
  // stay AFTER the automerge-enable rule (2). Grouping, automerge and the major
  // gate use different keys, so their relative order is purely for readability.
  // Each rule carries a `description`, which Renovate surfaces in the PR/branch.

  // (1) Fast lane — group non-major updates, one PR per manager. Scoped to
  // non-major so a major never inherits a groupName and folds back into the group
  // PR; it must reach the slow-lane gate (4) on its own instead. Grouping is
  // orthogonal to automerge, so a grouped non-major still automerges via rule (2).
  const nonMajorUpdateTypes = ['minor', 'patch', 'pin', 'digest']

  if (config.groupAllNonMajor) {
    // The {{manager}} template expands to a separate group per manager Renovate
    // detects (npm, maven, docker, …) with no enumeration, so a monorepo's
    // frontend (npm) and backend (maven) deps stay in distinct PRs. Supersedes
    // the explicit per-manager toggles.
    configObject.packageRules = configObject.packageRules || []
    configObject.packageRules.push({
      description: "Group each manager's non-major updates into one PR per manager",
      matchUpdateTypes: nonMajorUpdateTypes,
      groupName: '{{manager}} non-major dependencies'
    })
  } else {
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
            description: `Group ${group.groupName} (non-major updates)`,
            matchManagers: group.managers,
            matchUpdateTypes: nonMajorUpdateTypes,
            groupName: group.groupName
          })
        }
      })
    }
  }

  // NOTE: we intentionally do NOT emit `platformAutomerge`. It delegates merging
  // to the platform's native PR auto-merge, which always goes through a pull
  // request and leaves a merge/squash commit + PR trail. That directly conflicts
  // with this tool's clean-git-history default (automergeType: 'branch', which
  // commits straight to the base branch). If a future option lets users opt into
  // PR-based automerge, platformAutomerge can be reintroduced under that branch.

  if (config.automergeLevel === 'all') {
    configObject.automerge = true
  } else if (config.automergeLevel !== 'disabled' || config.automergeDevDependencies) {
    configObject.packageRules = configObject.packageRules || []

    if (config.automergeLevel !== 'disabled') {
      // (2) Fast lane — automerge low-risk updates. Always include 'pin' and
      // 'digest' alongside the chosen level: these are what the pinning presets
      // (docker:pinDigests, helpers:pinGitHubActionDigests, :pinDevDependencies)
      // generate, and automerging them keeps those bumps on the clean
      // branch-automerge path instead of opening human-merged PRs. The 0.x safety
      // block (3) below still applies and wins via later-rule precedence.
      const matchUpdateTypes = config.automergeLevel === 'patch'
        ? ['patch', 'pin', 'digest']
        : ['minor', 'patch', 'pin', 'digest']

      configObject.packageRules.push({
        description: `Automerge ${matchUpdateTypes.join(', ')} updates (low-risk, non-major)`,
        matchUpdateTypes,
        automerge: true
      })
    }

    if (config.automergeDevDependencies) {
      configObject.packageRules.push({
        description: 'Automerge all devDependencies (build/test tooling, not shipped to consumers)',
        matchDepTypes: ['devDependencies'],
        automerge: true
      })
      configObject.packageRules.push({
        description: 'Automerge Maven/Gradle wrapper updates',
        matchManagers: ['maven-wrapper', 'gradle-wrapper'],
        automerge: true
      })
    }
  }

  // (3) Safety block — must stay AFTER the automerge-enable rule (2): it shares the
  // `automerge` key, and later-rule-wins is exactly what flips these back to false.
  if (config.disablePreOneAutomerge && config.automergeLevel !== 'disabled') {
    configObject.packageRules = configObject.packageRules || []
    configObject.packageRules.push({
      description: 'Never automerge 0.x releases — semver allows breaking changes before 1.0',
      matchCurrentVersion: '/^0\\./',
      automerge: false
    })
  }

  if (config.ignoreTests) {
    configObject.ignoreTests = true
  }

  // (4) Slow lane — keep major updates off the automerge path and out of the PR
  // queue until a human ticks the box on the Dependency Dashboard. Emitted last so
  // the file reads who-is-grouped → what-automerges → safety → manual gate; it
  // shares no key with the rules above, so the position is purely for readability.
  if (config.requireMajorApproval) {
    configObject.packageRules = configObject.packageRules || []
    configObject.packageRules.push({
      description: 'Isolate major updates behind Dependency Dashboard approval',
      matchUpdateTypes: ['major'],
      dependencyDashboardApproval: true
    })
  }

  if (config.minimumReleaseAge !== 'never') {
    const ageMap: Record<string, string> = {
      '3-days': '3 days',
      '7-days': '7 days',
      '14-days': '14 days'
    }
    configObject.minimumReleaseAge = ageMap[config.minimumReleaseAge]
    // Pair with the release-age delay: with "strict", Renovate won't even raise
    // a branch/PR until the version clears minimumReleaseAge, instead of parking
    // a pending PR for days. Less dashboard churn (guide §6).
    configObject.internalChecksFilter = 'strict'
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

  // Only override vulnerabilityAlerts.minimumReleaseAge when a global release-age
  // delay is actually configured. Renovate's global minimumReleaseAge otherwise
  // applies to security updates too, so without "0 days" here those fixes would
  // wait out the same delay — the top-level vulnerabilityAlerts object is the only
  // correct place for the override (`isVulnerabilityAlert` is NOT a valid
  // packageRules matcher and is silently ignored). When no global delay is set,
  // "0 days" just restates Renovate's own default, so we omit it to avoid noise.
  const vulnerabilityAlerts: Record<string, unknown> = {}

  if (config.minimumReleaseAge !== 'never') {
    vulnerabilityAlerts.minimumReleaseAge = '0 days'
  }

  if (config.vulnerabilityAlerts.labels) {
    const labelsArray = config.vulnerabilityAlerts.labels
      .split(',')
      .map(label => label.trim())
      .filter(label => label.length > 0)

    if (labelsArray.length > 0) {
      vulnerabilityAlerts.labels = labelsArray
    }
  }

  if (config.vulnerabilityAlerts.scheduleOverride) {
    vulnerabilityAlerts.schedule = ['at any time']
  }

  if (config.vulnerabilityAlerts.automerge) {
    vulnerabilityAlerts.automerge = true
  }

  // Only attach the block if it carries at least one setting — otherwise an empty
  // `"vulnerabilityAlerts": {}` would just be noise (`:enableVulnerabilityAlerts`
  // in `extends` already turns the feature on).
  if (Object.keys(vulnerabilityAlerts).length > 0) {
    configObject.vulnerabilityAlerts = vulnerabilityAlerts
  }

  return configObject
}

export function generateRenovateConfigJson(config: RenovateConfig): string {
  return JSON.stringify(buildRenovateConfig(config), null, 2)
}

// ---------------------------------------------------------------------------
// Query-param "pseudo API"
//
// configFromQuery() lets the same generation logic be driven by URL query
// parameters instead of the form. It deep-merges the query onto
// defaultRenovateConfig, so every field (current and future) is supported
// automatically without enumerating them here. Nested fields use dot notation:
//
//   ?schedule=weekly&grouping.npm=true&lockFileMaintenance.enabled=false
//
// Booleans accept true/1/yes/on (anything else is false). Unknown keys and
// keys whose value type doesn't match the default are ignored.
// ---------------------------------------------------------------------------

export type QueryValue = string | null | undefined | (string | null)[]
export type ConfigQuery = Record<string, QueryValue>

function parseBool(value: string): boolean {
  const v = value.trim().toLowerCase()
  return v === 'true' || v === '1' || v === 'yes' || v === 'on'
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function applyConfigValue(target: Record<string, any>, path: string[], value: string): void {
  const [head, ...rest] = path
  if (head === undefined || !Object.prototype.hasOwnProperty.call(target, head)) {
    return // unknown key – ignore so the API stays forgiving
  }

  if (rest.length === 0) {
    const current = target[head]
    if (typeof current === 'boolean') {
      target[head] = parseBool(value)
    } else if (typeof current === 'string') {
      target[head] = value
    }
    // other types (none exist today) are intentionally left untouched
    return
  }

  if (typeof target[head] === 'object' && target[head] !== null) {
    applyConfigValue(target[head], rest, value)
  }
}

export function configFromQuery(query: ConfigQuery): RenovateConfig {
  const config = structuredClone(defaultRenovateConfig)

  for (const [rawKey, rawValue] of Object.entries(query)) {
    if (rawValue === undefined) continue
    // For repeated params (?x=a&x=b) take the last occurrence.
    const picked = Array.isArray(rawValue) ? rawValue[rawValue.length - 1] : rawValue
    const value = picked ?? '' // null (bare ?flag) becomes empty string
    applyConfigValue(config, rawKey.split('.'), value)
  }

  return config
}

export function generateConfigJsonFromQuery(query: ConfigQuery): string {
  return generateRenovateConfigJson(configFromQuery(query))
}
