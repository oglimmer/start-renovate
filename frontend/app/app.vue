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
                <option value="at-any-time">At Any Time - Default</option>
                <option value="weekly">Once per Week (Monday before 5am)</option>
                <option value="monthly">Once per Month (First day before 5am)</option>
                <option value="weekends">Weekends Only</option>
                <option value="outside-business-hours">Outside Business Hours (Evenings + Weekends)</option>
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

              <div class="space-y-3">
                <!-- Default Option -->
                <div class="flex items-start">
                  <input
                    id="pr-default"
                    v-model="config.prLimitStrategy"
                    type="radio"
                    value="default"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="pr-default" class="font-medium text-gray-900 cursor-pointer">
                      Balanced
                    </label>
                    <p class="text-sm text-gray-600">
                      Renovate's default settings: Up to 2 PRs per hour, max 10 open PRs at once.
                      Good for most teams with regular review capacity.
                    </p>
                  </div>
                </div>

                <!-- Conservative Option -->
                <div class="flex items-start">
                  <input
                    id="pr-conservative"
                    v-model="config.prLimitStrategy"
                    type="radio"
                    value="conservative"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="pr-conservative" class="font-medium text-gray-900 cursor-pointer">
                      Minimal Noise
                    </label>
                    <p class="text-sm text-gray-600">
                      Conservative limits: 1 PR per hour, max 3 open PRs.
                      Perfect for small teams or projects with limited CI resources.
                    </p>
                  </div>
                </div>

                <!-- Active Option -->
                <div class="flex items-start">
                  <input
                    id="pr-active"
                    v-model="config.prLimitStrategy"
                    type="radio"
                    value="active"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="pr-active" class="font-medium text-gray-900 cursor-pointer">
                      Fast Updates
                    </label>
                    <p class="text-sm text-gray-600">
                      Aggressive limits: 10 PRs per hour, max 20 open PRs.
                      For teams that can quickly review and merge updates, or have robust CI pipelines.
                    </p>
                  </div>
                </div>
              </div>

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

              <div class="space-y-3">
                <!-- Auto Option -->
                <div class="flex items-start">
                  <input
                    id="rebase-auto"
                    v-model="config.rebaseWhen"
                    type="radio"
                    value="auto"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="rebase-auto" class="font-medium text-gray-900 cursor-pointer">
                      Smart
                    </label>
                    <p class="text-sm text-gray-600">
                      Renovate's default: Auto-detects the best strategy. Keeps branches up-to-date if automerge is enabled or your repo requires it.
                    </p>
                  </div>
                </div>

                <!-- Conflicted Option -->
                <div class="flex items-start">
                  <input
                    id="rebase-conflicted"
                    v-model="config.rebaseWhen"
                    type="radio"
                    value="conflicted"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="rebase-conflicted" class="font-medium text-gray-900 cursor-pointer">
                      Only When Conflicted
                    </label>
                    <p class="text-sm text-gray-600">
                      Rebase only when merge conflicts occur. Reduces CI load if you have many concurrent PRs.
                      Good for busy repositories.
                    </p>
                  </div>
                </div>

                <!-- Behind Base Option -->
                <div class="flex items-start">
                  <input
                    id="rebase-behind"
                    v-model="config.rebaseWhen"
                    type="radio"
                    value="behind-base-branch"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="rebase-behind" class="font-medium text-gray-900 cursor-pointer">
                      Always Keep Up-to-Date
                    </label>
                    <p class="text-sm text-gray-600">
                      Rebase whenever the branch is behind base by 1+ commits. Ensures PRs are always current,
                      but increases CI runs.
                    </p>
                  </div>
                </div>

                <!-- Never Option -->
                <div class="flex items-start">
                  <input
                    id="rebase-never"
                    v-model="config.rebaseWhen"
                    type="radio"
                    value="never"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="rebase-never" class="font-medium text-gray-900 cursor-pointer">
                      Manual Only
                    </label>
                    <p class="text-sm text-gray-600">
                      Never automatically rebase. You'll need to manually trigger rebases. Not recommended for most use cases.
                    </p>
                  </div>
                </div>
              </div>

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

              <div class="space-y-3">
                <!-- Auto Option -->
                <div class="flex items-start">
                  <input
                    id="range-auto"
                    v-model="config.rangeStrategy"
                    type="radio"
                    value="auto"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="range-auto" class="font-medium text-gray-900 cursor-pointer">
                      Smart
                    </label>
                    <p class="text-sm text-gray-600">
                      Let Renovate decide the best strategy based on your project type and dependencies.
                    </p>
                  </div>
                </div>

                <!-- Replace Option -->
                <div class="flex items-start">
                  <input
                    id="range-replace"
                    v-model="config.rangeStrategy"
                    type="radio"
                    value="replace"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="range-replace" class="font-medium text-gray-900 cursor-pointer">
                      Replace When Needed
                    </label>
                    <p class="text-sm text-gray-600">
                      Only update ranges when new version falls outside. Example: <code class="bg-gray-100 px-1 rounded">^1.0.0</code> → <code class="bg-gray-100 px-1 rounded">^2.0.0</code>.
                      Conservative approach.
                    </p>
                  </div>
                </div>

                <!-- Bump Option -->
                <div class="flex items-start">
                  <input
                    id="range-bump"
                    v-model="config.rangeStrategy"
                    type="radio"
                    value="bump"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="range-bump" class="font-medium text-gray-900 cursor-pointer">
                      Always Bump Range
                    </label>
                    <p class="text-sm text-gray-600">
                      Explicitly update ranges for every change. Example: <code class="bg-gray-100 px-1 rounded">^1.0.0</code> → <code class="bg-gray-100 px-1 rounded">^1.0.1</code>.
                      Makes all updates visible in package.json.
                    </p>
                  </div>
                </div>

                <!-- Update Lockfile Option -->
                <div class="flex items-start">
                  <input
                    id="range-lockfile"
                    v-model="config.rangeStrategy"
                    type="radio"
                    value="update-lockfile"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="range-lockfile" class="font-medium text-gray-900 cursor-pointer">
                      Lockfile Only
                    </label>
                    <p class="text-sm text-gray-600">
                      Update lockfiles without changing package.json ranges. Ranges stay as-is, only resolved versions change.
                      Good for keeping stable ranges.
                    </p>
                  </div>
                </div>

                <!-- Pin Option -->
                <div class="flex items-start">
                  <input
                    id="range-pin"
                    v-model="config.rangeStrategy"
                    type="radio"
                    value="pin"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="range-pin" class="font-medium text-gray-900 cursor-pointer">
                      Pin to Exact Versions
                    </label>
                    <p class="text-sm text-gray-600">
                      Remove all ranges and use exact versions. Example: <code class="bg-gray-100 px-1 rounded">^1.0.0</code> → <code class="bg-gray-100 px-1 rounded">1.0.1</code>.
                      Maximum control and reproducibility.
                    </p>
                  </div>
                </div>
              </div>

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

              <div class="space-y-3">
                <!-- PR Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-pr"
                    v-model="config.automergeType"
                    type="radio"
                    value="pr"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-pr" class="font-medium text-gray-900 cursor-pointer">
                      Pull Request
                    </label>
                    <p class="text-sm text-gray-600">
                      Create PRs and automerge them after tests pass. Full visibility with notifications.
                      Works with all branch protection rules.
                    </p>
                  </div>
                </div>

                <!-- Branch Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-branch"
                    v-model="config.automergeType"
                    type="radio"
                    value="branch"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-branch" class="font-medium text-gray-900 cursor-pointer">
                      Silent Branch Merge
                    </label>
                    <p class="text-sm text-gray-600">
                      Merge directly to base branch without creating PRs. Silent updates with only commits visible.
                      Creates PR only if tests fail. Requires branch protection to allow Renovate pushes.
                    </p>
                  </div>
                </div>

                <!-- PR Comment Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-comment"
                    v-model="config.automergeType"
                    type="radio"
                    value="pr-comment"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-comment" class="font-medium text-gray-900 cursor-pointer">
                      External Merge Bot
                    </label>
                    <p class="text-sm text-gray-600">
                      Use with external merge bots like bors-ng. Renovate adds a comment to trigger your bot.
                      Only use if you have a merge bot configured.
                    </p>
                  </div>
                </div>
              </div>

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

              <div class="space-y-3 mb-4">
                <!-- Disabled Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-disabled"
                    v-model="config.automergeLevel"
                    type="radio"
                    value="disabled"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-disabled" class="font-medium text-gray-900 cursor-pointer">
                      Disabled
                    </label>
                    <p class="text-sm text-gray-600">
                      No automatic merging. All updates require manual review and approval.
                      Safest option with full control.
                    </p>
                  </div>
                </div>

                <!-- Patch Only Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-patch"
                    v-model="config.automergeLevel"
                    type="radio"
                    value="patch"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-patch" class="font-medium text-gray-900 cursor-pointer">
                      Patch Updates Only
                    </label>
                    <p class="text-sm text-gray-600">
                      Automerge only patch updates (e.g., 1.2.3 → 1.2.4). Most conservative automated approach.
                      Good for risk-averse teams with good test coverage.
                    </p>
                  </div>
                </div>

                <!-- Patch + Minor Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-minor"
                    v-model="config.automergeLevel"
                    type="radio"
                    value="minor"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-minor" class="font-medium text-gray-900 cursor-pointer">
                      Patch + Minor Updates
                    </label>
                    <p class="text-sm text-gray-600">
                      Automerge patch and minor updates (e.g., 1.2.3 → 1.3.0). Most common recommendation.
                      Major updates still require manual review.
                    </p>
                  </div>
                </div>

                <!-- All Updates Option -->
                <div class="flex items-start">
                  <input
                    id="automerge-all"
                    v-model="config.automergeLevel"
                    type="radio"
                    value="all"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="automerge-all" class="font-medium text-gray-900 cursor-pointer">
                      All Updates
                    </label>
                    <p class="text-sm text-gray-600">
                      Automerge all updates including major versions. Aggressive approach requiring excellent test coverage.
                      Only use if you have comprehensive automated tests.
                    </p>
                  </div>
                </div>
              </div>

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

              <div class="space-y-3">
                <!-- Never Option -->
                <div class="flex items-start">
                  <input
                    id="release-age-never"
                    v-model="config.minimumReleaseAge"
                    type="radio"
                    value="never"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="release-age-never" class="font-medium text-gray-900 cursor-pointer">
                      No Delay
                    </label>
                    <p class="text-sm text-gray-600">
                      Update to new releases immediately. Renovate's default behavior.
                      Get updates as soon as they're available.
                    </p>
                  </div>
                </div>

                <!-- 3 Days Option -->
                <div class="flex items-start">
                  <input
                    id="release-age-3"
                    v-model="config.minimumReleaseAge"
                    type="radio"
                    value="3-days"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="release-age-3" class="font-medium text-gray-900 cursor-pointer">
                      3 Days
                    </label>
                    <p class="text-sm text-gray-600">
                      Wait 3 days after release. Short stabilization period to catch immediate bugs.
                      Good balance for most teams.
                    </p>
                  </div>
                </div>

                <!-- 7 Days Option -->
                <div class="flex items-start">
                  <input
                    id="release-age-7"
                    v-model="config.minimumReleaseAge"
                    type="radio"
                    value="7-days"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="release-age-7" class="font-medium text-gray-900 cursor-pointer">
                      7 Days (1 Week)
                    </label>
                    <p class="text-sm text-gray-600">
                      Wait 1 week after release. Gives the community time to discover issues.
                      Recommended for stability-focused teams.
                    </p>
                  </div>
                </div>

                <!-- 14 Days Option -->
                <div class="flex items-start">
                  <input
                    id="release-age-14"
                    v-model="config.minimumReleaseAge"
                    type="radio"
                    value="14-days"
                    class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
                  >
                  <div class="ml-3 flex-1">
                    <label for="release-age-14" class="font-medium text-gray-900 cursor-pointer">
                      14 Days (2 Weeks)
                    </label>
                    <p class="text-sm text-gray-600">
                      Wait 2 weeks after release. Conservative approach for maximum stability.
                      Best for risk-averse production environments.
                    </p>
                  </div>
                </div>
              </div>

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
                  <strong>Security Best Practice:</strong> This configuration will automatically include a rule to bypass the delay for vulnerability alerts, ensuring critical security patches are applied immediately.
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
                    <option value="at-any-time">At Any Time - Default</option>
                    <option value="weekly">Once per Week (Monday before 5am)</option>
                    <option value="monthly">Once per Month (First day before 5am)</option>
                    <option value="weekends">Weekends Only</option>
                    <option value="outside-business-hours">Outside Business Hours (Evenings + Weekends)</option>
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
                  Group all updates from the same package manager into a single PR to reduce noise.
                  Select which package managers you want to group.
                </p>
              </div>

              <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
                <!-- npm -->
                <div class="flex items-center">
                  <input
                    id="group-npm"
                    v-model="config.grouping.npm"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-npm" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    npm (JavaScript)
                  </label>
                </div>

                <!-- Docker -->
                <div class="flex items-center">
                  <input
                    id="group-docker"
                    v-model="config.grouping.docker"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-docker" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Docker
                  </label>
                </div>

                <!-- Maven -->
                <div class="flex items-center">
                  <input
                    id="group-maven"
                    v-model="config.grouping.maven"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-maven" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Maven (Java)
                  </label>
                </div>

                <!-- Gradle -->
                <div class="flex items-center">
                  <input
                    id="group-gradle"
                    v-model="config.grouping.gradle"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-gradle" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Gradle (Java)
                  </label>
                </div>

                <!-- pip -->
                <div class="flex items-center">
                  <input
                    id="group-pip"
                    v-model="config.grouping.pip"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-pip" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    pip (Python)
                  </label>
                </div>

                <!-- Composer -->
                <div class="flex items-center">
                  <input
                    id="group-composer"
                    v-model="config.grouping.composer"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-composer" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Composer (PHP)
                  </label>
                </div>

                <!-- Helm -->
                <div class="flex items-center">
                  <input
                    id="group-helm"
                    v-model="config.grouping.helm"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-helm" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Helm (Kubernetes)
                  </label>
                </div>

                <!-- GitHub Actions -->
                <div class="flex items-center">
                  <input
                    id="group-github-actions"
                    v-model="config.grouping.githubActions"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-github-actions" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    GitHub Actions
                  </label>
                </div>

                <!-- Terraform -->
                <div class="flex items-center">
                  <input
                    id="group-terraform"
                    v-model="config.grouping.terraform"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-terraform" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Terraform
                  </label>
                </div>

                <!-- Go -->
                <div class="flex items-center">
                  <input
                    id="group-gomod"
                    v-model="config.grouping.gomod"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-gomod" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Go Modules
                  </label>
                </div>

                <!-- Cargo -->
                <div class="flex items-center">
                  <input
                    id="group-cargo"
                    v-model="config.grouping.cargo"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-cargo" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Cargo (Rust)
                  </label>
                </div>

                <!-- Bundler -->
                <div class="flex items-center">
                  <input
                    id="group-bundler"
                    v-model="config.grouping.bundler"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-bundler" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    Bundler (Ruby)
                  </label>
                </div>

                <!-- NuGet -->
                <div class="flex items-center">
                  <input
                    id="group-nuget"
                    v-model="config.grouping.nuget"
                    type="checkbox"
                    class="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                  >
                  <label for="group-nuget" class="ml-2 text-sm text-gray-900 cursor-pointer">
                    NuGet (.NET)
                  </label>
                </div>
              </div>

              <div class="mt-4 text-sm text-gray-600 bg-gray-50 p-3 rounded">
                <p class="mb-2">
                  <strong class="text-gray-700">What it does:</strong> Groups all updates from each selected package manager
                  into a single PR instead of creating separate PRs for each dependency update.
                </p>
                <p>
                  <strong class="text-gray-700">Why you want this:</strong> Reduces PR noise significantly. For example,
                  instead of 15 separate PRs for npm updates, you get 1 grouped PR titled "All npm updates".
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
              Supports GitHub URLs in various formats: direct file links, repository URLs (will look for renovate.json in root), or raw.githubusercontent.com URLs.
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
import { ref, computed } from 'vue'

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

interface RenovateConfig {
  semanticCommits: boolean
  timezone: string
  schedule: 'at-any-time' | 'weekly' | 'monthly' | 'weekends' | 'outside-business-hours'
  prLimitStrategy: 'default' | 'conservative' | 'active'
  rebaseWhen: 'auto' | 'never' | 'conflicted' | 'behind-base-branch'
  rangeStrategy: 'auto' | 'pin' | 'bump' | 'replace' | 'widen' | 'update-lockfile'
  automergeType: 'pr' | 'branch' | 'pr-comment'
  automergeLevel: 'disabled' | 'patch' | 'minor' | 'all'
  automergeDevDependencies: boolean
  ignoreTests: boolean
  disablePreOneAutomerge: boolean
  minimumReleaseAge: 'never' | '3-days' | '7-days' | '14-days'
  lockFileMaintenance: {
    enabled: boolean
    schedule: 'at-any-time' | 'weekly' | 'monthly' | 'weekends' | 'outside-business-hours'
    automerge: boolean
  }
  vulnerabilityAlerts: {
    labels: string
    scheduleOverride: boolean
    automerge: boolean
  }
  grouping: {
    npm: boolean
    docker: boolean
    maven: boolean
    gradle: boolean
    pip: boolean
    composer: boolean
    helm: boolean
    githubActions: boolean
    terraform: boolean
    gomod: boolean
    cargo: boolean
    bundler: boolean
    nuget: boolean
  }
}

const config = ref<RenovateConfig>({
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
})

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

const timezones = [
  { value: 'UTC', label: 'UTC - Coordinated Universal Time' },
  { value: 'Europe/Berlin', label: 'Europe/Berlin (CET/CEST)' },
  { value: 'Europe/London', label: 'Europe/London (GMT/BST)' },
  { value: 'Europe/Paris', label: 'Europe/Paris (CET/CEST)' },
  { value: 'Europe/Amsterdam', label: 'Europe/Amsterdam (CET/CEST)' },
  { value: 'America/New_York', label: 'America/New_York (EST/EDT - US East Coast)' },
  { value: 'America/Chicago', label: 'America/Chicago (CST/CDT - US Central)' },
  { value: 'America/Denver', label: 'America/Denver (MST/MDT - US Mountain)' },
  { value: 'America/Los_Angeles', label: 'America/Los_Angeles (PST/PDT - US West Coast)' },
  { value: 'America/Toronto', label: 'America/Toronto (EST/EDT - Canada)' },
  { value: 'America/Sao_Paulo', label: 'America/Sao_Paulo (BRT - Brazil)' },
  { value: 'Asia/Tokyo', label: 'Asia/Tokyo (JST - Japan)' },
  { value: 'Asia/Shanghai', label: 'Asia/Shanghai (CST - China)' },
  { value: 'Asia/Hong_Kong', label: 'Asia/Hong_Kong (HKT)' },
  { value: 'Asia/Singapore', label: 'Asia/Singapore (SGT)' },
  { value: 'Asia/Dubai', label: 'Asia/Dubai (GST - UAE)' },
  { value: 'Asia/Kolkata', label: 'Asia/Kolkata (IST - India)' },
  { value: 'Australia/Sydney', label: 'Australia/Sydney (AEDT/AEST)' },
  { value: 'Pacific/Auckland', label: 'Pacific/Auckland (NZDT/NZST - New Zealand)' },
  { value: 'Africa/Johannesburg', label: 'Africa/Johannesburg (SAST - South Africa)' }
]

const generatedConfig = computed(() => {
  const extends_array = [
    'config:recommended',
    ':enableVulnerabilityAlerts',
    ':dependencyDashboard'
  ]

  if (config.value.semanticCommits) {
    extends_array.push(':semanticCommits')
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const configObject: Record<string, any> = {
    $schema: 'https://docs.renovatebot.com/renovate-schema.json',
    extends: extends_array
  }

  if (config.value.timezone) {
    configObject.timezone = config.value.timezone
  }

  // Add schedule if not default
  if (config.value.schedule !== 'at-any-time') {
    const scheduleMap: Record<string, string[]> = {
      'weekly': ['before 5am on monday'],
      'monthly': ['before 5am on the first day of the month'],
      'weekends': ['every weekend'],
      'outside-business-hours': ['after 6pm every weekday', 'before 9am every weekday', 'every weekend']
    }
    configObject.schedule = scheduleMap[config.value.schedule]
  }

  // Add PR limits based on strategy
  if (config.value.prLimitStrategy === 'conservative') {
    configObject.prHourlyLimit = 1
    configObject.prConcurrentLimit = 3
  } else if (config.value.prLimitStrategy === 'active') {
    configObject.prHourlyLimit = 10
    configObject.prConcurrentLimit = 20
  }
  // 'default' strategy doesn't add anything (uses Renovate defaults: 2 and 10)

  // Add rebaseWhen if not default
  if (config.value.rebaseWhen !== 'auto') {
    configObject.rebaseWhen = config.value.rebaseWhen
  }

  // Add rangeStrategy if not default
  if (config.value.rangeStrategy !== 'auto') {
    configObject.rangeStrategy = config.value.rangeStrategy
  }

  // Add automergeType if not default
  if (config.value.automergeType !== 'pr') {
    configObject.automergeType = config.value.automergeType
  }

  // Add automerge configuration based on level
  if (config.value.automergeLevel === 'all') {
    // Global automerge for all dependencies and all update types
    configObject.automerge = true
  } else if (config.value.automergeLevel !== 'disabled' || config.value.automergeDevDependencies) {
    // Use packageRules for selective automerge
    configObject.packageRules = configObject.packageRules || []

    // Add rule for general automerge level (production dependencies)
    if (config.value.automergeLevel !== 'disabled') {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const rule: Record<string, any> = {
        automerge: true
      }

      // Add update type matching
      if (config.value.automergeLevel === 'patch') {
        rule.matchUpdateTypes = ['patch']
      } else if (config.value.automergeLevel === 'minor') {
        rule.matchUpdateTypes = ['minor', 'patch']
      }

      configObject.packageRules.push(rule)
    }

    // Add separate rule for dev dependencies if enabled
    if (config.value.automergeDevDependencies) {
      // Rule for npm-style devDependencies
      configObject.packageRules.push({
        matchDepTypes: ['devDependencies'],
        automerge: true
      })
      // Rule for build tool managers (maven-wrapper, gradle-wrapper, etc.)
      configObject.packageRules.push({
        matchManagers: ['maven-wrapper', 'gradle-wrapper'],
        automerge: true
      })
    }
  }

  // Add rule to disable automerge for pre-1.0.0 versions
  if (config.value.disablePreOneAutomerge && config.value.automergeLevel !== 'disabled') {
    configObject.packageRules = configObject.packageRules || []
    configObject.packageRules.push({
      matchCurrentVersion: '/^0\\./',
      automerge: false
    })
  }

  // Add ignoreTests if enabled
  if (config.value.ignoreTests) {
    configObject.ignoreTests = true
  }

  // Add minimumReleaseAge if not default
  if (config.value.minimumReleaseAge !== 'never') {
    const ageMap: Record<string, string> = {
      '3-days': '3 days',
      '7-days': '7 days',
      '14-days': '14 days'
    }
    configObject.minimumReleaseAge = ageMap[config.value.minimumReleaseAge]

    // Security best practice: Explicitly bypass minimumReleaseAge for vulnerability alerts
    // This ensures security patches are applied immediately regardless of the age delay
    configObject.packageRules = configObject.packageRules || []
    configObject.packageRules.push({
      description: 'Security updates should not wait for release age',
      isVulnerabilityAlert: true,
      minimumReleaseAge: '0 days'
    })
  }

  // Add lockFileMaintenance if enabled
  if (config.value.lockFileMaintenance.enabled) {
    configObject.lockFileMaintenance = {
      enabled: true
    }

    // Use the dedicated lock file maintenance schedule
    if (config.value.lockFileMaintenance.schedule !== 'at-any-time') {
      const lockFileScheduleMap: Record<string, string[]> = {
        'weekly': ['before 5am on monday'],
        'monthly': ['before 5am on the first day of the month'],
        'weekends': ['every weekend'],
        'outside-business-hours': ['after 6pm every weekday', 'before 9am every weekday', 'every weekend']
      }
      configObject.lockFileMaintenance.schedule = lockFileScheduleMap[config.value.lockFileMaintenance.schedule]
    } else {
      // Use "at any time" to override the default weekly schedule
      configObject.lockFileMaintenance.schedule = ['at any time']
    }

    // Add automerge if enabled
    if (config.value.lockFileMaintenance.automerge) {
      configObject.lockFileMaintenance.automerge = true
    }
  }

  // Add vulnerabilityAlerts customization if any options are set
  if (config.value.vulnerabilityAlerts.labels || config.value.vulnerabilityAlerts.scheduleOverride || config.value.vulnerabilityAlerts.automerge) {
    configObject.vulnerabilityAlerts = {}

    // Add labels if provided
    if (config.value.vulnerabilityAlerts.labels) {
      const labelsArray = config.value.vulnerabilityAlerts.labels
        .split(',')
        .map(label => label.trim())
        .filter(label => label.length > 0)

      if (labelsArray.length > 0) {
        configObject.vulnerabilityAlerts.labels = labelsArray
      }
    }

    // Add schedule override if enabled
    if (config.value.vulnerabilityAlerts.scheduleOverride) {
      configObject.vulnerabilityAlerts.schedule = ['at any time']
    }

    // Add automerge if enabled
    if (config.value.vulnerabilityAlerts.automerge) {
      configObject.vulnerabilityAlerts.automerge = true
    }
  }

  // Add package manager grouping
  const groupingMap: Record<string, { managers: string[]; groupName: string }> = {
    npm: { managers: ['npm'], groupName: 'npm dependencies' },
    docker: { managers: ['dockerfile', 'docker-compose'], groupName: 'Docker dependencies' },
    maven: { managers: ['maven'], groupName: 'Maven dependencies' },
    gradle: { managers: ['gradle', 'gradle-wrapper'], groupName: 'Gradle dependencies' },
    pip: { managers: ['pip_requirements', 'pip_setup', 'pipenv'], groupName: 'pip dependencies' },
    composer: { managers: ['composer'], groupName: 'Composer dependencies' },
    helm: { managers: ['helmv3', 'helmfile'], groupName: 'Helm dependencies' },
    githubActions: { managers: ['github-actions'], groupName: 'GitHub Actions' },
    terraform: { managers: ['terraform', 'terragrunt'], groupName: 'Terraform dependencies' },
    gomod: { managers: ['gomod'], groupName: 'Go dependencies' },
    cargo: { managers: ['cargo'], groupName: 'Cargo dependencies' },
    bundler: { managers: ['bundler'], groupName: 'Bundler dependencies' },
    nuget: { managers: ['nuget'], groupName: 'NuGet dependencies' }
  }

  const enabledGroups = Object.entries(config.value.grouping)
    .filter(([_, enabled]) => enabled)
    .map(([key, _]) => key)

  if (enabledGroups.length > 0) {
    configObject.packageRules = configObject.packageRules || []

    enabledGroups.forEach(groupKey => {
      const group = groupingMap[groupKey as keyof typeof groupingMap]
      if (group) {
        configObject.packageRules.push({
          matchManagers: group.managers,
          groupName: group.groupName
        })
      }
    })
  }

  return JSON.stringify(configObject, null, 2)
})

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

// Parse renovate.json and map it back to our config structure
const parseRenovateJson = (jsonString: string): Partial<RenovateConfig> => {
  const parsed = JSON.parse(jsonString)
  const result: Partial<RenovateConfig> = {}

  // Parse extends for semantic commits
  if (parsed.extends && Array.isArray(parsed.extends)) {
    result.semanticCommits = parsed.extends.includes(':semanticCommits')
  }

  // Parse timezone
  if (parsed.timezone) {
    result.timezone = parsed.timezone
  }

  // Parse schedule
  if (parsed.schedule) {
    const scheduleStr = Array.isArray(parsed.schedule) ? parsed.schedule.join(', ') : parsed.schedule
    if (scheduleStr.includes('monday') && scheduleStr.includes('5am')) {
      result.schedule = 'weekly'
    } else if (scheduleStr.includes('first day of the month')) {
      result.schedule = 'monthly'
    } else if (scheduleStr.includes('weekend') && !scheduleStr.includes('weekday')) {
      result.schedule = 'weekends'
    } else if (scheduleStr.includes('6pm') || scheduleStr.includes('9am')) {
      result.schedule = 'outside-business-hours'
    } else {
      result.schedule = 'at-any-time'
    }
  }

  // Parse PR limits
  if (parsed.prHourlyLimit === 1 && parsed.prConcurrentLimit === 3) {
    result.prLimitStrategy = 'conservative'
  } else if (parsed.prHourlyLimit === 10 && parsed.prConcurrentLimit === 20) {
    result.prLimitStrategy = 'active'
  } else if (parsed.prHourlyLimit !== undefined || parsed.prConcurrentLimit !== undefined) {
    // Custom values - pick closest match
    if ((parsed.prHourlyLimit || 2) <= 2 && (parsed.prConcurrentLimit || 10) <= 5) {
      result.prLimitStrategy = 'conservative'
    } else if ((parsed.prHourlyLimit || 2) >= 5) {
      result.prLimitStrategy = 'active'
    } else {
      result.prLimitStrategy = 'default'
    }
  }

  // Parse rebaseWhen
  if (parsed.rebaseWhen) {
    result.rebaseWhen = parsed.rebaseWhen
  }

  // Parse rangeStrategy
  if (parsed.rangeStrategy) {
    result.rangeStrategy = parsed.rangeStrategy
  }

  // Parse automergeType
  if (parsed.automergeType) {
    result.automergeType = parsed.automergeType
  }

  // Parse ignoreTests
  if (parsed.ignoreTests) {
    result.ignoreTests = parsed.ignoreTests
  }

  // Parse minimumReleaseAge
  if (parsed.minimumReleaseAge) {
    const age = parsed.minimumReleaseAge.toLowerCase()
    if (age.includes('3')) {
      result.minimumReleaseAge = '3-days'
    } else if (age.includes('7') || age.includes('1 week')) {
      result.minimumReleaseAge = '7-days'
    } else if (age.includes('14') || age.includes('2 week')) {
      result.minimumReleaseAge = '14-days'
    }
  }

  // Parse automerge settings
  if (parsed.automerge === true) {
    result.automergeLevel = 'all'
  }

  // Parse packageRules for automerge settings and grouping
  if (parsed.packageRules && Array.isArray(parsed.packageRules)) {
    // Initialize grouping
    result.grouping = {
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

    for (const rule of parsed.packageRules) {
      // Check for automerge rules
      if (rule.automerge === true) {
        if (rule.matchUpdateTypes) {
          const types = rule.matchUpdateTypes
          if (types.includes('minor') && types.includes('patch')) {
            result.automergeLevel = 'minor'
          } else if (types.includes('patch') && !types.includes('minor')) {
            result.automergeLevel = 'patch'
          }
        }
        if (rule.matchDepTypes && rule.matchDepTypes.includes('devDependencies')) {
          result.automergeDevDependencies = true
        }
      }

      // Check for pre-1.0.0 disable rule
      if (rule.matchCurrentVersion && rule.matchCurrentVersion.includes('^0\\.') && rule.automerge === false) {
        result.disablePreOneAutomerge = true
      }

      // Check for grouping rules
      if (rule.matchManagers && rule.groupName) {
        const managers = rule.matchManagers
        if (managers.includes('npm')) result.grouping!.npm = true
        if (managers.includes('dockerfile') || managers.includes('docker-compose')) result.grouping!.docker = true
        if (managers.includes('maven')) result.grouping!.maven = true
        if (managers.includes('gradle') || managers.includes('gradle-wrapper')) result.grouping!.gradle = true
        if (managers.includes('pip_requirements') || managers.includes('pip_setup') || managers.includes('pipenv')) result.grouping!.pip = true
        if (managers.includes('composer')) result.grouping!.composer = true
        if (managers.includes('helmv3') || managers.includes('helmfile')) result.grouping!.helm = true
        if (managers.includes('github-actions')) result.grouping!.githubActions = true
        if (managers.includes('terraform') || managers.includes('terragrunt')) result.grouping!.terraform = true
        if (managers.includes('gomod')) result.grouping!.gomod = true
        if (managers.includes('cargo')) result.grouping!.cargo = true
        if (managers.includes('bundler')) result.grouping!.bundler = true
        if (managers.includes('nuget')) result.grouping!.nuget = true
      }
    }
  }

  // Parse lockFileMaintenance
  if (parsed.lockFileMaintenance) {
    result.lockFileMaintenance = {
      enabled: parsed.lockFileMaintenance.enabled !== false,
      schedule: 'at-any-time',
      automerge: parsed.lockFileMaintenance.automerge === true
    }

    if (parsed.lockFileMaintenance.schedule) {
      const scheduleStr = Array.isArray(parsed.lockFileMaintenance.schedule)
        ? parsed.lockFileMaintenance.schedule.join(', ')
        : parsed.lockFileMaintenance.schedule
      if (scheduleStr.includes('monday') && scheduleStr.includes('5am')) {
        result.lockFileMaintenance.schedule = 'weekly'
      } else if (scheduleStr.includes('first day of the month')) {
        result.lockFileMaintenance.schedule = 'monthly'
      } else if (scheduleStr.includes('weekend') && !scheduleStr.includes('weekday')) {
        result.lockFileMaintenance.schedule = 'weekends'
      } else if (scheduleStr.includes('6pm') || scheduleStr.includes('9am')) {
        result.lockFileMaintenance.schedule = 'outside-business-hours'
      }
    }
  }

  // Parse vulnerabilityAlerts
  if (parsed.vulnerabilityAlerts) {
    result.vulnerabilityAlerts = {
      labels: '',
      scheduleOverride: false,
      automerge: false
    }

    if (parsed.vulnerabilityAlerts.labels && Array.isArray(parsed.vulnerabilityAlerts.labels)) {
      result.vulnerabilityAlerts.labels = parsed.vulnerabilityAlerts.labels.join(', ')
    }

    if (parsed.vulnerabilityAlerts.schedule) {
      const scheduleStr = Array.isArray(parsed.vulnerabilityAlerts.schedule)
        ? parsed.vulnerabilityAlerts.schedule.join(', ')
        : parsed.vulnerabilityAlerts.schedule
      if (scheduleStr.includes('any time')) {
        result.vulnerabilityAlerts.scheduleOverride = true
      }
    }

    if (parsed.vulnerabilityAlerts.automerge === true) {
      result.vulnerabilityAlerts.automerge = true
    }
  }

  return result
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

// Import from GitHub URL
const importFromUrl = async () => {
  importError.value = ''

  if (!importUrl.value.trim()) {
    importError.value = 'Please enter a GitHub URL'
    return
  }

  isImporting.value = true

  try {
    const rawUrl = convertToRawGitHubUrl(importUrl.value.trim())
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
  if (imported.minimumReleaseAge !== undefined) config.value.minimumReleaseAge = imported.minimumReleaseAge

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

const getBackendUrl = () => {
  if (typeof window === 'undefined') {
    // Server-side rendering
    return 'http://localhost:8080/api'
  }

  // Client-side
  if (window.location.hostname === 'localhost') {
    return 'http://localhost:8080/api'
  }

  // Production: use same protocol, domain, port with /api path
  return `${window.location.protocol}//${window.location.host}/api`
}

const getFeedback = async () => {
  isLoadingFeedback.value = true
  feedback.value = null

  try {
    const backendUrl = getBackendUrl()
    const response = await fetch(`${backendUrl}/feedback`, {
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
</script>

<style scoped>
code {
  font-family: 'Courier New', monospace;
}
</style>
