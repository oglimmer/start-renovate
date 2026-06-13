# `renovate.json` — Engineer's Configuration Guide

A practical, opinionated reference for configuring Renovate. Scope: the repository config file only — what engineers write. Assumes the bot is already installed (Mend app or self-hosted) at the org level.

---

## 1. The file

- **Filename / location** (first match wins): `renovate.json`, `renovate.json5`, `.github/renovate.json`, `.renovaterc`, `.renovaterc.json`, or a `renovate` key in `package.json`. Pick one per repo — multiple is undefined behavior.
- **JSON5/JSONC** (`renovate.json5`) supports comments and trailing commas. Prefer it for any non-trivial config.
- **Always add `$schema`** for editor autocomplete + validation:

```json
{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": ["config:recommended"]
}
```

**Mental model:** config in `extends` is resolved *first* and has *lower* precedence than raw keys in the same file. Your local keys win over presets.

---

## 2. Start from a preset — don't hand-roll

Two official starting points:

| Preset | Use when |
|---|---|
| `config:recommended` | Default. Sensible behavior for any language. Start here. |
| `config:best-practices` | You want the maintainers' hardened defaults (pinning, lockfile maintenance, release-age delays). Recommended for production. |

`config:best-practices` expands to:

```json
{
  "extends": [
    "config:recommended",
    "docker:pinDigests",
    "helpers:pinGitHubActionDigests",
    ":configMigration",
    ":pinDevDependencies",
    "abandonments:recommended",
    "security:minimumReleaseAgeNpm",
    ":maintainLockFilesWeekly"
  ]
}
```

- **DO** extend a preset and override only what you need.
- **DON'T** copy a giant config from a blog post. Start minimal; add rules when you hit a real problem.
- **DO** centralize org-wide config in a shared preset repo (`default.json`) and reference it: `"extends": ["local>my-org/renovate-config"]` (use `local>` for self-hosted/GHE; `github>org/repo`, `gitlab>org/repo` otherwise). Pin to a tag for reproducibility: `github>org/repo#1.2.3`.
- **DON'T** use npm-based presets — deprecated.

---

## 3. Core keys cheat-sheet

```json5
{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": ["config:best-practices"],
  "timezone": "Europe/Berlin",
  "schedule": ["before 6am on monday"],
  "prHourlyLimit": 2,
  "prConcurrentLimit": 10,
  "labels": ["dependencies"],
  "dependencyDashboard": true,
  "rangeStrategy": "bump",
  "configMigration": true
}
```

| Key | What it does | Note |
|---|---|---|
| `extends` | Inherit presets | Lower precedence than local keys |
| `timezone` | TZ for `schedule` | IANA name, e.g. `Europe/Berlin` |
| `schedule` | When PRs/branches are created | Cron-like English. Off-hours/weekly cuts noise |
| `prHourlyLimit` | New PRs/hour | `0` = unlimited. Default `2` |
| `prConcurrentLimit` | Max open Renovate PRs | Backpressure on CI |
| `labels` / `assignees` / `reviewers` | PR metadata | |
| `dependencyDashboard` | Tracking issue (on by default in `config:recommended`) | Single source of truth |
| `rangeStrategy` | How version ranges are rewritten | See §7 |
| `packageRules` | Per-dependency overrides | The real engine — §4 |
| `minimumReleaseAge` | Delay before a release is eligible | Supply-chain defense — §6 |
| `automerge` | Merge without human review | Use carefully — §5 |
| `vulnerabilityAlerts` | Fast-track security fixes | §6 |
| `enabledManagers` | Whitelist managers | Scope down noise |
| `ignorePaths` / `ignoreDeps` | Exclude paths/deps | |
| `configMigration` | Auto-PR to rename deprecated options | Keep it `true` |

---

## 4. `packageRules` — the engine

An array of `{ matchers..., settings... }`. For each dependency, Renovate finds all matching rules and merges them.

### Two rules you must internalize

1. **Within one rule, all matcher *types* are AND.** A rule with `matchManagers` *and* `matchUpdateTypes` only fires when both match.
2. **Within a single matcher, values are OR.** `"matchUpdateTypes": ["minor", "patch"]` = minor OR patch.
3. **Later rules override earlier rules.** Order matters: put broad rules first, specific overrides last.

```json5
{
  "packageRules": [
    // broad: group + automerge all non-major
    { "matchUpdateTypes": ["minor", "patch"], "groupName": "non-major", "automerge": true },
    // specific override: never automerge this one, even patch
    { "matchPackageNames": ["aws-sdk"], "automerge": false }
  ]
}
```

### Matchers (common)

- `matchManagers` — `["npm"]`, `["maven"]`, `["gradle"]`, `["dockerfile"]`, `["github-actions"]`, `["kubernetes"]`, …
- `matchDatasources` — `["npm"]`, `["docker"]`, `["maven"]`, …
- `matchUpdateTypes` — `["major", "minor", "patch", "pin", "digest", "lockFileMaintenance"]`
- `matchDepTypes` — `["dependencies", "devDependencies", "peerDependencies"]`
- `matchFileNames` — glob, e.g. `["packages/api/**"]`
- `matchPackageNames` — exact names **and** regex (regex wrapped in slashes)

### Name matching — current syntax (important)

`matchPackagePatterns`, `matchPackagePrefixes`, `matchPackageNames` (old) are **deprecated/consolidated**. Today, use **`matchPackageNames`** for both exact and regex:

```json5
{
  "matchPackageNames": [
    "lodash",            // exact
    "/^@angular//",      // regex: leading + trailing slash
    "!/^@types//"        // negated regex
  ]
}
```

- **DO** use `matchPackageNames` going forward; let `configMigration` rewrite old configs.
- **DON'T** mix deprecated matchers in new rules.
- Note: `matchPackageNames` matches the *human-facing* `depName`; the registry-facing `packageName` can differ (proxies, monorepos). For exact-source matching use `matchDepNames` / `matchSourceUrls` / `matchDatasources` as needed.

---

## 5. Automerge — safe configuration

Automerge is powerful and the biggest footgun. Rules of thumb:

- **DO** require passing CI. Automerge only fires when **branch protection / required status checks pass**. No CI = no safety net; don't automerge.
- **DO** prefer `platformAutomerge: true` — delegates to the platform's native auto-merge (GitHub/GitLab), so it merges the moment checks pass instead of waiting for the next Renovate run.
- **DO** scope automerge to low-risk update types first: patch, minor, digests, lockfile maintenance, dev dependencies.
- **DON'T** blindly automerge majors. Route them through review or dashboard approval.
- **DO** pair automerge with `minimumReleaseAge` (§6) to dodge compromised/yanked releases.

```json5
{
  "packageRules": [
    {
      "matchUpdateTypes": ["patch", "minor", "pin", "digest"],
      "automerge": true,
      "platformAutomerge": true,
      "minimumReleaseAge": "14 days"
    },
    {
      "matchDepTypes": ["devDependencies"],
      "matchUpdateTypes": ["major"],
      "automerge": true
    }
  ]
}
```

- `automergeType`: `"pr"` (default, merges the PR), `"branch"` (no PR — quietest, but you lose the PR audit trail), `"pr-comment"`.
- **DON'T** combine `automergeType: "branch"` with required PR reviews — they conflict.

---

## 6. Supply-chain & security safety

### `minimumReleaseAge` — wait before adopting

Delays a version until it's been public for N days, giving registries time to pull malicious/broken releases.

- **DO** set `"minimumReleaseAge": "14 days"` (or `"7 days"` at minimum) for anything you automerge, especially npm. Maintainers recommend 14 days for automerged third-party deps.
- Combine with `internalChecksFilter: "strict"` so PRs aren't even raised until the age requirement is met (less dashboard churn).

### Pin executables, float libraries

- **DO** pin Docker image digests (`docker:pinDigests`) and GitHub Action SHAs (`helpers:pinGitHubActionDigests`). Tags are mutable; digests/SHAs are not — this is a real supply-chain control. Renovate keeps them updated.
- **DO** pin versions of *applications* and CI tooling; for *libraries* you publish, keep ranges so consumers aren't over-constrained.

### Vulnerability alerts — fast lane

`vulnerabilityAlerts` is a special rule applied only to security fixes. Defaults already create PRs immediately, ignore schedule, and use `rangeStrategy: update-lockfile`.

```json5
{
  "vulnerabilityAlerts": {
    "labels": ["security"],
    "automerge": true,
    "platformAutomerge": true
  }
}
```

Requires the platform's security feed enabled (e.g. GitHub Dependency graph + Dependabot alerts). `minimumReleaseAge` does **not** apply here — security fixes shouldn't wait.

---

## 7. `rangeStrategy` — how ranges get rewritten

Controls what Renovate writes back into your manifest. Default in `config:recommended` is `auto`.

| Value | Behavior | Use for |
|---|---|---|
| `pin` | Replace ranges with exact versions | Apps / deployables — reproducible builds |
| `bump` | Raise the lower bound, keep the range (`^1.2.0` → `^1.3.0`) | Common app default |
| `replace` | Only change when current range no longer satisfies | Conservative |
| `widen` | Expand the range to include new version | `peerDependencies` |
| `update-lockfile` | Update lockfile only, leave manifest range alone | When the lockfile is the source of truth |
| `auto` | Renovate picks per ecosystem | Default; fine to keep |

- **DO** use `pin` (or commit lockfiles) for apps; `widen` for `peerDependencies` (preset `:widenPeerDependencies`).
- **DON'T** `pin` a library you publish — it over-constrains downstream consumers.

---

## 8. Reducing noise (the #1 complaint)

PR fatigue kills adoption. Tactics, roughly in order of impact:

1. **Group** related updates into one PR.
   ```json5
   { "matchUpdateTypes": ["minor", "patch"], "groupName": "all non-major" }
   ```
   Built-in groups exist: `group:allNonMajor`, `group:monorepos` (in `config:recommended`), `group:linters`, etc.
2. **Schedule** so PRs land in a predictable window:
   ```json5
   { "schedule": ["before 6am on monday"], "timezone": "Europe/Berlin" }
   ```
   Or use presets: `schedule:weekly`, `schedule:nonOfficeHours`, `schedule:monthly`.
3. **Limit** throughput: `prHourlyLimit`, `prConcurrentLimit`.
4. **Automerge** the boring stuff (§5) so it never reaches a human.
5. **Lockfile maintenance** on a schedule instead of per-dep: `:maintainLockFilesWeekly`.
6. **Dependency Dashboard approval** for noisy categories — PRs are only created after you tick a box:
   ```json5
   { "matchUpdateTypes": ["major"], "dependencyDashboardApproval": true }
   ```

- **DON'T** over-group — a single mega-PR mixing 40 unrelated bumps is unbisectable when CI breaks. Group by risk/relatedness, not "everything."

---

## 9. Dependency Dashboard

A tracking issue listing pending/open/errored updates. On by default in `config:recommended`.

- **DO** keep it on — it's your control panel: rate-limited updates, approval gating, and error visibility live here.
- Gate updates behind it with `dependencyDashboardApproval: true` (globally, per `major`, or per `packageRules` entry) when you want a human to opt in before PRs appear.

---

## 10. Copy-paste recipes

**Disable a dependency entirely:**
```json5
{ "matchPackageNames": ["legacy-lib"], "enabled": false }
```

**Pin a dependency to a major (block majors):**
```json5
{ "matchPackageNames": ["express"], "allowedVersions": "<5.0.0" }
```

**Separate major updates from minor/patch (default in recommended), but group all minors:**
```json5
{ "matchUpdateTypes": ["minor", "patch"], "groupName": "minor & patch" }
```

**Only run specific managers:**
```json5
{ "enabledManagers": ["npm", "dockerfile", "github-actions"] }
```

**Different policy per monorepo package:**
```json5
{ "matchFileNames": ["apps/web/**"], "schedule": ["before 6am on monday"] }
```

**Ignore a path (e.g. test fixtures, vendored code):**
```json5
{ "ignorePaths": ["**/fixtures/**", "**/__tests__/**"] }
```

---

## 11. Dos & Don'ts

**Do**
- Start from `config:recommended` / `config:best-practices`; override sparingly.
- Add `$schema`; validate config in CI (`npx --yes renovate-config-validator`).
- Centralize org defaults in a shared preset repo, pinned to a tag.
- Keep `configMigration: true` so deprecated keys auto-update.
- Gate automerge on green CI + `minimumReleaseAge`.
- Pin Docker digests and Action SHAs.
- Order `packageRules` broad → specific.

**Don't**
- Hand-write huge configs from blog posts.
- Automerge majors, or automerge anything without required status checks.
- Use deprecated matchers (`matchPackagePatterns`, `matchPackagePrefixes`) in new config.
- `pin` libraries you publish.
- Create one giant catch-all PR group.
- Maintain multiple Renovate config files in one repo.

---

## 12. Validate & debug

- **Lint before commit:**
  ```bash
  npx --yes --package renovate -- renovate-config-validator
  ```
  Wire it into CI so a broken `renovate.json` fails the build, not the bot.
- **Dry-run to see what Renovate *would* do** (self-hosted):
  ```bash
  LOG_LEVEL=debug renovate --dry-run --platform=local
  ```
- **Inspect matching:** debug logs print each dependency's `depName`, `packageName`, and `datasource` — use these exact values when writing matchers.
- **`extends` not applying?** Remember presets are lower precedence than local keys; a local key silently overrides the preset.
- **Rule not firing?** Check the AND-across-matcher-types rule (§4) and rule ordering (later wins).

---

### References
- Configuration options: https://docs.renovatebot.com/configuration-options/
- Full config presets: https://docs.renovatebot.com/presets-config/
- Upgrade best practices: https://docs.renovatebot.com/upgrade-best-practices/
- Key concepts — presets: https://docs.renovatebot.com/key-concepts/presets/
