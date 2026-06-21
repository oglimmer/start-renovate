<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-3xl mx-auto">
      <div class="text-center mb-8">
        <h1 class="text-4xl font-bold text-gray-900 mb-2">Renovate Initializr</h1>
        <p class="text-gray-600">Create your renovate.json configuration with ease</p>
        <p class="text-sm text-gray-500 mt-2">
          New to Renovate?
          <a href="https://docs.renovatebot.com/" target="_blank" rel="noopener noreferrer" class="text-indigo-600 hover:text-indigo-800 underline">
            Check out the official documentation
          </a>
        </p>
        <p class="text-sm text-gray-500 mt-1">
          Building an integration?
          <NuxtLink to="/developers" class="text-indigo-600 hover:text-indigo-800 underline">
            Read the REST API docs
          </NuxtLink>
        </p>
        <p class="text-sm text-gray-500 mt-1">
          Managing several repos?
          <NuxtLink to="/dashboard" class="text-indigo-600 hover:text-indigo-800 underline">
            Compare their config on the dashboard
          </NuxtLink>
        </p>
      </div>

      <div class="bg-white shadow-xl rounded-lg p-8 mb-6">
        <div class="mb-6">
          <div class="bg-blue-50 border-l-4 border-blue-500 p-4 mb-6">
            <div class="flex">
              <div class="flex-shrink-0">
                <svg class="h-5 w-5 text-blue-500 mt-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3 flex-1">
                <h3 class="text-sm font-bold text-blue-900 mb-3">Default Configuration Included</h3>
                <div class="space-y-3 text-sm text-blue-800">
                  <div>
                    <p class="mb-1">
                      <strong class="text-blue-900">What it does:</strong> Your renovate.json will automatically include these baseline presets:
                    </p>
                    <ul class="list-disc ml-5 space-y-1">
                      <li>
                        <code class="bg-white px-1.5 py-0.5 rounded text-xs">config:recommended</code> - Sensible defaults for most projects: pin GitHub Actions to digests, group monorepo packages, use semantic commit messages for major updates, and more.
                      </li>
                      <li>
                        <code class="bg-white px-1.5 py-0.5 rounded text-xs">:enableVulnerabilityAlerts</code> - Automatically creates PRs for security vulnerabilities detected by your platform's security advisories (GitHub Dependabot, GitLab, etc.).
                      </li>
                      <li>
                        <code class="bg-white px-1.5 py-0.5 rounded text-xs">:dependencyDashboard</code> - Creates a special issue that lists all pending updates, rate-limited updates, and provides manual approval controls.
                      </li>
                    </ul>
                  </div>
                  <div>
                    <p class="mb-1">
                      <strong class="text-blue-900">Why you want this:</strong>
                    </p>
                    <ul class="list-disc ml-5 space-y-1">
                      <li>Provides battle-tested defaults refined by thousands of users</li>
                      <li>Ensures security vulnerabilities are addressed immediately with dedicated PRs</li>
                      <li>Gives you a centralized dashboard to see all dependency updates at a glance</li>
                      <li>Prevents common pitfalls and follows best practices out of the box</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Notice shown after loading a repo's config from the dashboard -->
          <div v-if="importedFromRepo" class="mb-4 p-3 bg-green-50 border border-green-200 rounded-lg flex items-start justify-between gap-3">
            <p class="text-sm text-green-700">
              Loaded the Renovate configuration from <span class="font-medium">{{ importedFromRepo }}</span> into the editor.
            </p>
            <button class="shrink-0 text-green-600 hover:text-green-800" aria-label="Dismiss" @click="importedFromRepo = ''">
              <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <!-- Preset Selector -->
          <div class="border border-indigo-200 bg-indigo-50 rounded-lg p-6 mb-6">
            <label for="preset" class="text-lg font-medium text-gray-900 block mb-2">
              Start from a preset
            </label>
            <select
              id="preset"
              v-model="selectedPreset"
              class="w-full px-4 py-2 border border-gray-300 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
              @change="applyPreset"
            >
              <option v-for="preset in configPresets" :key="preset.id" :value="preset.id">
                {{ preset.label }}
              </option>
            </select>
            <p class="text-sm text-gray-600 mt-2">
              {{ activePreset?.description }}
            </p>
            <p class="text-xs text-gray-500 mt-2">
              Picking a preset replaces every option below with that preset's values. You can still tweak any
              option afterwards.
            </p>
          </div>

          <div class="flex justify-between items-center mb-4">
            <h2 class="text-2xl font-semibold text-gray-900">Configuration Options</h2>
            <button
              class="px-4 py-2 text-sm font-medium text-indigo-600 bg-indigo-50 rounded-lg hover:bg-indigo-100 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors"
              @click="showImportModal = true"
            >
              Import Configuration
            </button>
          </div>

          <!-- Semantic Commits Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div class="flex items-start">
              <input
                id="semanticCommits"
                v-model="config.semanticCommits"
                type="checkbox"
                class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
              >
              <div class="ml-4 flex-1">
                <label for="semanticCommits" class="text-lg font-medium text-gray-900 cursor-pointer">
                  Enable Semantic Commits
                </label>
                <div class="mt-2 text-sm text-gray-600">
                  <p class="mb-2">
                    <strong class="text-gray-700">What it does:</strong> Formats all Renovate commit messages and PR titles
                    according to the Conventional Commits specification (e.g., <code class="bg-gray-100 px-1 rounded">fix(deps):</code>,
                    <code class="bg-gray-100 px-1 rounded">chore(deps):</code>).
                  </p>
                  <p class="mb-2">
                    <strong class="text-gray-700">Why you want this:</strong>
                  </p>
                  <ul class="list-disc ml-5 space-y-1">
                    <li>Maintains consistency with semantic commit conventions in your repository</li>
                    <li>Enables automatic changelog generation and semantic versioning tools</li>
                    <li>Uses <code class="bg-gray-100 px-1 rounded">fix:</code> prefix for production dependencies and <code class="bg-gray-100 px-1 rounded">chore:</code> for development dependencies</li>
                    <li>Improves integration with tools like semantic-release and conventional-changelog</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          <!-- Timezone Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label for="timezone" class="text-lg font-medium text-gray-900 block mb-2">
                Timezone
              </label>
              <select
                id="timezone"
                v-model="config.timezone"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
              >
                <option value="">Default (UTC)</option>
                <option v-for="tz in timezones" :key="tz.value" :value="tz.value">
                  {{ tz.label }}
                </option>
              </select>
              <div class="mt-3 text-sm text-gray-600">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Sets the timezone for Renovate's schedule. When you configure
                  schedules (like "run updates only during weekends" or "between 10pm-6am"), times are interpreted using this timezone.
                </p>
              </div>
            </div>
          </div>

          <!-- Schedule Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label for="schedule" class="text-lg font-medium text-gray-900 block mb-2">
                Update Schedule
              </label>
              <select
                id="schedule"
                v-model="config.schedule"
                class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
              >
                <option v-for="opt in scheduleOptions" :key="opt.value" :value="opt.value">
                  {{ opt.label }}
                </option>
              </select>
              <div class="mt-3 text-sm text-gray-600">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Controls when Renovate is allowed to create or update PRs.
                  Helps minimize disruption by scheduling updates during low-activity periods.
                </p>
                <p class="mb-2">
                  <strong class="text-gray-700">Why you want this:</strong> Prevents PR noise during working hours.
                  "Weekends" and "Weeknights" are popular choices to keep updates out of the workday.
                </p>
                <p class="text-xs text-gray-600">
                  <strong>Note:</strong> Security vulnerability alerts will always override this schedule to ensure immediate patching.
                </p>
              </div>
            </div>
          </div>

          <!-- PR Limits Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label class="text-lg font-medium text-gray-900 block mb-2">
                Pull Request Management
              </label>
              <p class="text-sm text-gray-600 mb-4">
                Control how many PRs Renovate creates to avoid overwhelming your team and CI resources.
              </p>

              <RadioCardGroup v-model="config.prLimitStrategy" name="pr" :options="prLimitOptions" />

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Controls the rate and volume of PRs Renovate creates
                  via <code class="bg-white px-1 rounded">prHourlyLimit</code> (PRs per hour) and
                  <code class="bg-white px-1 rounded">prConcurrentLimit</code> (max open PRs).
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Prevents CI overload, makes reviews manageable,
                  and integrates with Renovate's Dependency Dashboard to show queued updates.
                </p>
              </div>
            </div>
          </div>

          <!-- Rebase Strategy Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label class="text-lg font-medium text-gray-900 block mb-2">
                Branch Update Strategy
              </label>
              <p class="text-sm text-gray-600 mb-4">
                Control when Renovate rebases its branches to keep them up-to-date with your base branch.
              </p>

              <RadioCardGroup v-model="config.rebaseWhen" name="rebase" :options="rebaseOptions" />

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Controls when Renovate updates its PR branches
                  to incorporate new commits from your base branch (like <code class="bg-white px-1 rounded">main</code>).
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Balances keeping PRs current with managing CI costs.
                  "Smart" works for most teams, "Only When Conflicted" helps if rebases are slowing Renovate down.
                </p>
              </div>
            </div>
          </div>

          <!-- Range Strategy Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label class="text-lg font-medium text-gray-900 block mb-2">
                Version Range Update Strategy
              </label>
              <p class="text-sm text-gray-600 mb-4">
                Control how Renovate updates version ranges in your package files (e.g., ^1.0.0 vs 1.0.0).
              </p>

              <RadioCardGroup v-model="config.rangeStrategy" name="range" :options="rangeOptions" />

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Determines how Renovate modifies version constraints
                  in your package.json, composer.json, or similar files when updating dependencies.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Controls the balance between stability
                  (conservative ranges) and visibility (explicit updates). "Replace When Needed" is often the safest choice.
                </p>
              </div>
            </div>
          </div>

          <!-- Automerge Type Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label class="text-lg font-medium text-gray-900 block mb-2">
                Automerge Method
              </label>
              <p class="text-sm text-gray-600 mb-4">
                Choose how Renovate should automerge updates when automerge is enabled (requires separate automerge configuration).
              </p>

              <RadioCardGroup v-model="config.automergeType" name="automerge-type" :options="automergeTypeOptions" />

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Controls how Renovate performs automerges.
                  "Pull Request" creates visible PRs, "Silent Branch Merge" commits directly for minimal noise.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> "Pull Request" provides full visibility and audit trail.
                  "Silent Branch Merge" reduces notification noise for trusted dependencies but requires compatible branch protection.
                </p>
              </div>
            </div>
          </div>

          <!-- Automerge Level Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label class="text-lg font-medium text-gray-900 block mb-2">
                Automerge Level
              </label>
              <p class="text-sm text-gray-600 mb-4">
                Automatically merge dependency updates that pass tests. Choose what types of updates to automerge.
              </p>

              <RadioCardGroup v-model="config.automergeLevel" name="automerge-level" :options="automergeLevelOptions" class="mb-4" />

              <!-- Dev Dependencies Checkbox -->
              <div v-if="config.automergeLevel !== 'all'" class="border-t border-gray-200 pt-4">
                <div class="flex items-start">
                  <input
                    id="automerge-dev-all"
                    v-model="config.automergeDevDependencies"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-dev-all" class="font-medium text-gray-900 cursor-pointer">
                      Always Automerge All Development Dependencies
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Automerge <strong>all</strong> devDependencies updates (build tools, linters, test frameworks), regardless of update type.
                      Works independently from the level setting above. Production dependencies follow the level setting.
                    </p>
                  </div>
                </div>
              </div>

              <!-- Ignore Tests Checkbox -->
              <div v-if="config.automergeLevel !== 'disabled'" class="border-t border-gray-200 pt-4 mt-4">
                <div class="flex items-start">
                  <input
                    id="ignore-tests"
                    v-model="config.ignoreTests"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="ignore-tests" class="font-medium text-gray-900 cursor-pointer">
                      Automerge Without Waiting for Tests
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Automerge immediately without waiting for status checks or test results.
                      <span class="text-red-600 font-medium">Not recommended</span> - only use if your repository has no test suite.
                    </p>
                  </div>
                </div>
              </div>

              <!-- Disable Automerge for Pre-1.0.0 Checkbox -->
              <div v-if="config.automergeLevel !== 'disabled'" class="border-t border-gray-200 pt-4 mt-4">
                <div class="flex items-start">
                  <input
                    id="disable-pre-1-automerge"
                    v-model="config.disablePreOneAutomerge"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="disable-pre-1-automerge" class="font-medium text-gray-900 cursor-pointer">
                      Disable Automerge for Pre-1.0.0 Versions
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Never automerge dependencies with version 0.x (e.g., 0.5.2, 0.12.4).
                      <span class="text-indigo-600 font-medium">Recommended for safety</span> - pre-1.0.0 packages can introduce breaking changes at any time.
                    </p>
                  </div>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Enables automatic merging of PRs that pass all tests.
                  Uses <code class="bg-white px-1 rounded">packageRules</code> to control which update types are automerged.
                </p>
                <p class="mb-2">
                  <strong class="text-gray-700">Why you want this:</strong> Reduces manual work for safe updates.
                  Most teams use "Patch + Minor". Requires good test coverage to be safe.
                </p>
                <p class="text-xs text-gray-600">
                  <strong>Example:</strong> If you select "Patch Updates Only" and check dev dependencies, you'll get patch updates automerged for production
                  dependencies AND all updates automerged for dev dependencies.
                </p>
              </div>
            </div>
          </div>

          <!-- Major Update Review -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <div class="flex items-start">
                <input
                  id="require-major-approval"
                  v-model="config.requireMajorApproval"
                  type="checkbox"
                  class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                >
                <div class="ml-3 flex-1">
                  <label for="require-major-approval" class="text-lg font-medium text-gray-900 cursor-pointer">
                    Require Approval for Major Updates
                  </label>
                  <p class="text-sm text-gray-600 mt-1">
                    Gate major version updates behind the Dependency Dashboard — no PR is created until you
                    tick its checkbox. Adds a <code class="bg-gray-100 px-1 rounded">packageRules</code> entry with
                    <code class="bg-gray-100 px-1 rounded">dependencyDashboardApproval: true</code> for
                    <code class="bg-gray-100 px-1 rounded">major</code> updates.
                  </p>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Major updates wait on the dashboard until you
                  explicitly approve them, instead of opening PRs automatically.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Majors are the most likely to break and are
                  never automerged anyway. Gating them keeps unrequested major PRs from piling up while you stay in
                  control of when to take them on. Recommended.
                </p>
              </div>
            </div>
          </div>

          <!-- Minimum Release Age Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <label class="text-lg font-medium text-gray-900 block mb-2">
                Minimum Release Age
              </label>
              <p class="text-sm text-gray-600 mb-4">
                Wait a specified period after a new version is released before Renovate creates an update PR.
                Helps avoid cutting-edge releases that might be unstable.
              </p>

              <RadioCardGroup v-model="config.minimumReleaseAge" name="release-age" :options="releaseAgeOptions" />

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Delays update PRs until a package version has been
                  published for at least the specified duration. The timer starts from the package registry's published timestamp.
                </p>
                <p class="mb-2">
                  <strong class="text-gray-700">Why you want this:</strong> Reduces risk of adopting releases with critical bugs
                  or breaking changes. Lets early adopters discover issues first. Particularly valuable for production dependencies.
                </p>
                <p class="text-xs text-gray-600">
                  <strong>Security Best Practice:</strong> When a delay is set, a <code class="bg-white px-1 rounded">vulnerabilityAlerts.minimumReleaseAge: "0 days"</code> override is added automatically to bypass the delay for vulnerability alerts, ensuring critical security patches are applied immediately. (With "No Delay" there's nothing to bypass, so it's omitted.)
                </p>
                <p class="text-xs text-gray-600 mt-1">
                  <strong>Less churn:</strong> When a delay is set, <code class="bg-white px-1 rounded">internalChecksFilter: "strict"</code> is added automatically so Renovate doesn't even raise a branch/PR until the version clears the waiting period — instead of parking a pending PR for days.
                </p>
              </div>
            </div>
          </div>

          <!-- Supply-Chain Hardening -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <div class="mb-4">
                <h3 class="text-lg font-medium text-gray-900 mb-2">Supply-Chain Hardening</h3>
                <p class="text-sm text-gray-600">
                  The hardened defaults from <code class="bg-gray-100 px-1 rounded">config:best-practices</code>:
                  pin mutable references (Docker tags, Action versions, devDependencies) to immutable
                  versions, and flag abandoned packages. Renovate keeps pinned versions updated for you, and
                  with branch automerge these stay on a clean, linear history.
                </p>
              </div>

              <div class="space-y-4">
                <!-- Docker Digests -->
                <div class="flex items-start">
                  <input
                    id="pin-docker-digests"
                    v-model="config.pinning.dockerDigests"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="pin-docker-digests" class="font-medium text-gray-900 cursor-pointer">
                      Pin Docker Image Digests
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Adds <code class="bg-gray-100 px-1 rounded">docker:pinDigests</code>. Pins images to
                      <code class="bg-gray-100 px-1 rounded">tag@sha256:…</code> so a moved tag can't silently swap the image underneath you.
                    </p>
                  </div>
                </div>

                <!-- GitHub Action SHAs -->
                <div class="flex items-start">
                  <input
                    id="pin-github-action-digests"
                    v-model="config.pinning.githubActionDigests"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="pin-github-action-digests" class="font-medium text-gray-900 cursor-pointer">
                      Pin GitHub Action SHAs
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Adds <code class="bg-gray-100 px-1 rounded">helpers:pinGitHubActionDigests</code>. Pins
                      <code class="bg-gray-100 px-1 rounded">uses:</code> refs to a full commit SHA instead of a mutable tag like <code class="bg-gray-100 px-1 rounded">@v4</code>.
                    </p>
                  </div>
                </div>

                <!-- Pin devDependencies -->
                <div class="flex items-start">
                  <input
                    id="pin-dev-dependencies"
                    v-model="config.pinning.devDependencies"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="pin-dev-dependencies" class="font-medium text-gray-900 cursor-pointer">
                      Pin devDependencies to Exact Versions
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Adds <code class="bg-gray-100 px-1 rounded">:pinDevDependencies</code>. Pins build tools, linters,
                      and test frameworks to exact versions for reproducible builds. Safe even for published libraries —
                      consumers never install your devDependencies.
                    </p>
                  </div>
                </div>

                <!-- Flag Abandoned Packages -->
                <div class="flex items-start">
                  <input
                    id="flag-abandoned"
                    v-model="config.flagAbandonedPackages"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="flag-abandoned" class="font-medium text-gray-900 cursor-pointer">
                      Flag Abandoned Packages
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Adds <code class="bg-gray-100 px-1 rounded">abandonments:recommended</code>. Surfaces dependencies
                      that are no longer maintained (and suggested replacements) so you can act before they become a risk.
                    </p>
                  </div>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Replaces mutable tags/ranges with immutable
                  versions in your Dockerfiles, Compose files, Action workflows, and devDependencies, and flags
                  unmaintained packages.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Tags and ranges are mutable — exact
                  digests/SHAs/versions are not. Pinning is a real supply-chain control that prevents a compromised
                  or re-pushed reference from changing what you build/run. Enabled by default; presets are no-ops where
                  the relevant files don't exist. These pin/digest updates are automerged, so they keep a clean git history.
                </p>
              </div>
            </div>
          </div>

          <!-- Lock File Maintenance Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <div class="flex items-start mb-4">
                <input
                  id="lockfile-enabled"
                  v-model="config.lockFileMaintenance.enabled"
                  type="checkbox"
                  class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                >
                <div class="ml-3 flex-1">
                  <label for="lockfile-enabled" class="text-lg font-medium text-gray-900 cursor-pointer">
                    Enable Lock File Maintenance
                  </label>
                  <p class="text-sm text-gray-600 mt-1">
                    Periodically refresh lock files (package-lock.json, yarn.lock, etc.) to update all dependencies,
                    including transitive ones, even when package.json hasn't changed.
                  </p>
                </div>
              </div>

              <!-- Schedule and Automerge Options -->
              <div v-if="config.lockFileMaintenance.enabled" class="ml-8 space-y-4">
                <!-- Schedule Selector -->
                <div>
                  <label for="lockfile-schedule" class="block text-sm font-medium text-gray-900 mb-2">
                    Lock File Maintenance Schedule
                  </label>
                  <select
                    id="lockfile-schedule"
                    v-model="config.lockFileMaintenance.schedule"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-gray-900"
                  >
                    <option v-for="opt in scheduleOptions" :key="opt.value" :value="opt.value">
                      {{ opt.label }}
                    </option>
                  </select>
                  <p class="text-xs text-gray-600 mt-1">
                    Set a dedicated schedule for lock file maintenance, independent of the global update schedule.
                  </p>
                </div>

                <!-- Automerge Checkbox -->
                <div class="flex items-start">
                  <input
                    id="lockfile-automerge"
                    v-model="config.lockFileMaintenance.automerge"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="lockfile-automerge" class="font-medium text-gray-900 cursor-pointer">
                      Automerge Lock File Maintenance PRs
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Automatically merge lock file maintenance updates if tests pass.
                      This is the lowest-risk type of automerge.
                    </p>
                  </div>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Regenerates lock files from scratch to update
                  all dependencies (including indirect/transitive ones) to their latest compatible versions.
                  Similar to deleting your lock file and running <code class="bg-white px-1 rounded">npm install</code> fresh.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Lock files can accumulate outdated transitive dependencies
                  over time. This ensures everything stays current, even dependencies you don't directly control.
                  Lowest-risk automerge candidate.
                </p>
              </div>
            </div>
          </div>

          <!-- Vulnerability Alerts Option -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <div class="mb-4">
                <div class="flex items-center mb-2">
                  <svg class="h-5 w-5 text-yellow-600 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path fill-rule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
                  </svg>
                  <h3 class="text-lg font-medium text-gray-900">Security Vulnerability Alerts</h3>
                </div>
                <p class="text-sm text-gray-600">
                  Already enabled by default with <code class="bg-gray-100 px-1 rounded">:enableVulnerabilityAlerts</code>.
                  Configure additional options below.
                </p>
              </div>

              <div class="space-y-4">
                <!-- Labels Input -->
                <div>
                  <label for="vuln-labels" class="block text-sm font-medium text-gray-900 mb-2">
                    Custom Labels
                  </label>
                  <input
                    id="vuln-labels"
                    v-model="config.vulnerabilityAlerts.labels"
                    type="text"
                    placeholder="e.g., security, urgent"
                    class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  >
                  <p class="text-xs text-gray-600 mt-1">
                    Comma-separated labels to add to vulnerability alert PRs. Default: "security"
                  </p>
                </div>

                <!-- Schedule Override Checkbox -->
                <div class="flex items-start">
                  <input
                    id="vuln-schedule-override"
                    v-model="config.vulnerabilityAlerts.scheduleOverride"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="vuln-schedule-override" class="font-medium text-gray-900 cursor-pointer">
                      Override Schedule (Create at Any Time)
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Explicitly set schedule to "at any time" to ensure vulnerability PRs are never delayed by other schedule configurations.
                      Recommended if you have custom schedules elsewhere.
                    </p>
                  </div>
                </div>

                <!-- Automerge Checkbox -->
                <div class="flex items-start">
                  <input
                    id="vuln-automerge"
                    v-model="config.vulnerabilityAlerts.automerge"
                    type="checkbox"
                    class="mt-1 h-5 w-5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <div class="ml-3 flex-1">
                    <label for="vuln-automerge" class="font-medium text-gray-900 cursor-pointer">
                      Automerge Security Updates
                    </label>
                    <p class="text-sm text-gray-600 mt-1">
                      Automatically merge security vulnerability updates if tests pass. Ensures fast patching of security issues.
                    </p>
                  </div>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-yellow-50 border-l-4 border-yellow-400 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Vulnerability alerts bypass normal schedules and rate limits
                  to create PRs immediately when security issues are detected. Adds <code class="bg-white px-1 rounded">[SECURITY]</code> suffix to commit messages.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Critical security patches should be applied as soon as possible,
                  not wait for your regular update schedule. GitHub Dependabot alerts are automatically detected.
                </p>
              </div>
            </div>
          </div>

          <!-- Group Updates by Package Manager -->
          <div class="border border-gray-200 rounded-lg p-6 mb-4 hover:shadow-md transition-shadow">
            <div>
              <div class="mb-4">
                <h3 class="text-lg font-medium text-gray-900 mb-2">Group Updates by Package Manager</h3>
                <p class="text-sm text-gray-600">
                  Group non-major updates from the same package manager into a single PR to reduce noise.
                  Majors are never grouped — they stay individual under major-update approval. Either
                  auto-group every manager at once, or pick specific managers below.
                </p>
              </div>

              <!-- Fast lane: auto-group every manager via the {{manager}} template -->
              <div class="flex items-start mb-4 p-3 bg-indigo-50 rounded-lg">
                <input
                  id="group-all-non-major"
                  v-model="config.groupAllNonMajor"
                  type="checkbox"
                  class="h-4 w-4 mt-0.5 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                >
                <label for="group-all-non-major" class="ml-2 text-sm text-gray-900 cursor-pointer">
                  <span class="font-medium">Auto-group every manager's non-major updates</span>
                  <span class="block text-gray-600 mt-1">
                    Emits a single <code v-pre class="bg-white px-1 rounded">{{manager}}</code> rule that groups
                    minor/patch updates into one PR per manager — across every manager Renovate detects, no need to
                    pick them below. Supersedes the per-manager selection.
                  </span>
                </label>
              </div>

              <div
                class="grid grid-cols-2 md:grid-cols-3 gap-3"
                :class="{ 'opacity-50 pointer-events-none': config.groupAllNonMajor }">
                <div v-for="group in managerGroups" :key="group.key" class="flex items-center">
                  <input
                    :id="`group-${group.key}`"
                    v-model="config.grouping[group.key]"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label :for="`group-${group.key}`" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    {{ group.label }}
                  </label>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Groups non-major (minor/patch/pin/digest) updates
                  from each manager into a single PR instead of one PR per dependency. Major updates are excluded so they
                  stay individual and gated behind major-update approval.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Reduces PR noise significantly. For example,
                  instead of 15 separate PRs for npm updates, you get 1 grouped PR — while a breaking major still arrives
                  on its own for a deliberate review.
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- Generate and Feedback Buttons -->
        <div class="mt-8 space-y-4">
          <button
            class="w-full bg-indigo-600 text-white py-3 px-6 rounded-lg font-semibold text-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors shadow-lg"
            @click="generateAndDownload"
          >
            Generate renovate.json
          </button>
          <button
            :disabled="isLoadingFeedback"
            class="w-full bg-green-600 text-white py-3 px-6 rounded-lg font-semibold text-lg hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2 transition-colors shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
            @click="getFeedback"
          >
            {{ isLoadingFeedback ? 'Getting Feedback...' : 'Get AI Feedback' }}
          </button>
        </div>
      </div>

      <!-- Feedback Section -->
      <div v-if="feedback" class="bg-white shadow-xl rounded-lg p-8 mb-6">
        <div class="flex justify-between items-start mb-4">
          <h3 class="text-xl font-semibold text-gray-900">AI Feedback</h3>
          <button
            class="text-gray-400 hover:text-gray-600 transition-colors"
            @click="feedback = null"
          >
            <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd" />
            </svg>
          </button>
        </div>

        <!-- Summary -->
        <div class="mb-6">
          <h4 class="text-lg font-medium text-gray-900 mb-2">Summary</h4>
          <div class="bg-blue-50 border-l-4 border-blue-500 p-4 rounded">
            <p class="text-gray-800">{{ feedback.summary }}</p>
          </div>
        </div>

        <!-- Issues -->
        <div v-if="feedback.issues && feedback.issues.length > 0" class="mb-6">
          <h4 class="text-lg font-medium text-gray-900 mb-3">Issues Found</h4>
          <div class="space-y-3">
            <div
              v-for="(issue, index) in feedback.issues"
              :key="index"
              class="border rounded-lg p-4"
              :class="{
                'border-red-300 bg-red-50': issue.severity === 'error',
                'border-yellow-300 bg-yellow-50': issue.severity === 'warning',
                'border-blue-300 bg-blue-50': issue.severity === 'info'
              }"
            >
              <div class="flex items-start">
                <span
                  class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium mr-3 mt-0.5"
                  :class="{
                    'bg-red-100 text-red-800': issue.severity === 'error',
                    'bg-yellow-100 text-yellow-800': issue.severity === 'warning',
                    'bg-blue-100 text-blue-800': issue.severity === 'info'
                  }"
                >
                  {{ issue.severity.toUpperCase() }}
                </span>
                <div class="flex-1">
                  <div class="flex items-start justify-between mb-2">
                    <p class="font-medium text-gray-900">{{ issue.message }}</p>
                    <code class="ml-2 text-xs bg-gray-100 px-2 py-1 rounded text-gray-600 whitespace-nowrap">{{ issue.jsonPath }}</code>
                  </div>
                  <p class="text-sm text-gray-700 mt-1">
                    <strong class="text-gray-900">Suggestion:</strong> {{ issue.suggestion }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Improved Config -->
        <div v-if="feedback.improvedRenovateJson" class="mb-4">
          <h4 class="text-lg font-medium text-gray-900 mb-3">Improved Configuration</h4>
          <p class="text-sm text-gray-600 mb-2">Here's an AI-suggested improved version of your configuration:</p>
          <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm">{{ feedback.improvedRenovateJson }}</pre>
        </div>
      </div>

      <!-- Preview Section -->
      <div class="bg-white shadow-xl rounded-lg p-8">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-xl font-semibold text-gray-900">Configuration Preview</h3>
          <button
            class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors"
            @click="copyToClipboard"
          >
            {{ copyButtonText }}
          </button>
        </div>
        <pre class="bg-gray-900 text-green-400 p-4 rounded-lg overflow-x-auto text-sm">{{ generatedConfig }}</pre>
      </div>
    </div>

    <!-- Import Modal -->
    <div v-if="showImportModal" class="fixed inset-0 z-50 overflow-y-auto">
      <div class="flex min-h-screen items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="fixed inset-0 bg-black bg-opacity-50 transition-opacity" @click="showImportModal = false"></div>

        <!-- Modal -->
        <div class="relative bg-white rounded-lg shadow-xl max-w-lg w-full p-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="text-xl font-semibold text-gray-900">Import Configuration</h3>
            <button
              class="text-gray-400 hover:text-gray-600 transition-colors"
              @click="showImportModal = false"
            >
              <svg class="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <p class="text-sm text-gray-600 mb-4">
            Import an existing renovate.json configuration to populate the form fields.
          </p>

          <!-- Tabs -->
          <div class="flex border-b border-gray-200 mb-4">
            <button
              class="px-4 py-2 text-sm font-medium transition-colors"
              :class="importTab === 'paste' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500 hover:text-gray-700'"
              @click="importTab = 'paste'; importError = ''"
            >
              Paste JSON
            </button>
            <button
              class="px-4 py-2 text-sm font-medium transition-colors"
              :class="importTab === 'url' ? 'text-indigo-600 border-b-2 border-indigo-600' : 'text-gray-500 hover:text-gray-700'"
              @click="importTab = 'url'; importError = ''"
            >
              GitHub URL
            </button>
          </div>

          <!-- Paste JSON Tab -->
          <div v-if="importTab === 'paste'">
            <textarea
              v-model="importJsonText"
              placeholder="Paste your renovate.json content here..."
              class="w-full h-48 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 font-mono text-sm resize-none"
            ></textarea>
            <button
              class="mt-4 w-full bg-indigo-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors"
              @click="importFromJson"
            >
              Import JSON
            </button>
          </div>

          <!-- GitHub URL Tab -->
          <div v-if="importTab === 'url'">
            <input
              v-model="importUrl"
              type="text"
              placeholder="https://github.com/owner/repo/blob/main/renovate.json"
              class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-sm"
            >
            <p class="mt-2 text-xs text-gray-500">
              Supports public GitHub URLs: direct file links (blob/raw), or a repository root URL (looks for renovate.json on the default branch).
              For GitLab or private repositories, use the
              <NuxtLink to="/dashboard" class="text-indigo-600 hover:text-indigo-800 underline">Dashboard</NuxtLink>.
            </p>
            <button
              :disabled="isImporting"
              class="mt-4 w-full bg-indigo-600 text-white py-2 px-4 rounded-lg font-medium hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              @click="importFromUrl"
            >
              {{ isImporting ? 'Importing...' : 'Import from URL' }}
            </button>
          </div>

          <!-- Error Message -->
          <div v-if="importError" class="mt-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p class="text-sm text-red-600">{{ importError }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { generateRenovateConfigJson, configPresets, defaultPresetId, renovateDefaultsConfig, type RenovateConfig } from '../lib/generateRenovateConfig'
import { parseRenovateJson } from '../lib/parseRenovateConfig'
import { timezones } from '../lib/timezones'
import {
  prLimitOptions,
  rebaseOptions,
  rangeOptions,
  automergeTypeOptions,
  automergeLevelOptions,
  releaseAgeOptions,
  scheduleOptions,
  managerGroups
} from '../lib/formOptions'

// Same-origin backend access (relative /api, CSRF + credentials) — shared with the dashboard.
const { request } = useApi()

// sessionStorage key the dashboard uses to hand a repo's renovate.json to the editor.
const EDITOR_IMPORT_KEY = 'renovate:editor-import'

interface Issue {
  severity: string
  jsonPath: string
  message: string
  suggestion: string
}

interface FeedbackResponse {
  summary: string
  issues: Issue[]
  improvedRenovateJson: string
}

// Defaults live in the shared lib so the form and the /generate pseudo-API
// agree on a single source of truth. structuredClone keeps this instance
// isolated from the exported constant.
const config = ref<RenovateConfig>(structuredClone(renovateDefaultsConfig))

// Preset selector. The form opens on the neutral Renovate defaults preset, so
// the dropdown starts there. Selecting a preset overwrites every field; the
// user can still tweak individual options afterwards.
const selectedPreset = ref<string>(defaultPresetId)
const activePreset = computed(() => configPresets.find(p => p.id === selectedPreset.value))

const applyPreset = () => {
  const preset = configPresets.find(p => p.id === selectedPreset.value)
  if (preset) {
    config.value = structuredClone(preset.config)
  }
}

const feedback = ref<FeedbackResponse | null>(null)
const isLoadingFeedback = ref(false)
const copyButtonText = ref('Copy')

// Import functionality state
const showImportModal = ref(false)
const importTab = ref<'paste' | 'url'>('paste')
const importJsonText = ref('')
const importUrl = ref('')
const importError = ref('')
const isImporting = ref(false)
// Repo name shown in the "loaded from dashboard" notice; empty hides it.
const importedFromRepo = ref('')

const generatedConfig = computed(() => generateRenovateConfigJson(config.value))

const generateAndDownload = () => {
  const configText = generatedConfig.value
  const blob = new Blob([configText], { type: 'application/json' })
  const url = URL.createObjectURL(blob)

  const link = document.createElement('a')
  link.href = url
  link.download = 'renovate.json'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

const copyToClipboard = async () => {
  try {
    await navigator.clipboard.writeText(generatedConfig.value)
    copyButtonText.value = 'Copied!'
    setTimeout(() => {
      copyButtonText.value = 'Copy'
    }, 2000)
  } catch {
    copyButtonText.value = 'Failed'
    setTimeout(() => {
      copyButtonText.value = 'Copy'
    }, 2000)
  }
}

// Heuristic: does this look like a GitLab URL? GitLab project paths use the `/-/` marker, and
// self-hosted instances usually carry "gitlab" in the host. URL import only supports GitHub
// (raw.githubusercontent.com is CORS-open); GitLab is handled in the authenticated dashboard.
const looksLikeGitLabUrl = (url: string): boolean => {
  return url.includes('/-/') || url.toLowerCase().includes('gitlab')
}

// Convert GitHub URL to raw content URL
const convertToRawGitHubUrl = (url: string): string => {
  // Handle various GitHub URL formats
  // https://github.com/owner/repo/blob/branch/path/to/renovate.json
  // https://github.com/owner/repo/raw/branch/path/to/renovate.json
  // https://raw.githubusercontent.com/owner/repo/branch/path/to/renovate.json

  if (url.includes('raw.githubusercontent.com')) {
    return url // Already a raw URL
  }

  // Convert blob URL to raw URL
  if (url.includes('github.com') && url.includes('/blob/')) {
    return url.replace('github.com', 'raw.githubusercontent.com').replace('/blob/', '/')
  }

  // Convert tree URL to raw URL (for direct file links)
  if (url.includes('github.com') && url.includes('/tree/')) {
    return url.replace('github.com', 'raw.githubusercontent.com').replace('/tree/', '/')
  }

  // If it's a repo root URL, try to fetch renovate.json from default branch
  const repoMatch = url.match(/github\.com\/([^/]+)\/([^/]+)\/?$/)
  if (repoMatch) {
    const [, owner, repo] = repoMatch
    return `https://raw.githubusercontent.com/${owner}/${repo}/main/renovate.json`
  }

  return url
}

// Import from pasted JSON
const importFromJson = () => {
  importError.value = ''

  if (!importJsonText.value.trim()) {
    importError.value = 'Please paste a JSON configuration'
    return
  }

  try {
    const parsedConfig = parseRenovateJson(importJsonText.value)
    applyImportedConfig(parsedConfig)
    showImportModal.value = false
    importJsonText.value = ''
  } catch (e) {
    importError.value = `Invalid JSON: ${e instanceof Error ? e.message : 'Unknown error'}`
  }
}

// Import from a GitHub URL (client-side, public repos only). GitLab is intentionally not handled
// here: its raw endpoint blocks browser CORS, and private/GitLab configs need the user's token —
// both are solved by the dashboard's "Open in editor", so GitLab URLs are pointed there.
const importFromUrl = async () => {
  importError.value = ''

  const trimmed = importUrl.value.trim()
  if (!trimmed) {
    importError.value = 'Please enter a GitHub URL'
    return
  }

  if (looksLikeGitLabUrl(trimmed)) {
    importError.value =
      'GitLab import is not available here. Open the Dashboard, pick the repository, and use "Open in editor" to load its renovate.json.'
    return
  }

  isImporting.value = true

  try {
    const rawUrl = convertToRawGitHubUrl(trimmed)
    const response = await fetch(rawUrl)

    if (!response.ok) {
      if (response.status === 404) {
        throw new Error('File not found. Make sure the URL points to a valid renovate.json file.')
      }
      throw new Error(`Failed to fetch: ${response.status} ${response.statusText}`)
    }

    const jsonText = await response.text()
    const parsedConfig = parseRenovateJson(jsonText)
    applyImportedConfig(parsedConfig)
    showImportModal.value = false
    importUrl.value = ''
  } catch (e) {
    importError.value = e instanceof Error ? e.message : 'Failed to import configuration'
  } finally {
    isImporting.value = false
  }
}

// Apply imported config to the current config
const applyImportedConfig = (imported: Partial<RenovateConfig>) => {
  // Merge imported values with current config, keeping defaults for unspecified values
  if (imported.semanticCommits !== undefined) config.value.semanticCommits = imported.semanticCommits
  if (imported.timezone !== undefined) config.value.timezone = imported.timezone
  if (imported.schedule !== undefined) config.value.schedule = imported.schedule
  if (imported.prLimitStrategy !== undefined) config.value.prLimitStrategy = imported.prLimitStrategy
  if (imported.rebaseWhen !== undefined) config.value.rebaseWhen = imported.rebaseWhen
  if (imported.rangeStrategy !== undefined) config.value.rangeStrategy = imported.rangeStrategy
  if (imported.automergeType !== undefined) config.value.automergeType = imported.automergeType
  if (imported.automergeLevel !== undefined) config.value.automergeLevel = imported.automergeLevel
  if (imported.automergeDevDependencies !== undefined) config.value.automergeDevDependencies = imported.automergeDevDependencies
  if (imported.ignoreTests !== undefined) config.value.ignoreTests = imported.ignoreTests
  if (imported.disablePreOneAutomerge !== undefined) config.value.disablePreOneAutomerge = imported.disablePreOneAutomerge
  if (imported.requireMajorApproval !== undefined) config.value.requireMajorApproval = imported.requireMajorApproval
  if (imported.minimumReleaseAge !== undefined) config.value.minimumReleaseAge = imported.minimumReleaseAge

  if (imported.pinning !== undefined) {
    config.value.pinning = { ...config.value.pinning, ...imported.pinning }
  }
  if (imported.flagAbandonedPackages !== undefined) config.value.flagAbandonedPackages = imported.flagAbandonedPackages

  if (imported.lockFileMaintenance !== undefined) {
    config.value.lockFileMaintenance = { ...config.value.lockFileMaintenance, ...imported.lockFileMaintenance }
  }

  if (imported.vulnerabilityAlerts !== undefined) {
    config.value.vulnerabilityAlerts = { ...config.value.vulnerabilityAlerts, ...imported.vulnerabilityAlerts }
  }

  if (imported.grouping !== undefined) {
    config.value.grouping = { ...config.value.grouping, ...imported.grouping }
  }
}

const getFeedback = async () => {
  isLoadingFeedback.value = true
  feedback.value = null

  try {
    // Same-origin `/api` via useApi (CSRF + credentials handled centrally) — never a hardcoded
    // backend host. Mirrors how the dashboard talks to the backend.
    const response = await request('/feedback', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        renovateJson: generatedConfig.value
      })
    })

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const data: FeedbackResponse = await response.json()
    feedback.value = data
  } catch (error) {
    console.error('Error getting feedback:', error)
    // Create a feedback object with just an error message
    feedback.value = {
      summary: `Error getting feedback: ${error instanceof Error ? error.message : 'Unknown error'}`,
      issues: [],
      improvedRenovateJson: ''
    }
  } finally {
    isLoadingFeedback.value = false
  }
}

// On load, pick up a config handed over from the dashboard's "Open in editor" and apply it. The
// handoff goes through sessionStorage (one-shot) rather than the URL so large configs don't end up
// in the address bar or browser history.
onMounted(() => {
  if (typeof window === 'undefined') return
  const raw = sessionStorage.getItem(EDITOR_IMPORT_KEY)
  if (!raw) return
  sessionStorage.removeItem(EDITOR_IMPORT_KEY)
  try {
    const payload = JSON.parse(raw) as { fullName?: string; json?: string }
    if (!payload.json) return
    applyImportedConfig(parseRenovateJson(payload.json))
    importedFromRepo.value = payload.fullName || 'the selected repository'
  } catch (e) {
    console.error('Failed to load imported config from dashboard:', e)
  }
})
</script>

<style scoped>
code {
  font-family: 'Courier New', monospace;
}
</style>
