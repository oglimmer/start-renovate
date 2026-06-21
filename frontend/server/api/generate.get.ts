// REST counterpart of the in-browser /generate page.
//
// Both surfaces call the SAME generation logic (app/lib/generateRenovateConfig.ts),
// so the form, the browser page and this endpoint can never drift apart — the
// single source of truth the rest of the codebase is built around.
//
// Unlike /api/feedback (Spring backend: AI, schema validation, rate limit), this
// is a pure, deterministic function of the query string: no auth, no validation,
// no upstream calls. It just maps query params onto the default config and serves
// the resulting renovate.json as application/json so curl / CI / bots can consume
// it directly (the browser page only ever rendered HTML).
import { generateConfigJsonFromQuery } from '../../app/lib/generateRenovateConfig'

export default defineEventHandler((event) => {
  const json = generateConfigJsonFromQuery(getQuery(event))

  // Read-only, no secrets, no rate limit — safe to expose to any origin so the
  // endpoint is usable from browser tooling on other sites, not just same-origin.
  setResponseHeaders(event, {
    'content-type': 'application/json; charset=utf-8',
    'access-control-allow-origin': '*',
    'cache-control': 'public, max-age=3600'
  })

  // Already a pretty-printed JSON string — return it verbatim so the 2-space
  // formatting (identical to the form's output) is preserved on the wire.
  return json
})
