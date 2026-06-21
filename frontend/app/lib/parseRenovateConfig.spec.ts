import { describe, it, expect } from 'vitest'
import {
  buildRenovateConfig,
  configPresets,
  defaultRenovateConfig,
  generateRenovateConfigJson,
  type RenovateConfig
} from './generateRenovateConfig'
import { parseRenovateJson } from './parseRenovateConfig'

// parseRenovateJson is the inverse of buildRenovateConfig and re-hardcodes the
// strings buildRenovateConfig emits (schedule cron text, manager names, PR-limit
// numbers, extends preset ids). These tests pin the two together: if the forward
// generator changes any emitted string, the round-trip below stops being stable
// and this spec fails — catching FE drift the way the backend's analyzer test
// guards the BE twin.

// Every leaf the parser recovers must equal the source config's leaf. The parser
// is intentionally partial (it omits anything it can't confidently read back, e.g.
// Renovate-default values that buildRenovateConfig leaves out of the JSON), so we
// only assert the keys it DOES return — and demand those be exactly right.
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function expectRecoveredLeavesMatch(parsed: any, source: any, path = ''): void {
  for (const [key, value] of Object.entries(parsed)) {
    const here = path ? `${path}.${key}` : key
    if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
      expectRecoveredLeavesMatch(value, source[key], here)
    } else {
      expect(value, `round-trip mismatch at "${here}"`).toEqual(source[key])
    }
  }
}

function roundTrip(config: RenovateConfig): Partial<RenovateConfig> {
  return parseRenovateJson(generateRenovateConfigJson(config))
}

describe('parseRenovateJson — round-trips buildRenovateConfig output', () => {
  for (const preset of configPresets) {
    it(`recovers the "${preset.id}" preset without drift`, () => {
      expectRecoveredLeavesMatch(roundTrip(preset.config), preset.config)
    })
  }

  it('recovers per-manager grouping (groupAllNonMajor off)', () => {
    const config: RenovateConfig = {
      ...structuredClone(defaultRenovateConfig),
      groupAllNonMajor: false,
      grouping: {
        ...defaultRenovateConfig.grouping,
        npm: true,
        docker: true,
        maven: true,
        githubActions: true
      }
    }
    const parsed = roundTrip(config)
    expect(parsed.grouping).toEqual(config.grouping)
  })

  it('recovers patch-only automerge', () => {
    const config: RenovateConfig = {
      ...structuredClone(defaultRenovateConfig),
      automergeLevel: 'patch'
    }
    expect(roundTrip(config).automergeLevel).toBe('patch')
  })

  it('recovers "all" automerge', () => {
    const config: RenovateConfig = {
      ...structuredClone(defaultRenovateConfig),
      automergeLevel: 'all'
    }
    expect(roundTrip(config).automergeLevel).toBe('all')
  })

  it('recovers conservative PR limits', () => {
    const config: RenovateConfig = {
      ...structuredClone(defaultRenovateConfig),
      prLimitStrategy: 'conservative'
    }
    expect(roundTrip(config).prLimitStrategy).toBe('conservative')
  })

  it.each(['weekly', 'monthly', 'weekends', 'outside-business-hours'] as const)(
    'recovers the "%s" schedule',
    (schedule) => {
      const config: RenovateConfig = {
        ...structuredClone(defaultRenovateConfig),
        schedule
      }
      expect(roundTrip(config).schedule).toBe(schedule)
    }
  )

  it.each(['3-days', '7-days', '14-days'] as const)(
    'recovers the "%s" minimum release age',
    (minimumReleaseAge) => {
      const config: RenovateConfig = {
        ...structuredClone(defaultRenovateConfig),
        minimumReleaseAge
      }
      expect(roundTrip(config).minimumReleaseAge).toBe(minimumReleaseAge)
    }
  )
})

describe('parseRenovateJson — tolerant of arbitrary input', () => {
  it('returns an empty-ish partial for an unrecognized config', () => {
    const parsed = parseRenovateJson(JSON.stringify({ extends: ['config:recommended'] }))
    // extends is an array, so pinning/semanticCommits/flagAbandoned are read as off…
    expect(parsed.semanticCommits).toBe(false)
    expect(parsed.pinning).toEqual({ dockerDigests: false, githubActionDigests: false, devDependencies: false })
    // …but no schedule/automerge/limits were present, so they stay unset.
    expect(parsed.schedule).toBeUndefined()
    expect(parsed.automergeLevel).toBeUndefined()
    expect(parsed.prLimitStrategy).toBeUndefined()
  })

  it('ignores unknown keys without throwing', () => {
    expect(() => parseRenovateJson(JSON.stringify({ somethingUnknown: 42, packageRules: [] }))).not.toThrow()
  })

  it('throws on invalid JSON (caller catches and surfaces the message)', () => {
    expect(() => parseRenovateJson('{ not valid json')).toThrow()
  })

  // Sanity anchor: buildRenovateConfig is the exact string source the parser keys
  // off, so a direct emit→parse on the hardened default must agree on the headline
  // hardening toggles.
  it('agrees with buildRenovateConfig on the hardened defaults', () => {
    const json = JSON.stringify(buildRenovateConfig(defaultRenovateConfig))
    const parsed = parseRenovateJson(json)
    expect(parsed.pinning).toEqual(defaultRenovateConfig.pinning)
    expect(parsed.requireMajorApproval).toBe(true)
    expect(parsed.disablePreOneAutomerge).toBe(true)
    expect(parsed.flagAbandonedPackages).toBe(true)
  })
})
