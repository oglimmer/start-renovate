<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-5xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <NuxtLink to="/" class="inline-flex items-center text-sm text-indigo-600 hover:text-indigo-800 mb-4">
          <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Back to the configurator
        </NuxtLink>
        <h1 class="text-4xl font-bold text-gray-900 mb-2">Developer API</h1>
        <p class="text-gray-600 max-w-2xl">
          The same AI-powered configuration review that drives the
          <NuxtLink to="/" class="text-indigo-600 hover:text-indigo-800 underline">Renovate Initializr</NuxtLink>
          UI is exposed as a small, public REST API. Send it a <code class="bg-white px-1.5 py-0.5 rounded text-sm">renovate.json</code>
          and get back a plain-language summary, a list of concrete issues, and an improved configuration.
        </p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-[200px_1fr] gap-8">
        <!-- Table of contents -->
        <aside class="hidden lg:block">
          <nav class="sticky top-8 space-y-1 text-sm">
            <p class="font-semibold text-gray-900 mb-2">On this page</p>
            <a
              v-for="item in toc"
              :key="item.id"
              :href="`#${item.id}`"
              class="block py-1 text-gray-600 hover:text-indigo-600 transition-colors"
            >
              {{ item.label }}
            </a>
          </nav>
        </aside>

        <!-- Content -->
        <div class="min-w-0 space-y-6">
          <!-- Overview -->
          <section id="overview" class="bg-white shadow-xl rounded-lg p-8 scroll-mt-8">
            <h2 class="text-2xl font-semibold text-gray-900 mb-4">Overview</h2>
            <p class="text-gray-700 mb-4">
              The API has a single job: <strong>analyse a Renovate configuration and tell you how to make it better.</strong>
              Under the hood your config is first validated against the official Renovate JSON schema, then handed to an
              DeepSeek model that returns structured, actionable feedback. It's the right tool when you want to lint and
              improve <code class="bg-gray-100 px-1 rounded">renovate.json</code> files programmatically — in CI, a bot, or
              your own developer tooling.
            </p>

            <h3 class="text-sm font-bold text-gray-900 uppercase tracking-wide mb-2 mt-6">Base URL</h3>
            <div class="space-y-2 text-sm">
              <div class="flex items-baseline gap-3">
                <span class="inline-block w-24 text-gray-500">Production</span>
                <code class="bg-gray-100 px-2 py-1 rounded text-gray-800">https://renovate.oglimmer.com/api</code>
              </div>
              <div class="flex items-baseline gap-3">
                <span class="inline-block w-24 text-gray-500">Local dev</span>
                <code class="bg-gray-100 px-2 py-1 rounded text-gray-800">http://localhost:8080/api</code>
              </div>
            </div>

            <div class="mt-6 grid sm:grid-cols-2 gap-4 text-sm">
              <div class="bg-gray-50 rounded p-4">
                <p class="font-semibold text-gray-900 mb-1">Conventions</p>
                <ul class="list-disc ml-5 space-y-1 text-gray-700">
                  <li>All requests and responses are <code class="bg-white px-1 rounded">application/json</code>.</li>
                  <li>UTF-8 encoded, no trailing slash on paths.</li>
                  <li>The <code class="bg-white px-1 rounded">/api</code> prefix is the server context path.</li>
                </ul>
              </div>
              <div class="bg-gray-50 rounded p-4">
                <p class="font-semibold text-gray-900 mb-1">Auth &amp; CORS</p>
                <ul class="list-disc ml-5 space-y-1 text-gray-700">
                  <li><strong>No authentication</strong> is required — the endpoint is open.</li>
                  <li>CORS is restricted to <code class="bg-white px-1 rounded">https://renovate.oglimmer.com</code>, so browser calls from other origins are blocked. Call it server-to-server instead.</li>
                  <li><strong>Rate limited</strong> to 1 request per minute per IP — extra calls get <code class="bg-white px-1 rounded">429 Too Many Requests</code>.</li>
                  <li>Be a good citizen: each call costs an LLM round-trip.</li>
                </ul>
              </div>
            </div>
          </section>

          <!-- Endpoint -->
          <section id="feedback" class="bg-white shadow-xl rounded-lg p-8 scroll-mt-8">
            <div class="flex items-center gap-3 mb-1 flex-wrap">
              <span class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-green-100 text-green-800">POST</span>
              <code class="text-lg font-mono text-gray-900">/api/feedback</code>
            </div>
            <p class="text-gray-700 mt-3 mb-4">
              Submit a Renovate configuration and receive AI feedback on it.
            </p>

            <div class="bg-blue-50 border-l-4 border-blue-500 p-4 rounded mb-6 text-sm text-blue-900">
              <p class="mb-2"><strong>What it does:</strong> validates your config against the Renovate JSON schema,
                then asks an LLM to review it for best practices, security, and PR hygiene.</p>
              <p><strong>Why you'd use it:</strong> to catch misconfigurations and get an opinionated, improved config
                without reading the entire Renovate documentation — automatable from CI or your own tools.</p>
            </div>

            <h3 class="text-sm font-bold text-gray-900 uppercase tracking-wide mb-3">How a request flows</h3>
            <ol class="list-decimal ml-5 space-y-1 text-sm text-gray-700 mb-6">
              <li>The <code class="bg-gray-100 px-1 rounded">renovateJson</code> string is parsed and validated against the Renovate schema (draft-07).</li>
              <li>If invalid, you get <code class="bg-gray-100 px-1 rounded">400 Bad Request</code> immediately — no LLM call is made.</li>
              <li>If valid, the config is sent to the model, which returns a structured summary, issues, and an improved config.</li>
            </ol>

            <!-- Request body -->
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Request body</h3>
            <p class="text-sm text-gray-600 mb-3">
              <code class="bg-gray-100 px-1 rounded">RenovateFeedbackRequest</code>
            </p>
            <div class="overflow-x-auto mb-3">
              <table class="min-w-full text-sm border border-gray-200 rounded-lg">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Field</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Type</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Required</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Description</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td class="px-4 py-2 border-b border-gray-100"><code class="text-indigo-700">renovateJson</code></td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-600">string</td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-600">Yes</td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-700">
                      Your Renovate config, serialised as a JSON <em>string</em> (i.e. the file contents),
                      not a nested JSON object.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="bg-yellow-50 border-l-4 border-yellow-400 p-3 rounded text-sm text-yellow-900 mb-6">
              <strong>Heads up:</strong> <code class="bg-white px-1 rounded">renovateJson</code> is a <em>string field</em>.
              Stringify your config and place it inside the request object — see the examples below, which double-encode it.
            </div>

            <!-- Examples -->
            <h3 class="text-lg font-semibold text-gray-900 mb-3">Example request</h3>

            <p class="text-sm font-medium text-gray-700 mb-2">JavaScript (fetch)</p>
            <CodeBlock :code="jsExample" lang="js" copy-key="js" :copied="copied" @copy="copy" />

            <p class="text-sm font-medium text-gray-700 mb-2 mt-5">cURL</p>
            <CodeBlock :code="curlExample" lang="bash" copy-key="curl" :copied="copied" @copy="copy" />

            <!-- Response -->
            <h3 class="text-lg font-semibold text-gray-900 mb-2 mt-8">Response <span class="text-sm font-normal text-gray-500">— 200 OK</span></h3>
            <p class="text-sm text-gray-600 mb-3">
              <code class="bg-gray-100 px-1 rounded">RenovateFeedbackResponse</code>
            </p>
            <CodeBlock :code="responseExample" lang="json" copy-key="resp" :copied="copied" @copy="copy" />

            <!-- Status codes -->
            <h3 class="text-lg font-semibold text-gray-900 mb-3 mt-8">Status codes</h3>
            <div class="overflow-x-auto">
              <table class="min-w-full text-sm border border-gray-200 rounded-lg">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Code</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Meaning</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in statusCodes" :key="row.code">
                    <td class="px-4 py-2 border-b border-gray-100 align-top">
                      <code :class="row.code.startsWith('2') ? 'text-green-700' : 'text-red-700'">{{ row.code }}</code>
                    </td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-700">{{ row.meaning }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <div class="bg-amber-50 border-l-4 border-amber-400 p-3 rounded text-sm text-amber-900 mt-4">
              <strong>Graceful degradation:</strong> if the upstream model call itself fails (timeout, quota, etc.),
              the API still returns <code class="bg-white px-1 rounded">200 OK</code> with a <code class="bg-white px-1 rounded">summary</code>
              describing the failure, an empty <code class="bg-white px-1 rounded">issues</code> array, and your original config
              echoed back in <code class="bg-white px-1 rounded">improvedRenovateJson</code>. Check
              <code class="bg-white px-1 rounded">issues.length</code> and the summary text rather than relying on the status code alone.
            </div>
          </section>

          <!-- Generate (client-side pseudo API) -->
          <section id="generate" class="bg-white shadow-xl rounded-lg p-8 scroll-mt-8">
            <div class="flex items-center gap-3 mb-1 flex-wrap">
              <span class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-bold bg-sky-100 text-sky-800">GET</span>
              <code class="text-lg font-mono text-gray-900">/generate</code>
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-600">runs in the browser</span>
            </div>
            <p class="text-gray-700 mt-3 mb-4">
              Build a <code class="bg-gray-100 px-1 rounded">renovate.json</code> straight from URL query parameters —
              the form, without the form.
            </p>

            <div class="bg-blue-50 border-l-4 border-blue-500 p-4 rounded mb-6 text-sm text-blue-900">
              <p class="mb-2"><strong>What it does:</strong> maps query parameters onto the default configuration and
                returns the generated JSON. Open it in a browser, link to it, or fetch it and read
                <code class="bg-white px-1 rounded">response.text()</code>.</p>
              <p class="mb-2"><strong>Why it exists:</strong> it shares the <em>exact same generation logic</em> as the
                interactive form — there is a single source of truth, so the two can never drift apart.</p>
              <p><strong>Note:</strong> unlike <code class="bg-white px-1 rounded">/api/feedback</code>, this is not a
                server endpoint. It's a statically-hosted page that computes the result client-side, so there is no AI,
                no validation step, and no rate limit.</p>
            </div>

            <h3 class="text-sm font-bold text-gray-900 uppercase tracking-wide mb-3">Rules</h3>
            <ul class="list-disc ml-5 space-y-1 text-sm text-gray-700 mb-6">
              <li>Any parameter you omit keeps its default value (see the table below).</li>
              <li>Nested options use <strong>dot notation</strong>, e.g. <code class="bg-gray-100 px-1 rounded">grouping.npm=true</code>.</li>
              <li>Booleans accept <code class="bg-gray-100 px-1 rounded">true</code>/<code class="bg-gray-100 px-1 rounded">1</code>/<code class="bg-gray-100 px-1 rounded">yes</code>/<code class="bg-gray-100 px-1 rounded">on</code>; anything else is <code class="bg-gray-100 px-1 rounded">false</code>.</li>
              <li>Unknown parameters are ignored.</li>
            </ul>

            <h3 class="text-lg font-semibold text-gray-900 mb-3">Example request</h3>
            <CodeBlock :code="generateUrlExample" lang="url" copy-key="genurl" :copied="copied" @copy="copy" />

            <h3 class="text-lg font-semibold text-gray-900 mb-2 mt-6">Response</h3>
            <p class="text-sm text-gray-600 mb-3">The generated <code class="bg-gray-100 px-1 rounded">renovate.json</code> (omitted parameters fall back to defaults):</p>
            <CodeBlock :code="generateOutputExample" lang="json" copy-key="genout" :copied="copied" @copy="copy" />

            <h3 class="text-lg font-semibold text-gray-900 mb-3 mt-8">Parameters</h3>
            <div class="overflow-x-auto">
              <table class="min-w-full text-sm border border-gray-200 rounded-lg">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Parameter</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Accepts</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Default</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="p in generateParams" :key="p.param">
                    <td class="px-4 py-2 border-b border-gray-100 align-top"><code class="text-indigo-700 whitespace-nowrap">{{ p.param }}</code></td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-700">{{ p.accepts }}</td>
                    <td class="px-4 py-2 border-b border-gray-100 align-top text-gray-600 whitespace-nowrap"><code>{{ p.def }}</code></td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <!-- DTO reference -->
          <section id="dtos" class="bg-white shadow-xl rounded-lg p-8 scroll-mt-8">
            <h2 class="text-2xl font-semibold text-gray-900 mb-4">Data types (DTOs)</h2>

            <!-- RenovateFeedbackResponse -->
            <h3 class="text-lg font-semibold text-gray-900 mb-2">RenovateFeedbackResponse</h3>
            <p class="text-sm text-gray-600 mb-3">The top-level response object.</p>
            <div class="overflow-x-auto mb-8">
              <table class="min-w-full text-sm border border-gray-200 rounded-lg">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Field</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Type</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Description</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="f in responseFields" :key="f.name">
                    <td class="px-4 py-2 border-b border-gray-100 align-top"><code class="text-indigo-700">{{ f.name }}</code></td>
                    <td class="px-4 py-2 border-b border-gray-100 align-top text-gray-600 whitespace-nowrap">{{ f.type }}</td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-700">{{ f.desc }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Issue -->
            <h3 class="text-lg font-semibold text-gray-900 mb-2">Issue</h3>
            <p class="text-sm text-gray-600 mb-3">A single finding inside <code class="bg-gray-100 px-1 rounded">issues[]</code>.</p>
            <div class="overflow-x-auto mb-4">
              <table class="min-w-full text-sm border border-gray-200 rounded-lg">
                <thead class="bg-gray-50">
                  <tr>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Field</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Type</th>
                    <th class="text-left font-semibold text-gray-700 px-4 py-2 border-b border-gray-200">Description</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="f in issueFields" :key="f.name">
                    <td class="px-4 py-2 border-b border-gray-100 align-top"><code class="text-indigo-700">{{ f.name }}</code></td>
                    <td class="px-4 py-2 border-b border-gray-100 align-top text-gray-600 whitespace-nowrap">{{ f.type }}</td>
                    <td class="px-4 py-2 border-b border-gray-100 text-gray-700">{{ f.desc }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="text-sm text-gray-700">
              <code class="bg-gray-100 px-1 rounded">severity</code> is one of:
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 mx-0.5">info</span>
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-yellow-100 text-yellow-800 mx-0.5">warning</span>
              <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800 mx-0.5">error</span>
            </p>
          </section>

          <!-- Footer -->
          <p class="text-center text-sm text-gray-500 pt-2">
            Questions or an integration to share?
            <a
              href="https://github.com/oglimmer/start-renovate"
              target="_blank"
              rel="noopener noreferrer"
              class="text-indigo-600 hover:text-indigo-800 underline"
            >Open an issue on GitHub</a>.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
useHead({
  title: 'Developer API — Renovate Initializr',
  meta: [
    { name: 'description', content: 'REST API reference for the Renovate Initializr AI configuration feedback endpoint, with request/response examples and DTOs.' }
  ]
})

const toc = [
  { id: 'overview', label: 'Overview' },
  { id: 'feedback', label: 'POST /feedback' },
  { id: 'generate', label: 'GET /generate' },
  { id: 'dtos', label: 'Data types' }
]

// Parameters accepted by the GET /generate pseudo-endpoint. Mirrors RenovateConfig.
const generateParams = [
  { param: 'semanticCommits', accepts: 'boolean', def: 'true' },
  { param: 'timezone', accepts: 'IANA timezone, e.g. UTC', def: 'Europe/Berlin' },
  { param: 'schedule', accepts: 'at-any-time | weekly | monthly | weekends | outside-business-hours', def: 'at-any-time' },
  { param: 'prLimitStrategy', accepts: 'default | conservative | active', def: 'active' },
  { param: 'rebaseWhen', accepts: 'auto | never | conflicted | behind-base-branch', def: 'behind-base-branch' },
  { param: 'rangeStrategy', accepts: 'auto | pin | bump | replace | widen | update-lockfile', def: 'bump' },
  { param: 'automergeType', accepts: 'pr | branch | pr-comment', def: 'branch' },
  { param: 'automergeLevel', accepts: 'disabled | patch | minor | all', def: 'minor' },
  { param: 'automergeDevDependencies', accepts: 'boolean', def: 'false' },
  { param: 'ignoreTests', accepts: 'boolean', def: 'false' },
  { param: 'disablePreOneAutomerge', accepts: 'boolean', def: 'true' },
  { param: 'requireMajorApproval', accepts: 'boolean', def: 'true' },
  { param: 'minimumReleaseAge', accepts: 'never | 3-days | 7-days | 14-days', def: '7-days' },
  { param: 'pinning.dockerDigests', accepts: 'boolean', def: 'true' },
  { param: 'pinning.githubActionDigests', accepts: 'boolean', def: 'true' },
  { param: 'pinning.devDependencies', accepts: 'boolean', def: 'true' },
  { param: 'flagAbandonedPackages', accepts: 'boolean', def: 'true' },
  { param: 'lockFileMaintenance.enabled', accepts: 'boolean', def: 'true' },
  { param: 'lockFileMaintenance.schedule', accepts: 'same values as schedule', def: 'weekly' },
  { param: 'lockFileMaintenance.automerge', accepts: 'boolean', def: 'true' },
  { param: 'vulnerabilityAlerts.labels', accepts: 'comma-separated string', def: 'security' },
  { param: 'vulnerabilityAlerts.scheduleOverride', accepts: 'boolean', def: 'true' },
  { param: 'vulnerabilityAlerts.automerge', accepts: 'boolean', def: 'true' },
  { param: 'grouping.<manager>', accepts: 'boolean — manager is one of: npm, docker, maven, gradle, pip, composer, helm, githubActions, terraform, gomod, cargo, bundler, nuget', def: 'false' }
]

const generateUrlExample = 'https://renovate.oglimmer.com/generate'
  + '?schedule=weekly'
  + '&prLimitStrategy=conservative'
  + '&grouping.npm=true'
  + '&grouping.docker=true'

const generateOutputExample = `{
  "$schema": "https://docs.renovatebot.com/renovate-schema.json",
  "extends": [
    "config:recommended",
    ":enableVulnerabilityAlerts",
    ":dependencyDashboard",
    ":semanticCommits",
    "docker:pinDigests",
    "helpers:pinGitHubActionDigests",
    ":pinDevDependencies",
    "abandonments:recommended"
  ],
  "configMigration": true,
  "timezone": "Europe/Berlin",
  "schedule": ["before 5am on monday"],
  "prHourlyLimit": 1,
  "prConcurrentLimit": 3,
  "rebaseWhen": "behind-base-branch",
  "rangeStrategy": "bump",
  "automergeType": "branch",
  "packageRules": [
    { "automerge": true, "matchUpdateTypes": ["minor", "patch", "pin", "digest"] },
    { "matchCurrentVersion": "/^0\\\\./", "automerge": false },
    { "matchUpdateTypes": ["major"], "dependencyDashboardApproval": true },
    { "matchManagers": ["npm"], "groupName": "npm dependencies" },
    { "matchManagers": ["dockerfile", "docker-compose"], "groupName": "Docker dependencies" }
  ],
  "minimumReleaseAge": "7 days",
  "internalChecksFilter": "strict",
  "lockFileMaintenance": {
    "enabled": true,
    "schedule": ["before 5am on monday"],
    "automerge": true
  },
  "vulnerabilityAlerts": {
    "minimumReleaseAge": "0 days",
    "labels": ["security"],
    "schedule": ["at any time"],
    "automerge": true
  }
}`

const statusCodes = [
  { code: '200 OK', meaning: 'Feedback generated successfully (see the graceful-degradation note below).' },
  { code: '400 Bad Request', meaning: 'The renovateJson is not valid JSON, or fails Renovate schema validation. The error message names the offending fields.' },
  { code: '429 Too Many Requests', meaning: 'Rate limit exceeded — the endpoint allows 1 request per minute per IP. Wait and retry.' }
]

const responseFields = [
  { name: 'summary', type: 'string', desc: 'A 1-2 sentence plain-language assessment of the configuration and its quality.' },
  { name: 'issues', type: 'Issue[]', desc: 'Up to 8 concrete findings. Empty when nothing noteworthy was found (or the model call failed).' },
  { name: 'improvedRenovateJson', type: 'string', desc: 'A corrected/optimised version of your config as a JSON string. Falls back to your original input if generation fails.' }
]

const issueFields = [
  { name: 'severity', type: 'string', desc: 'Severity level: "info", "warning", or "error".' },
  { name: 'jsonPath', type: 'string', desc: 'JSONPath pointing at the relevant part of the config, e.g. "$.prConcurrentLimit".' },
  { name: 'message', type: 'string', desc: 'What the issue is.' },
  { name: 'suggestion', type: 'string', desc: 'A concrete recommendation for how to fix it.' }
]

const jsExample = `// renovateJson is a *string* — stringify your config, then the request object.
const renovateConfig = {
  $schema: 'https://docs.renovatebot.com/renovate-schema.json',
  extends: ['config:recommended', ':enableVulnerabilityAlerts'],
  timezone: 'Europe/Berlin'
}

const res = await fetch('https://renovate.oglimmer.com/api/feedback', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ renovateJson: JSON.stringify(renovateConfig) })
})

if (!res.ok) throw new Error(\`Validation failed: \${res.status}\`)

const feedback = await res.json()
console.log(feedback.summary)
feedback.issues.forEach(i => console.log(\`[\${i.severity}] \${i.jsonPath}: \${i.message}\`))`

const curlExample = `curl -X POST https://renovate.oglimmer.com/api/feedback \\
  -H "Content-Type: application/json" \\
  -d '{
    "renovateJson": "{\\"extends\\":[\\"config:recommended\\"],\\"timezone\\":\\"Europe/Berlin\\"}"
  }'`

const responseExample = `{
  "summary": "Solid baseline using recommended presets. A couple of tweaks would improve PR hygiene.",
  "issues": [
    {
      "severity": "warning",
      "jsonPath": "$.prConcurrentLimit",
      "message": "No concurrency limit set; Renovate may open many PRs at once.",
      "suggestion": "Add \\"prConcurrentLimit\\": 10 to cap simultaneous open PRs."
    },
    {
      "severity": "info",
      "jsonPath": "$.timezone",
      "message": "Timezone is set but no schedule is defined, so it has no effect.",
      "suggestion": "Add a \\"schedule\\", e.g. [\\"before 5am on monday\\"]."
    }
  ],
  "improvedRenovateJson": "{\\n  \\"extends\\": [\\"config:recommended\\", \\":enableVulnerabilityAlerts\\"],\\n  \\"timezone\\": \\"Europe/Berlin\\",\\n  \\"prConcurrentLimit\\": 10\\n}"
}`

const copied = ref<string | null>(null)
let copyTimer: ReturnType<typeof setTimeout> | undefined

const copy = async (key: string, code: string) => {
  try {
    await navigator.clipboard.writeText(code)
    copied.value = key
    if (copyTimer) clearTimeout(copyTimer)
    copyTimer = setTimeout(() => { copied.value = null }, 2000)
  } catch {
    // Clipboard unavailable (e.g. non-secure context) — silently ignore.
  }
}
</script>
