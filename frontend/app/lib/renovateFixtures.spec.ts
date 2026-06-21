import { describe, it, expect } from 'vitest'
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import {
  buildRenovateConfig,
  defaultRenovateConfig,
  renovateDefaultsConfig,
  type RenovateConfig
} from './generateRenovateConfig'
import { parseRenovateJson } from './parseRenovateConfig'

// ===========================================================================
// Shared cross-language fixture corpus
//
// These scenarios are the SINGLE source of truth for the canonical renovate.json
// documents that buildRenovateConfig emits. Each is generated into
// <repo>/fixtures/renovate/<name>.json and consumed by BOTH inverse mappings:
//   • the frontend parser   (parseRenovateConfig.ts — round-tripped below)
//   • the backend analyzer  (RenovateConfigAnalyzer.java — its test loads the
//     same files instead of a hand-copied snapshot)
//
// The "fixtures are up to date" test below regenerates each document in-memory
// and asserts the committed file matches byte-for-byte, so the corpus can never
// drift from buildRenovateConfig. Run `UPDATE_FIXTURES=1 npx vitest run` after an
// intentional generator change to refresh the committed files.
// ===========================================================================

function withDefaults(overrides: Partial<RenovateConfig>): RenovateConfig {
  return { ...structuredClone(defaultRenovateConfig), ...overrides }
}

export const fixtureScenarios: { name: string; config: RenovateConfig }[] = [
  { name: 'default-oglimmer', config: defaultRenovateConfig },
  { name: 'renovate-defaults', config: renovateDefaultsConfig },
  {
    name: 'per-manager-grouping',
    config: withDefaults({
      groupAllNonMajor: false,
      grouping: {
        ...defaultRenovateConfig.grouping,
        npm: true,
        docker: true,
        maven: true,
        githubActions: true
      }
    })
  },
  { name: 'automerge-patch', config: withDefaults({ automergeLevel: 'patch' }) },
  { name: 'automerge-all', config: withDefaults({ automergeLevel: 'all' }) },
  { name: 'pr-limits-conservative', config: withDefaults({ prLimitStrategy: 'conservative' }) },
  { name: 'schedule-outside-business-hours', config: withDefaults({ schedule: 'outside-business-hours' }) },
  { name: 'min-release-age-3-days', config: withDefaults({ minimumReleaseAge: '3-days' }) }
]

const FIXTURE_DIR = fileURLToPath(new URL('../../../fixtures/renovate/', import.meta.url))
const UPDATE = process.env.UPDATE_FIXTURES === '1'

function fixturePath(name: string): string {
  return `${FIXTURE_DIR}${name}.json`
}

describe('shared fixture corpus', () => {
  if (UPDATE) {
    mkdirSync(FIXTURE_DIR, { recursive: true })
    // Index file lets the backend enumerate the corpus without hard-coding names.
    writeFileSync(
      fixturePath('index'),
      JSON.stringify(fixtureScenarios.map(s => s.name), null, 2) + '\n'
    )
  }

  for (const { name, config } of fixtureScenarios) {
    it(`"${name}" fixture is up to date with buildRenovateConfig`, () => {
      const generated = JSON.stringify(buildRenovateConfig(config), null, 2) + '\n'
      if (UPDATE) {
        writeFileSync(fixturePath(name), generated)
        return
      }
      const committed = readFileSync(fixturePath(name), 'utf8')
      expect(committed, `fixtures/renovate/${name}.json is stale — run UPDATE_FIXTURES=1 npx vitest run`).toBe(generated)
    })

    it(`"${name}" round-trips through parseRenovateJson`, () => {
      // The parser recovers a partial; every leaf it returns must match the source
      // config (it omits Renovate-default values that buildRenovateConfig leaves out).
      const parsed = parseRenovateJson(JSON.stringify(buildRenovateConfig(config)))
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const walk = (node: any, source: any, path = ''): void => {
        for (const [key, value] of Object.entries(node)) {
          const here = path ? `${path}.${key}` : key
          if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
            walk(value, source[key], here)
          } else {
            expect(value, `round-trip mismatch at "${here}" for ${name}`).toEqual(source[key])
          }
        }
      }
      walk(parsed, config)
    })
  }
})
