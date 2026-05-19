import { describe, it, expect } from 'vitest'
import { buildRenovateConfig, type RenovateConfig } from './generateRenovateConfig'

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
    minimumReleaseAge: 'never',
    lockFileMaintenance: {
      enabled: true,
      schedule: 'at-any-time',
      automerge: true
    },
    vulnerabilityAlerts: {
      labels: 'security',
      scheduleOverride: true,
      automerge: true
    },
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
    lockFileMaintenance: { ...base.lockFileMaintenance, ...(overrides.lockFileMaintenance ?? {}) },
    vulnerabilityAlerts: { ...base.vulnerabilityAlerts, ...(overrides.vulnerabilityAlerts ?? {}) },
    grouping: { ...base.grouping, ...(overrides.grouping ?? {}) }
  }
}

describe('buildRenovateConfig — vulnerability alert release-age override', () => {
  it('emits vulnerabilityAlerts.minimumReleaseAge: "0 days" by default (defense in depth)', () => {
    const out = buildRenovateConfig(makeConfig())
    expect(out.vulnerabilityAlerts).toBeDefined()
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBe('0 days')
  })

  it('still emits the override when global minimumReleaseAge is "never" — protects against later manual edits', () => {
    const out = buildRenovateConfig(makeConfig({ minimumReleaseAge: 'never' }))
    expect(out.minimumReleaseAge).toBeUndefined()
    expect(out.vulnerabilityAlerts.minimumReleaseAge).toBe('0 days')
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

  it('keeps the override when all vulnerabilityAlerts UI options are off', () => {
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

  it('never places minimumReleaseAge inside a packageRules entry', () => {
    const out = buildRenovateConfig(makeConfig({ minimumReleaseAge: '7-days', automergeDevDependencies: true }))
    const rules = out.packageRules ?? []
    for (const rule of rules) {
      expect(rule).not.toHaveProperty('minimumReleaseAge')
    }
  })
})
