// Display catalog for the comparison matrix. The `id` of every option MUST match the optionId the
// backend RenovateConfigAnalyzer emits (the dotted RenovateConfig field paths), so the two never
// drift. Order here is the render order in the matrix.

export interface DashboardOption {
  id: string
  label: string
}

export interface DashboardOptionGroup {
  group: string
  options: DashboardOption[]
}

export const dashboardOptionGroups: DashboardOptionGroup[] = [
  {
    group: 'General',
    options: [
      { id: 'semanticCommits', label: 'Semantic commits' },
      { id: 'timezone', label: 'Timezone' },
      { id: 'prLimitStrategy', label: 'PR limit strategy' },
      { id: 'rebaseWhen', label: 'Rebase when' },
      { id: 'rangeStrategy', label: 'Range strategy' },
      { id: 'minimumReleaseAge', label: 'Minimum release age' },
      { id: 'ignoreTests', label: 'Ignore tests' },
      { id: 'flagAbandonedPackages', label: 'Flag abandoned packages' },
    ],
  },
  {
    group: 'Scheduling',
    options: [{ id: 'schedule', label: 'Schedule' }],
  },
  {
    group: 'Automerge',
    options: [
      { id: 'automergeType', label: 'Automerge type' },
      { id: 'automergeLevel', label: 'Automerge level' },
      { id: 'automergeDevDependencies', label: 'Automerge devDependencies' },
      { id: 'disablePreOneAutomerge', label: 'Disable pre-1.0 automerge' },
      { id: 'requireMajorApproval', label: 'Require major approval' },
    ],
  },
  {
    group: 'Digest pinning',
    options: [
      { id: 'pinning.dockerDigests', label: 'Docker digests' },
      { id: 'pinning.githubActionDigests', label: 'GitHub Action digests' },
      { id: 'pinning.devDependencies', label: 'Dev dependencies' },
    ],
  },
  {
    group: 'Lock file maintenance',
    options: [
      { id: 'lockFileMaintenance.enabled', label: 'Enabled' },
      { id: 'lockFileMaintenance.schedule', label: 'Schedule' },
      { id: 'lockFileMaintenance.automerge', label: 'Automerge' },
    ],
  },
  {
    group: 'Vulnerability alerts',
    options: [
      { id: 'vulnerabilityAlerts.labels', label: 'Labels' },
      { id: 'vulnerabilityAlerts.scheduleOverride', label: 'Schedule override' },
      { id: 'vulnerabilityAlerts.automerge', label: 'Automerge' },
    ],
  },
  {
    group: 'Grouping',
    options: [
      { id: 'grouping.npm', label: 'npm' },
      { id: 'grouping.docker', label: 'Docker' },
      { id: 'grouping.maven', label: 'Maven' },
      { id: 'grouping.gradle', label: 'Gradle' },
      { id: 'grouping.pip', label: 'pip' },
      { id: 'grouping.composer', label: 'Composer' },
      { id: 'grouping.helm', label: 'Helm' },
      { id: 'grouping.githubActions', label: 'GitHub Actions' },
      { id: 'grouping.terraform', label: 'Terraform' },
      { id: 'grouping.gomod', label: 'Go modules' },
      { id: 'grouping.cargo', label: 'Cargo' },
      { id: 'grouping.bundler', label: 'Bundler' },
      { id: 'grouping.nuget', label: 'NuGet' },
    ],
  },
]

export type CellStateValue = 'SET_ON' | 'SET_OFF' | 'UNSET' | 'CUSTOM'

export interface CellState {
  state: CellStateValue
  value: string | null
}

export interface RepoDashboardEntry {
  fullName: string
  hasRenovate: boolean
  configFilePath: string | null
  options: Record<string, CellState>
  error: string | null
}

export interface DashboardResponse {
  repos: RepoDashboardEntry[]
}

export interface RepoSummary {
  fullName: string
  name: string
  owner: string
  isPrivate: boolean
  defaultBranch: string | null
  enabled: boolean
}
