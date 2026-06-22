import { describe, it, expect } from 'vitest'
import {
  buildRenovateConfig,
  configFromQuery,
  configPresets,
  defaultPresetId,
  defaultRenovateConfig,
  generateConfigJsonFromQuery,
  generateRenovateConfigJson,
  renovateDefaultsConfig,
  type RenovateConfig
} from './generateRenovateConfig'

function makeConfig(overrides: Partial<RenovateConfig> = {}): RenovateConfig {
  const base: RenovateConfig = {
    semanticCommits: true,
    timezone: 'Europe/Berlin',
    schedule: 'at-any-time',
    prLimitStrategy: 'active',
    rebaseWhen: 'behind-base-branch',
    rangeStrategy: 'bump',
    automergeType: 'branch',
    automergeLevel: 'minor',
    automergeDevDependencies: false,
    ignoreTests: false,
    disablePreOneAutomerge: true,
    requireMajorApproval: true,
    minimumReleaseAge: '7-days',
    pinning: {
      dockerDigests: true,
      githubActionDigests: true,
      devDependencies: true
    },
    flagAbandonedPackages: true,
    lockFileMaintenance: {
      enabled: true,
      schedule: 'weekly',
      automerge: true
    },
    vulnerabilityAlerts: {
      labels: 'security',
      scheduleOverride: true,
      automerge: true
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
  return {
    ...base,
    ...overrides,
    pinning: { ...base.pinning, ...(overrides.pinning ?? {}) },
    lockFileMaintenance: { ...base.lockFileMaintenance, ...(overrides.lockFileMaintenance ?? {}) },
    vulnerabilityAlerts: { ...base.vulnerabilityAlerts, ...(overrides.vulnerabilityAlerts ?? {}) },
    grouping: { ...base.grouping, ...(overrides.grouping ?? {}) }
  }
}

describe('buildRenovateConfig — vulnerability alert release-age override', () => {
  it('emits vulnerabilityAlerts.minimumReleaseAge: "0 days" by default (a global delay is configured)', () => {
    const out = buildRenovateConfig(makeConfig())
    expect(out.vulnerabilityAlerts).toBeDefined()
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBe('0 days')
  })

  it('omits the override when global minimumReleaseAge is "never" — there is no delay to cancel', () => {
    const out = buildRenovateConfig(makeConfig({ minimumReleaseAge: 'never' }))
    expect(out.minimumReleaseAge).toBeUndefined()
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBeUndefined()
  })

  it('emits both global "7 days" and vulnerabilityAlerts "0 days" when a global delay is configured', () => {
    const out = buildRenovateConfig(makeConfig({ minimumReleaseAge: '7-days' }))
    expect(out.minimumReleaseAge).toBe('7 days')
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBe('0 days')
  })

  it('keeps the override present alongside every other vulnerabilityAlerts customization', () => {
    const out = buildRenovateConfig(makeConfig({
      minimumReleaseAge: '14-days',
      vulnerabilityAlerts: {
        labels: 'security, urgent',
        scheduleOverride: true,
        automerge: true
      }
    }))
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBe('0 days')
    expect(out.vulnerabilityAlerts.labels).toEqual(['security', 'urgent'])
    expect(out.vulnerabilityAlerts.schedule).toEqual(['at any time'])
    expect(out.vulnerabilityAlerts.automerge).toBe(true)
  })

  it('keeps the override (global delay set) when all vulnerabilityAlerts UI options are off', () => {
    const out = buildRenovateConfig(makeConfig({
      minimumReleaseAge: '7-days',
      vulnerabilityAlerts: {
        labels: '',
        scheduleOverride: false,
        automerge: false
      }
    }))
    expect(out.vulnerabilityAlerts).toEqual({ minimumReleaseAge: '0 days' })
  })

  it('never emits a packageRules entry with isVulnerabilityAlert — that matcher is silently ignored by Renovate', () => {
    const configs: RenovateConfig[] = [
      makeConfig(),
      makeConfig({ minimumReleaseAge: '3-days' }),
      makeConfig({ minimumReleaseAge: '7-days', automergeLevel: 'all' }),
      makeConfig({ minimumReleaseAge: '14-days', automergeDevDependencies: true, disablePreOneAutomerge: true }),
      makeConfig({ grouping: { npm: true, docker: true, maven: false, gradle: false, pip: false, composer: false, helm: false, githubActions: false, terraform: false, gomod: false, cargo: false, bundler: false, nuget: false } })
    ]
    for (const cfg of configs) {
      const out = buildRenovateConfig(cfg)
      const rules = out.packageRules ?? []
      for (const rule of rules) {
        expect(rule).not.toHaveProperty('isVulnerabilityAlert')
      }
    }
  })

  it('never introduces a positive minimumReleaseAge delay via a packageRules entry (rules may only cancel it with "0 days")', () => {
    // A per-rule minimumReleaseAge is valid Renovate config, but the global
    // stabilization window must never be re-scoped into a rule — the only legitimate
    // in-rule use is the Docker :latest carve-out, which CANCELS the delay ("0 days").
    const out = buildRenovateConfig(makeConfig({ minimumReleaseAge: '7-days', automergeDevDependencies: true }))
    const rules = out.packageRules ?? []
    for (const rule of rules) {
      if (Object.prototype.hasOwnProperty.call(rule, 'minimumReleaseAge')) {
        expect(rule.minimumReleaseAge).toBe('0 days')
      }
    }
  })

  it('carves Docker :latest out of digest pinning and cancels the release-age delay for digest pins', () => {
    const rulesFor = (overrides: Partial<RenovateConfig>) =>
      (buildRenovateConfig(makeConfig(overrides)).packageRules ?? []) as Record<string, unknown>[]
    const isLatest = (r: Record<string, unknown>) =>
      Array.isArray(r.matchDatasources) && (r.matchDatasources as string[]).includes('docker')
      && r.matchCurrentValue === '/^latest$/'
    const isDigestAge = (r: Record<string, unknown>) =>
      Array.isArray(r.matchDatasources) && (r.matchDatasources as string[]).includes('docker')
      && Array.isArray(r.matchUpdateTypes) && (r.matchUpdateTypes as string[]).includes('digest')

    // dockerDigests on + a global delay → :latest is never pinned (and carries no
    // release-age key itself), and digest/pin updates get the delay cancelled.
    const withDelay = rulesFor({
      pinning: { dockerDigests: true, githubActionDigests: false, devDependencies: false },
      minimumReleaseAge: '7-days'
    })
    const latest = withDelay.find(isLatest)
    expect(latest).toMatchObject({ pinDigests: false })
    expect(latest).not.toHaveProperty('minimumReleaseAge')
    expect(withDelay.find(isDigestAge)).toMatchObject({
      matchUpdateTypes: ['pin', 'digest'],
      minimumReleaseAge: '0 days'
    })

    // dockerDigests on but no global delay → still don't pin :latest, but there is
    // no delay to cancel, so omit the digest-age rule entirely.
    const noDelay = rulesFor({
      pinning: { dockerDigests: true, githubActionDigests: false, devDependencies: false },
      minimumReleaseAge: 'never'
    })
    expect(noDelay.find(isLatest)).toMatchObject({ pinDigests: false })
    expect(noDelay.find(isDigestAge)).toBeUndefined()

    // dockerDigests off → neither rule (nothing would pin a Docker digest).
    const off = rulesFor({ pinning: { dockerDigests: false, githubActionDigests: false, devDependencies: false } })
    expect(off.find(isLatest)).toBeUndefined()
    expect(off.find(isDigestAge)).toBeUndefined()
  })
})

describe('buildRenovateConfig — safe-by-default settings', () => {
  it('defaults minimumReleaseAge to a 7-day stabilization window (not "never")', () => {
    expect(defaultRenovateConfig.minimumReleaseAge).toBe('7-days')
    const out = buildRenovateConfig(defaultRenovateConfig)
    expect(out.minimumReleaseAge).toBe('7 days')
    // ...while security fixes are still never delayed.
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBe('0 days')
  })

  it('keeps branch automerge as the clean-history default and never emits platformAutomerge', () => {
    expect(defaultRenovateConfig.automergeType).toBe('branch')
    const out = buildRenovateConfig(defaultRenovateConfig)
    expect(out.automergeType).toBe('branch')
    expect(out).not.toHaveProperty('platformAutomerge')
    const rules = out.packageRules ?? []
    for (const rule of rules) {
      expect(rule).not.toHaveProperty('platformAutomerge')
    }
  })

  it('always emits configMigration: true', () => {
    expect(buildRenovateConfig(makeConfig()).configMigration).toBe(true)
    expect(buildRenovateConfig(defaultRenovateConfig).configMigration).toBe(true)
  })

  it('enables the config:best-practices hardening presets by default via extends', () => {
    expect(defaultRenovateConfig.pinning).toEqual({ dockerDigests: true, githubActionDigests: true, devDependencies: true })
    expect(defaultRenovateConfig.flagAbandonedPackages).toBe(true)
    const out = buildRenovateConfig(defaultRenovateConfig)
    expect(out.extends).toContain('docker:pinDigests')
    expect(out.extends).toContain('helpers:pinGitHubActionDigests')
    expect(out.extends).toContain(':pinDevDependencies')
    expect(out.extends).toContain('abandonments:recommended')
  })

  it('omits each hardening preset when its flag is off', () => {
    const out = buildRenovateConfig(makeConfig({
      pinning: { dockerDigests: false, githubActionDigests: false, devDependencies: false },
      flagAbandonedPackages: false
    }))
    expect(out.extends).not.toContain('docker:pinDigests')
    expect(out.extends).not.toContain('helpers:pinGitHubActionDigests')
    expect(out.extends).not.toContain(':pinDevDependencies')
    expect(out.extends).not.toContain('abandonments:recommended')
  })

  it('automerges pin and digest update types so hardening keeps a clean git history', () => {
    // minor level → minor + patch + pin + digest
    const minorRule = buildRenovateConfig(makeConfig({ automergeLevel: 'minor' })).packageRules
      .find((r: { matchUpdateTypes?: string[] }) => Array.isArray(r.matchUpdateTypes))
    expect(minorRule.matchUpdateTypes).toEqual(['minor', 'patch', 'pin', 'digest'])

    // patch level → patch + pin + digest
    const patchRule = buildRenovateConfig(makeConfig({ automergeLevel: 'patch' })).packageRules
      .find((r: { matchUpdateTypes?: string[] }) => Array.isArray(r.matchUpdateTypes))
    expect(patchRule.matchUpdateTypes).toEqual(['patch', 'pin', 'digest'])
  })

  it('defaults lock-file maintenance to a weekly schedule (matches :maintainLockFilesWeekly)', () => {
    expect(defaultRenovateConfig.lockFileMaintenance.schedule).toBe('weekly')
    const out = buildRenovateConfig(defaultRenovateConfig)
    expect(out.lockFileMaintenance.schedule).toEqual(['before 5am on monday'])
    expect(out.lockFileMaintenance.automerge).toBe(true)
  })

  it('adds internalChecksFilter: "strict" whenever a release-age delay is set, and omits it for "never"', () => {
    expect(buildRenovateConfig(defaultRenovateConfig).internalChecksFilter).toBe('strict')
    expect(buildRenovateConfig(makeConfig({ minimumReleaseAge: '14-days' })).internalChecksFilter).toBe('strict')
    expect(buildRenovateConfig(makeConfig({ minimumReleaseAge: 'never' })).internalChecksFilter).toBeUndefined()
  })

  it('gates major updates behind dashboard approval by default, and drops the rule when disabled', () => {
    expect(defaultRenovateConfig.requireMajorApproval).toBe(true)
    const gated = buildRenovateConfig(defaultRenovateConfig).packageRules
      .find((r: { matchUpdateTypes?: string[]; dependencyDashboardApproval?: boolean }) =>
        r.matchUpdateTypes?.includes('major') && r.dependencyDashboardApproval === true)
    expect(gated).toBeDefined()

    const off = buildRenovateConfig(makeConfig({ requireMajorApproval: false })).packageRules ?? []
    expect(off.some((r: { dependencyDashboardApproval?: boolean }) => r.dependencyDashboardApproval === true)).toBe(false)
  })

  it('keeps the major-approval gate independent of automerge (works even when automerge is disabled)', () => {
    const out = buildRenovateConfig(makeConfig({ automergeLevel: 'disabled', requireMajorApproval: true }))
    const gated = (out.packageRules ?? []).find((r: { matchUpdateTypes?: string[]; dependencyDashboardApproval?: boolean }) =>
      r.matchUpdateTypes?.includes('major') && r.dependencyDashboardApproval === true)
    expect(gated).toBeDefined()
  })

  it('exposes hardening flags through the query-param API', () => {
    expect(configFromQuery({ 'pinning.dockerDigests': 'false' }).pinning.dockerDigests).toBe(false)
    expect(configFromQuery({ 'pinning.devDependencies': 'false' }).pinning.devDependencies).toBe(false)
    expect(configFromQuery({ flagAbandonedPackages: 'false' }).flagAbandonedPackages).toBe(false)
    expect(configFromQuery({ requireMajorApproval: 'false' }).requireMajorApproval).toBe(false)
    expect(configFromQuery({}).pinning).toEqual({ dockerDigests: true, githubActionDigests: true, devDependencies: true })
  })
})

describe('buildRenovateConfig — fast/slow lane grouping', () => {
  const isGroupRule = (r: { groupName?: string }) => typeof r.groupName === 'string'

  it('groupAllNonMajor emits a single {{manager}} rule scoped to non-major update types', () => {
    const out = buildRenovateConfig(makeConfig({ groupAllNonMajor: true }))
    const groupRules = (out.packageRules ?? []).filter(isGroupRule)
    expect(groupRules).toContainEqual({
      description: "Group each manager's non-major updates into one PR per manager",
      matchUpdateTypes: ['minor', 'patch', 'pin', 'digest'],
      groupName: '{{manager}} non-major dependencies'
    })
    // No manager-pinned group rules when auto-grouping is on.
    expect(groupRules.every((r: { matchManagers?: string[] }) => r.matchManagers === undefined)).toBe(true)
  })

  it('groupAllNonMajor supersedes the per-manager toggles (those rules are not emitted)', () => {
    const out = buildRenovateConfig(makeConfig({
      groupAllNonMajor: true,
      grouping: { npm: true, docker: true, maven: false, gradle: false, pip: false, composer: false, helm: false, githubActions: false, terraform: false, gomod: false, cargo: false, bundler: false, nuget: false }
    }))
    const groupRules = (out.packageRules ?? []).filter(isGroupRule)
    expect(groupRules).toHaveLength(1)
    expect(groupRules[0].groupName).toBe('{{manager}} non-major dependencies')
  })

  it('per-manager grouping is scoped to non-major so majors never join a group', () => {
    const out = buildRenovateConfig(makeConfig({
      groupAllNonMajor: false,
      grouping: { npm: true, docker: false, maven: false, gradle: false, pip: false, composer: false, helm: false, githubActions: false, terraform: false, gomod: false, cargo: false, bundler: false, nuget: false }
    }))
    const groupRules = (out.packageRules ?? []).filter(isGroupRule)
    expect(groupRules).toContainEqual({
      description: 'Group npm dependencies (non-major updates)',
      matchManagers: ['npm'],
      matchUpdateTypes: ['minor', 'patch', 'pin', 'digest'],
      groupName: 'npm dependencies'
    })
    // Every group rule excludes major.
    for (const r of groupRules) {
      expect((r as { matchUpdateTypes?: string[] }).matchUpdateTypes).not.toContain('major')
    }
  })

  it('defaults groupAllNonMajor on in the oglimmer preset and off in renovate-defaults', () => {
    expect(defaultRenovateConfig.groupAllNonMajor).toBe(true)
    expect(renovateDefaultsConfig.groupAllNonMajor).toBe(false)
  })

  it('emits packageRules top-to-bottom: group → automerge → 0.x safety block → major gate', () => {
    const rules = buildRenovateConfig(defaultRenovateConfig).packageRules ?? []
    const idx = (pred: (r: Record<string, unknown>) => boolean) => rules.findIndex(pred)

    const group = idx((r) => typeof r.groupName === 'string')
    const automerge = idx((r) => Array.isArray(r.matchUpdateTypes)
      && (r.matchUpdateTypes as string[]).includes('minor') && r.automerge === true)
    const safety = idx((r) => r.matchCurrentVersion === '/^0\\./')
    const major = idx((r) => Array.isArray(r.matchUpdateTypes)
      && (r.matchUpdateTypes as string[]).includes('major'))

    expect(group).toBeGreaterThanOrEqual(0)
    expect(group).toBeLessThan(automerge)
    // the 0.x block shares the `automerge` key, so it MUST follow the enable rule.
    expect(automerge).toBeLessThan(safety)
    expect(safety).toBeLessThan(major)
    // major gate is the final rule.
    expect(major).toBe(rules.length - 1)
  })

  it('gives every emitted packageRule a description (the file should teach, not just configure)', () => {
    for (const cfg of [defaultRenovateConfig, makeConfig({ groupAllNonMajor: false, grouping: { ...makeConfig().grouping, npm: true, docker: true } })]) {
      const rules = buildRenovateConfig(cfg).packageRules ?? []
      expect(rules.length).toBeGreaterThan(0)
      for (const rule of rules) {
        expect(typeof rule.description).toBe('string')
        expect((rule.description as string).length).toBeGreaterThan(0)
      }
    }
  })

  it('is reachable through the query-param API', () => {
    expect(configFromQuery({ groupAllNonMajor: 'false' }).groupAllNonMajor).toBe(false)
    expect(configFromQuery({}).groupAllNonMajor).toBe(true)
  })
})

describe('configPresets — selectable starting points', () => {
  it('lists renovate-defaults first (it is the form default) followed by oglimmer', () => {
    expect(configPresets.map(p => p.id)).toEqual(['renovate-defaults', 'oglimmer'])
  })

  it('defaults the form to the renovate-defaults preset', () => {
    expect(defaultPresetId).toBe('renovate-defaults')
    expect(configPresets.some(p => p.id === defaultPresetId)).toBe(true)
  })

  it('maps the oglimmer preset to the shared opinionated defaults', () => {
    const oglimmer = configPresets.find(p => p.id === 'oglimmer')
    expect(oglimmer?.config).toBe(defaultRenovateConfig)
  })

  it('automerges all dev dependencies in the oglimmer preset by default', () => {
    expect(defaultRenovateConfig.automergeDevDependencies).toBe(true)
    const rules = buildRenovateConfig(defaultRenovateConfig).packageRules ?? []
    expect(rules).toContainEqual({
      description: 'Automerge all devDependencies (build/test tooling, not shipped to consumers)',
      matchDepTypes: ['devDependencies'],
      automerge: true
    })
  })

  it('maps the renovate-defaults preset to the neutral baseline', () => {
    const preset = configPresets.find(p => p.id === 'renovate-defaults')
    expect(preset?.config).toBe(renovateDefaultsConfig)
  })

  it('renovate-defaults parks every opinionated knob at its neutral position', () => {
    expect(renovateDefaultsConfig.semanticCommits).toBe(false)
    expect(renovateDefaultsConfig.automergeLevel).toBe('disabled')
    expect(renovateDefaultsConfig.minimumReleaseAge).toBe('never')
    expect(renovateDefaultsConfig.requireMajorApproval).toBe(false)
    expect(renovateDefaultsConfig.lockFileMaintenance.enabled).toBe(false)
    expect(renovateDefaultsConfig.pinning).toEqual({
      dockerDigests: false,
      githubActionDigests: false,
      devDependencies: false
    })
    expect(renovateDefaultsConfig.flagAbandonedPackages).toBe(false)
    expect(Object.values(renovateDefaultsConfig.grouping).every(v => v === false)).toBe(true)
  })

  it('renovate-defaults emits none of the hardening/automerge opinions, only the always-on baseline', () => {
    const out = buildRenovateConfig(renovateDefaultsConfig)
    // Always-on baseline the form can't turn off.
    expect(out.extends).toContain('config:recommended')
    expect(out.extends).toContain(':enableVulnerabilityAlerts')
    expect(out.extends).toContain(':dependencyDashboard')
    // Opinions that must be absent in the neutral preset.
    expect(out.extends).not.toContain(':semanticCommits')
    expect(out.extends).not.toContain('docker:pinDigests')
    expect(out.extends).not.toContain('helpers:pinGitHubActionDigests')
    expect(out.extends).not.toContain(':pinDevDependencies')
    expect(out.extends).not.toContain('abandonments:recommended')
    expect(out.packageRules).toBeUndefined()
    expect(out.minimumReleaseAge).toBeUndefined()
    // No global delay and no vuln-alert opinions → the whole block is omitted
    // (`:enableVulnerabilityAlerts` in extends already enables the feature).
    expect(out.vulnerabilityAlerts).toBeUndefined()
    expect(out.internalChecksFilter).toBeUndefined()
    expect(out.lockFileMaintenance).toBeUndefined()
    expect(out.automergeType).toBeUndefined()
  })
})

describe('configFromQuery — query-param pseudo API', () => {
  it('returns the defaults (deep clone) for an empty query', () => {
    const out = configFromQuery({})
    expect(out).toEqual(defaultRenovateConfig)
    expect(out).not.toBe(defaultRenovateConfig)
    expect(out.grouping).not.toBe(defaultRenovateConfig.grouping)
  })

  it('produces the same JSON as the form defaults for an empty query', () => {
    expect(generateConfigJsonFromQuery({})).toBe(generateRenovateConfigJson(defaultRenovateConfig))
  })

  it('coerces boolean fields from common truthy/falsy strings', () => {
    expect(configFromQuery({ semanticCommits: 'false' }).semanticCommits).toBe(false)
    expect(configFromQuery({ 'grouping.npm': 'true' }).grouping.npm).toBe(true)
    expect(configFromQuery({ 'grouping.npm': '1' }).grouping.npm).toBe(true)
    expect(configFromQuery({ 'grouping.npm': 'yes' }).grouping.npm).toBe(true)
    expect(configFromQuery({ 'grouping.npm': 'on' }).grouping.npm).toBe(true)
    expect(configFromQuery({ 'grouping.npm': 'nonsense' }).grouping.npm).toBe(false)
  })

  it('assigns string fields verbatim', () => {
    const out = configFromQuery({ timezone: 'UTC', schedule: 'weekly', 'vulnerabilityAlerts.labels': 'security,urgent' })
    expect(out.timezone).toBe('UTC')
    expect(out.schedule).toBe('weekly')
    expect(out.vulnerabilityAlerts.labels).toBe('security,urgent')
  })

  it('maps nested keys via dot notation', () => {
    const out = configFromQuery({ 'lockFileMaintenance.enabled': 'false', 'lockFileMaintenance.automerge': 'false' })
    expect(out.lockFileMaintenance.enabled).toBe(false)
    expect(out.lockFileMaintenance.automerge).toBe(false)
  })

  it('ignores unknown keys and never mutates the exported defaults', () => {
    const snapshot = structuredClone(defaultRenovateConfig)
    const out = configFromQuery({ bogus: 'x', 'grouping.nope': 'true', 'grouping.npm': 'true' })
    expect(out).not.toHaveProperty('bogus')
    expect(out.grouping).not.toHaveProperty('nope')
    expect(out.grouping.npm).toBe(true)
    expect(defaultRenovateConfig).toEqual(snapshot)
  })

  it('takes the last value for repeated params and treats a bare flag as empty', () => {
    expect(configFromQuery({ schedule: ['weekly', 'monthly'] }).schedule).toBe('monthly')
    expect(configFromQuery({ timezone: null }).timezone).toBe('')
    expect(configFromQuery({ semanticCommits: null }).semanticCommits).toBe(false)
  })
})
