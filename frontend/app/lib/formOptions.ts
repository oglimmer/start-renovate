// Presentational option data for the configurator form (pages/index.vue). Extracted so the page
// template can render the repetitive radio groups, the schedule dropdowns and the per-manager
// grouping grid with a single v-for each, instead of dozens of copy-pasted blocks. Pure display
// copy — the values mirror the RenovateConfig field unions so the bindings stay type-checked.
import type { RenovateConfig } from './generateRenovateConfig'

export interface RadioOption<T extends string> {
  value: T
  label: string
  // May contain trusted static markup (e.g. <code> in the range examples); rendered with v-html.
  description: string
}

export const prLimitOptions: RadioOption<RenovateConfig['prLimitStrategy']>[] = [
  {
    value: 'default',
    label: 'Balanced',
    description:
      "Renovate's default settings: Up to 2 PRs per hour, max 10 open PRs at once. Good for most teams with regular review capacity."
  },
  {
    value: 'conservative',
    label: 'Minimal Noise',
    description:
      'Conservative limits: 1 PR per hour, max 3 open PRs. Perfect for small teams or projects with limited CI resources.'
  },
  {
    value: 'active',
    label: 'Fast Updates',
    description:
      'Aggressive limits: 10 PRs per hour, max 20 open PRs. For teams that can quickly review and merge updates, or have robust CI pipelines.'
  }
]

export const rebaseOptions: RadioOption<RenovateConfig['rebaseWhen']>[] = [
  {
    value: 'auto',
    label: 'Smart',
    description:
      "Renovate's default: Auto-detects the best strategy. Keeps branches up-to-date if automerge is enabled or your repo requires it."
  },
  {
    value: 'conflicted',
    label: 'Only When Conflicted',
    description:
      'Rebase only when merge conflicts occur. Reduces CI load if you have many concurrent PRs. Good for busy repositories.'
  },
  {
    value: 'behind-base-branch',
    label: 'Always Keep Up-to-Date',
    description:
      'Rebase whenever the branch is behind base by 1+ commits. Ensures PRs are always current, but increases CI runs.'
  },
  {
    value: 'never',
    label: 'Manual Only',
    description:
      "Never automatically rebase. You'll need to manually trigger rebases. Not recommended for most use cases."
  }
]

export const rangeOptions: RadioOption<RenovateConfig['rangeStrategy']>[] = [
  {
    value: 'auto',
    label: 'Smart',
    description: 'Let Renovate decide the best strategy based on your project type and dependencies.'
  },
  {
    value: 'replace',
    label: 'Replace When Needed',
    description:
      'Only update ranges when new version falls outside. Example: <code class="bg-gray-100 px-1 rounded">^1.0.0</code> → <code class="bg-gray-100 px-1 rounded">^2.0.0</code>. Conservative approach.'
  },
  {
    value: 'bump',
    label: 'Always Bump Range',
    description:
      'Explicitly update ranges for every change. Example: <code class="bg-gray-100 px-1 rounded">^1.0.0</code> → <code class="bg-gray-100 px-1 rounded">^1.0.1</code>. Makes all updates visible in package.json.'
  },
  {
    value: 'update-lockfile',
    label: 'Lockfile Only',
    description:
      'Update lockfiles without changing package.json ranges. Ranges stay as-is, only resolved versions change. Good for keeping stable ranges.'
  },
  {
    value: 'pin',
    label: 'Pin to Exact Versions',
    description:
      'Remove all ranges and use exact versions. Example: <code class="bg-gray-100 px-1 rounded">^1.0.0</code> → <code class="bg-gray-100 px-1 rounded">1.0.1</code>. Maximum control and reproducibility.'
  }
]

export const automergeTypeOptions: RadioOption<RenovateConfig['automergeType']>[] = [
  {
    value: 'pr',
    label: 'Pull Request',
    description:
      'Create PRs and automerge them after tests pass. Full visibility with notifications. Works with all branch protection rules.'
  },
  {
    value: 'branch',
    label: 'Silent Branch Merge',
    description:
      'Merge directly to base branch without creating PRs. Silent updates with only commits visible. Creates PR only if tests fail. Requires branch protection to allow Renovate pushes.'
  },
  {
    value: 'pr-comment',
    label: 'External Merge Bot',
    description:
      'Use with external merge bots like bors-ng. Renovate adds a comment to trigger your bot. Only use if you have a merge bot configured.'
  }
]

export const automergeLevelOptions: RadioOption<RenovateConfig['automergeLevel']>[] = [
  {
    value: 'disabled',
    label: 'Disabled',
    description:
      'No automatic merging. All updates require manual review and approval. Safest option with full control.'
  },
  {
    value: 'patch',
    label: 'Patch Updates Only',
    description:
      'Automerge only patch updates (e.g., 1.2.3 → 1.2.4). Most conservative automated approach. Good for risk-averse teams with good test coverage.'
  },
  {
    value: 'minor',
    label: 'Patch + Minor Updates',
    description:
      'Automerge patch and minor updates (e.g., 1.2.3 → 1.3.0). Most common recommendation. Major updates still require manual review.'
  },
  {
    value: 'all',
    label: 'All Updates',
    description:
      'Automerge all updates including major versions. Aggressive approach requiring excellent test coverage. Only use if you have comprehensive automated tests.'
  }
]

export const releaseAgeOptions: RadioOption<RenovateConfig['minimumReleaseAge']>[] = [
  {
    value: 'never',
    label: 'No Delay',
    description:
      "Update to new releases immediately. Renovate's default behavior. Get updates as soon as they're available."
  },
  {
    value: '3-days',
    label: '3 Days',
    description:
      'Wait 3 days after release. Short stabilization period to catch immediate bugs. Good balance for most teams.'
  },
  {
    value: '7-days',
    label: '7 Days (1 Week)',
    description:
      'Wait 1 week after release. Gives the community time to discover issues. Recommended for stability-focused teams.'
  },
  {
    value: '14-days',
    label: '14 Days (2 Weeks)',
    description:
      'Wait 2 weeks after release. Conservative approach for maximum stability. Best for risk-averse production environments.'
  }
]

// Shared by the global update schedule and the lock-file maintenance schedule (same vocabulary).
export const scheduleOptions: { value: RenovateConfig['schedule']; label: string }[] = [
  { value: 'at-any-time', label: 'At Any Time - Default' },
  { value: 'weekly', label: 'Once per Week (Monday before 5am)' },
  { value: 'monthly', label: 'Once per Month (First day before 5am)' },
  { value: 'weekends', label: 'Weekends Only' },
  { value: 'outside-business-hours', label: 'Outside Business Hours (Evenings + Weekends)' }
]

// Per-manager grouping toggles. `key` indexes RenovateConfig.grouping; label is the display name.
export const managerGroups: { key: keyof RenovateConfig['grouping']; label: string }[] = [
  { key: 'npm', label: 'npm (JavaScript)' },
  { key: 'docker', label: 'Docker' },
  { key: 'maven', label: 'Maven (Java)' },
  { key: 'gradle', label: 'Gradle (Java)' },
  { key: 'pip', label: 'pip (Python)' },
  { key: 'composer', label: 'Composer (PHP)' },
  { key: 'helm', label: 'Helm (Kubernetes)' },
  { key: 'githubActions', label: 'GitHub Actions' },
  { key: 'terraform', label: 'Terraform' },
  { key: 'gomod', label: 'Go Modules' },
  { key: 'cargo', label: 'Cargo (Rust)' },
  { key: 'bundler', label: 'Bundler (Ruby)' },
  { key: 'nuget', label: 'NuGet (.NET)' }
]
