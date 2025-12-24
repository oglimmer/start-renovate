# Renovate Initializr

A small web app that helps you create a clear, best‑practice `renovate.json` without reading the entire Renovate docs. Pick your preferences, preview the JSON live, and download the file for your repo.

**Why**: Renovate is powerful but configuration can feel overwhelming. This tool gives teams a sensible starting point with opinionated defaults and explainers for common options.

## What It Does

- Builds a `renovate.json` from guided choices with live preview
- Starts from safe defaults: `config:recommended`, vulnerability alerts, dependency dashboard
- Adds semantic commits, timezone, schedules, PR limits, and rebase strategy
- Sets automerge policies (level, type) with sensible safeguards
- Groups updates by package manager to reduce PR noise
- Generates a downloadable `renovate.json` to commit in your repo root

## Quick Start

1) Install dependencies

```
npm install
```

2) Run the app locally

```
npm run dev
```

3) Open `http://localhost:3000`, choose options, click “Generate renovate.json”, and commit the downloaded file to your repository root.

## Configuration Defaults

The generator includes these by default:

- **Extends**: `config:recommended`, `:enableVulnerabilityAlerts`, `:dependencyDashboard`
- **Semantic Commits**: enabled by default (toggleable)
- **Timezone**: configurable so schedules align to your local time
- **Schedules**: presets for weekly, monthly, weekends, or outside business hours
- **PR Limits**: pick conservative, default, or active limits to control PR volume
- **Rebasing**: choose when Renovate should rebase update branches
- **Version Ranges**: control range strategy (auto, bump, replace, pin, update-lockfile)
- **Automerge**: set level (disabled/patch/minor/all) and type (PR/branch/PR comment)
- **Safeguards**: optional rule to avoid automerging `0.x` updates
- **Lock File Maintenance**: enable + schedule + optional automerge
- **Vulnerability Alerts**: labels, schedule override, and automerge options
- **Grouping**: group updates by package manager (npm, Docker, Maven, Gradle, pip, Composer, Helm, GitHub Actions, Terraform, Go, Cargo, Bundler, NuGet)

All selections are reflected in the live preview and exported JSON.

## Typical Workflow

- Start with the defaults, then adjust schedule/timezone to match your team
- Pick a PR limit strategy that suits your review capacity and CI
- Enable automerge for low‑risk updates (e.g., `patch` or `minor`) and consider a safeguard for pre‑1.0.0
- Group by package manager to dramatically reduce PR noise
- Download and commit `renovate.json`, then let Renovate open its onboarding PR

## Who This Is For

- Teams new to Renovate who want a safe starting point
- Experienced users who want to standardize Renovate across repos
- Anyone who prefers an interactive, documented way to build `renovate.json`

## Non‑Goals

- Not a complete replacement for the Renovate documentation
- Does not cover every advanced edge case or manager‑specific knob

## Contributing

Issues and PRs welcome. If you see confusing defaults or missing options that many teams would benefit from, please open a discussion.
