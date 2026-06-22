import { describe, it, expect } from 'vitest'
import { readFileSync, writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import {
  buildRenovateConfig,
  defaultRenovateConfig,
  type RenovateConfig
} from './generateRenovateConfig'

// ===========================================================================
// Dogfooding: this repo's OWN renovate.json must be generator output
//
// start-renovate is a renovate.json generator, so its own committed config has to
// be something the tool actually emits — never a hand-edit that drifts from what
// users would get. This is THE_REPO_CONFIG: the oglimmer hardened profile (the
// shared single source of truth) with one deviation — a weekly schedule.
//
// The guard below regenerates renovate.json in-memory and asserts the committed
// file matches byte-for-byte, so any generator change that would alter our own
// output fails CI until the file is refreshed. Run `npm run renovate:regen` (==
// UPDATE_FIXTURES=1 vitest run) after an intentional generator change to rewrite
// both this file and the fixtures corpus together. Mirrors renovateFixtures.spec.ts.
// ===========================================================================

export const THE_REPO_CONFIG: RenovateConfig = {
  ...structuredClone(defaultRenovateConfig),
  // Updates land in one weekly batch (before 5am Monday) rather than continuously.
  schedule: 'weekly'
}

const REPO_RENOVATE_JSON = fileURLToPath(new URL('../../../renovate.json', import.meta.url))
const UPDATE = process.env.UPDATE_FIXTURES === '1'

describe('repo dogfooding — renovate.json is generator output', () => {
  it('renovate.json is up to date with buildRenovateConfig(THE_REPO_CONFIG)', () => {
    const generated = JSON.stringify(buildRenovateConfig(THE_REPO_CONFIG), null, 2) + '\n'
    if (UPDATE) {
      writeFileSync(REPO_RENOVATE_JSON, generated)
      return
    }
    const committed = readFileSync(REPO_RENOVATE_JSON, 'utf8')
    expect(
      committed,
      'renovate.json is stale or hand-edited — run `npm run renovate:regen` to regenerate it from the tool'
    ).toBe(generated)
  })
})
