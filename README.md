# Renovate Initializr

A full‑stack web application that helps you create a clear, best‑practice `renovate.json` without reading the entire Renovate docs. Pick your preferences, preview the JSON live, get AI‑powered feedback on your configuration, and download the file for your repo.

**Why**: Renovate is powerful but configuration can feel overwhelming. This tool gives teams a sensible starting point with opinionated defaults, explainers for common options, and intelligent validation powered by OpenAI.

## What It Does

- Builds a `renovate.json` from guided choices with live preview
- Provides AI‑powered feedback to validate and improve your configuration
- Starts from safe defaults: `config:recommended`, vulnerability alerts, dependency dashboard
- Adds semantic commits, timezone, schedules, PR limits, and rebase strategy
- Sets automerge policies (level, type) with sensible safeguards
- Groups updates by package manager to reduce PR noise
- Generates a downloadable `renovate.json` to commit in your repo root

## Architecture

- **Backend**: Spring Boot 4.0 (Java 21) REST API with OpenAI integration for configuration feedback
- **Frontend**: Nuxt.js web application for interactive configuration building
- **Deployment**: Helm chart for Kubernetes deployment with ingress, sealed secrets, and health monitoring

## Quick Start

### Local Development

#### Frontend Only (without AI feedback)

1) Install dependencies

```bash
cd frontend
npm install
```

2) Run the frontend

```bash
npm run dev
```

3) Open `http://localhost:3000`, choose options, and download your `renovate.json`

#### Full Stack (with AI feedback)

1) Set up the backend

```bash
cd backend

# Configure OpenAI API key
export OPENAI_API_KEY="sk-your-openai-api-key-here"

# Run the backend
./mvnw spring-boot:run
```

The backend will start on `http://localhost:8080`

2) Set up the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:3000` and connect to the backend at `http://localhost:8080`

3) Open `http://localhost:3000`, choose options, get AI feedback, and download your `renovate.json`

### Kubernetes Deployment

Deploy to your Kubernetes cluster using Helm:

```bash
# Create and apply sealed secret for OpenAI API key
# (See helm/README.md for detailed instructions)

# Install the Helm chart
cd helm
helm install start-renovate ./start-renovate

# Or with custom values
helm install start-renovate ./start-renovate -f my-values.yaml
```

See [helm/README.md](helm/README.md) for complete deployment documentation.

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
- Use the AI feedback feature to validate your configuration and get improvement suggestions
- Download and commit `renovate.json`, then let Renovate open its onboarding PR

## Who This Is For

- Teams new to Renovate who want a safe starting point
- Experienced users who want to standardize Renovate across repos
- Anyone who prefers an interactive, documented way to build `renovate.json`
- DevOps teams looking to deploy a self‑hosted configuration generator

## Prerequisites

### For Local Development
- **Frontend**: Node.js 18+ and npm
- **Backend**: Java 21+ and Maven
- **AI Feedback**: OpenAI API key (optional, only needed for feedback feature)

### For Kubernetes Deployment
- Kubernetes 1.19+
- Helm 3.0+
- Sealed Secrets controller (for storing OpenAI API key)
- Container registry access

## Technical Stack

- **Backend**: Spring Boot 4.0, Java 21, Spring WebFlux, OpenAI Java SDK
- **Frontend**: Nuxt.js, Vue.js
- **Deployment**: Kubernetes, Helm, Sealed Secrets
- **Build Tools**: Maven (backend), npm (frontend)
- **Code Quality**: Spotless (backend), ESLint (frontend)

## Non‑Goals

- Not a complete replacement for the Renovate documentation
- Does not cover every advanced edge case or manager‑specific knob

## Contributing

Issues and PRs welcome. If you see confusing defaults or missing options that many teams would benefit from, please open a discussion.
