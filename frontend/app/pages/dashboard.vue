<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-10 px-4 sm:px-6 lg:px-8">
    <div class="max-w-7xl mx-auto">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <div>
          <h1 class="text-3xl font-bold text-gray-900">Renovate Dashboard</h1>
          <p class="text-gray-600 text-sm mt-1">
            Compare Renovate configuration across your projects and spot what's out of alignment.
          </p>
        </div>
        <div class="flex items-center gap-4">
          <NuxtLink to="/" class="text-sm text-indigo-600 hover:text-indigo-800 underline">
            ← Config generator
          </NuxtLink>
          <div v-if="user" class="flex items-center gap-2">
            <img v-if="user.avatarUrl" :src="user.avatarUrl" :alt="user.login" class="h-8 w-8 rounded-full">
            <span class="text-sm text-gray-700">{{ user.login }}</span>
            <button class="text-sm text-gray-500 hover:text-gray-700 underline" @click="onLogout">
              Logout
            </button>
          </div>
        </div>
      </div>

      <!-- Loading auth -->
      <div v-if="loading" class="text-center text-gray-500 py-20">Checking session…</div>

      <!-- Logged out -->
      <div v-else-if="!user" class="max-w-md mx-auto bg-white shadow-xl rounded-lg p-8 text-center">
        <h2 class="text-xl font-semibold text-gray-900 mb-2">Connect GitHub</h2>
        <p class="text-gray-600 text-sm mb-6">
          Sign in with GitHub to choose repositories and compare their Renovate configuration.
        </p>
        <button
          class="inline-flex items-center gap-2 px-5 py-2.5 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors"
          @click="login"
        >
          <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path fill-rule="evenodd" clip-rule="evenodd" d="M12 2C6.48 2 2 6.58 2 12.26c0 4.5 2.87 8.32 6.84 9.67.5.1.68-.22.68-.49l-.01-1.7c-2.78.62-3.37-1.37-3.37-1.37-.46-1.18-1.11-1.5-1.11-1.5-.91-.63.07-.62.07-.62 1 .07 1.53 1.06 1.53 1.06.9 1.57 2.36 1.12 2.94.86.09-.67.35-1.12.63-1.38-2.22-.26-4.55-1.14-4.55-5.07 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.3.1-2.7 0 0 .84-.28 2.75 1.05A9.36 9.36 0 0 1 12 6.84c.85 0 1.71.12 2.51.34 1.91-1.33 2.75-1.05 2.75-1.05.55 1.4.2 2.44.1 2.7.64.72 1.03 1.63 1.03 2.75 0 3.94-2.34 4.81-4.57 5.06.36.32.68.94.68 1.9l-.01 2.82c0 .27.18.6.69.49A10.02 10.02 0 0 0 22 12.26C22 6.58 17.52 2 12 2Z" />
          </svg>
          Login with GitHub
        </button>
      </div>

      <!-- Authenticated -->
      <div v-else class="grid grid-cols-1 lg:grid-cols-4 gap-6">
        <!-- Repo picker -->
        <div class="lg:col-span-1 bg-white shadow rounded-lg p-4 h-fit">
          <h2 class="text-sm font-semibold text-gray-900 mb-3">
            Repositories
            <span class="text-gray-400 font-normal">({{ enabledCount }} tracked)</span>
          </h2>
          <input
            v-model="repoFilter"
            type="text"
            placeholder="Filter…"
            class="w-full px-3 py-1.5 mb-3 text-sm border border-gray-300 rounded focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
          >
          <div v-if="reposLoading" class="text-sm text-gray-400 py-4 text-center">Loading repos…</div>
          <div v-else class="max-h-[28rem] overflow-y-auto -mr-1 pr-1 space-y-1">
            <label
              v-for="repo in filteredRepos"
              :key="repo.fullName"
              class="flex items-center gap-2 px-2 py-1.5 rounded hover:bg-gray-50 cursor-pointer text-sm"
            >
              <input
                type="checkbox"
                :checked="repo.enabled"
                :disabled="busyRepos.has(repo.fullName)"
                class="h-4 w-4 text-indigo-600 rounded border-gray-300 focus:ring-indigo-500"
                @change="toggleRepo(repo)"
              >
              <span class="truncate text-gray-700 flex-1 min-w-0" :title="repo.fullName">{{ repo.fullName }}</span>
              <span
                v-if="repo.hasRenovateConfig"
                class="shrink-0 text-[10px] font-medium text-green-600"
                title="Already has a Renovate config file"
              >✓ config</span>
              <span v-if="repo.isPrivate" class="shrink-0 text-[10px] uppercase tracking-wide text-gray-400">private</span>
            </label>
            <div v-if="filteredRepos.length === 0" class="text-sm text-gray-400 py-4 text-center">
              No repositories match.
            </div>
          </div>
        </div>

        <!-- Matrix -->
        <div class="lg:col-span-3 bg-white shadow rounded-lg p-4 overflow-hidden">
          <div class="flex items-center justify-between mb-3">
            <h2 class="text-sm font-semibold text-gray-900">Configuration comparison</h2>
            <label class="flex items-center gap-2 text-sm text-gray-600 cursor-pointer">
              <input v-model="differencesOnly" type="checkbox" class="h-4 w-4 text-indigo-600 rounded border-gray-300 focus:ring-indigo-500">
              Differences only
            </label>
          </div>

          <div v-if="dashboardLoading" class="text-sm text-gray-400 py-10 text-center">Loading configurations…</div>

          <div v-else-if="enabledEntries.length === 0" class="text-sm text-gray-400 py-10 text-center">
            Enable one or more repositories on the left to compare them.
          </div>

          <div v-else class="overflow-x-auto">
            <table class="text-sm border-collapse">
              <thead>
                <tr>
                  <th class="sticky left-0 z-10 bg-white text-left font-medium text-gray-500 px-3 py-2 align-bottom min-w-[12rem]">
                    Option
                  </th>
                  <th
                    v-for="entry in enabledEntries"
                    :key="entry.fullName"
                    class="px-3 py-2 text-left font-medium text-gray-700 whitespace-nowrap"
                  >
                    <div class="flex flex-col">
                      <a
                        :href="`https://github.com/${entry.fullName}`"
                        target="_blank"
                        rel="noopener noreferrer"
                        :title="entry.fullName"
                        class="text-indigo-600 hover:text-indigo-800 hover:underline"
                      >{{ shortName(entry.fullName) }}</a>
                      <span v-if="!entry.hasRenovate" class="text-[10px] font-normal text-amber-500">no config</span>
                      <span v-else-if="entry.error" class="text-[10px] font-normal text-red-500" :title="entry.error">parse error</span>
                      <span v-else class="text-[10px] font-normal text-gray-400">{{ entry.configFilePath }}</span>
                    </div>
                  </th>
                </tr>
              </thead>
              <tbody>
                <template v-for="group in visibleGroups" :key="group.group">
                  <tr>
                    <td
                      :colspan="enabledEntries.length + 1"
                      class="sticky left-0 bg-gradient-to-r from-indigo-50 to-transparent border-l-2 border-indigo-400 text-[11px] uppercase tracking-wider text-indigo-700 font-semibold px-3 py-1.5"
                    >
                      {{ group.group }}
                    </td>
                  </tr>
                  <tr
                    v-for="opt in group.options"
                    :key="opt.id"
                    class="border-b border-gray-100 hover:bg-indigo-50/40"
                    :class="{ 'bg-amber-50/60': differencesHighlight && rowDiffers(opt.id) }"
                  >
                    <td class="sticky left-0 z-10 bg-inherit text-gray-700 px-3 py-1.5 whitespace-nowrap">
                      {{ opt.label }}
                    </td>
                    <td
                      v-for="entry in enabledEntries"
                      :key="entry.fullName + opt.id"
                      class="px-3 py-1.5 text-center"
                    >
                      <span :class="cell(entry, opt.id).cls" :title="cell(entry, opt.id).title" class="font-medium">
                        {{ cell(entry, opt.id).symbol }}</span>
                      <span v-if="cell(entry, opt.id).text" class="ml-1 text-xs text-gray-500">{{ cell(entry, opt.id).text }}</span>
                    </td>
                  </tr>
                </template>
              </tbody>
            </table>

            <!-- Legend -->
            <div class="flex flex-wrap gap-4 mt-4 text-xs text-gray-500">
              <span><span class="text-green-600 font-medium">✓</span> configured / on</span>
              <span><span class="text-gray-400 font-medium">✗</span> off</span>
              <span><span class="text-gray-300 font-medium">–</span> not set (Renovate default)</span>
              <span><span class="text-amber-500 font-medium">?</span> custom (can't determine)</span>
              <span><span class="text-gray-300 font-medium">·</span> no Renovate config</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { dashboardOptionGroups } from '~/lib/dashboardOptions'
import type { CellState, DashboardResponse, RepoDashboardEntry, RepoSummary } from '~/lib/dashboardOptions'

const { request } = useApi()
const { user, loading, fetchMe, login, logout } = useAuth()

const repos = ref<RepoSummary[]>([])
const reposLoading = ref(false)
const repoFilter = ref('')
const busyRepos = ref<Set<string>>(new Set())

const dashboard = ref<DashboardResponse | null>(null)
const dashboardLoading = ref(false)
const differencesOnly = ref(false)

const enabledEntries = computed<RepoDashboardEntry[]>(() => dashboard.value?.repos ?? [])
const enabledCount = computed(() => repos.value.filter(r => r.enabled).length)
const differencesHighlight = computed(() => enabledEntries.value.length >= 2)

const filteredRepos = computed(() => {
  const q = repoFilter.value.trim().toLowerCase()
  if (!q) return repos.value
  return repos.value.filter(r => r.fullName.toLowerCase().includes(q))
})

const visibleGroups = computed(() => {
  if (!differencesOnly.value) return dashboardOptionGroups
  return dashboardOptionGroups
    .map(g => ({ group: g.group, options: g.options.filter(o => rowDiffers(o.id)) }))
    .filter(g => g.options.length > 0)
})

function shortName(fullName: string): string {
  const parts = fullName.split('/')
  return parts.length > 1 ? parts[1]! : fullName
}

function rowDiffers(id: string): boolean {
  const entries = enabledEntries.value
  if (entries.length < 2) return false
  const keys = entries.map(e => {
    const c = e.options[id]
    if (!e.hasRenovate || !c) return 'absent'
    return `${c.state}|${c.value ?? ''}`
  })
  return new Set(keys).size > 1
}

interface CellDisplay {
  symbol: string
  cls: string
  title: string
  text: string
}

function cell(entry: RepoDashboardEntry, id: string): CellDisplay {
  if (!entry.hasRenovate) {
    return { symbol: '·', cls: 'text-gray-300', title: 'No Renovate config', text: '' }
  }
  const c: CellState | undefined = entry.options[id]
  if (!c) return { symbol: '·', cls: 'text-gray-300', title: 'n/a', text: '' }
  switch (c.state) {
    case 'SET_ON':
      return { symbol: '✓', cls: 'text-green-600', title: c.value ?? 'on', text: c.value ?? '' }
    case 'SET_OFF':
      return { symbol: '✗', cls: 'text-gray-400', title: 'off', text: '' }
    case 'UNSET':
      return { symbol: '–', cls: 'text-gray-300', title: 'not set (Renovate default)', text: '' }
    case 'CUSTOM':
      return { symbol: '?', cls: 'text-amber-500', title: c.value ? `custom: ${c.value}` : 'custom', text: '' }
    default:
      return { symbol: '·', cls: 'text-gray-300', title: '', text: '' }
  }
}

async function loadRepos(): Promise<void> {
  reposLoading.value = true
  try {
    const res = await request('/repos')
    repos.value = res.ok ? ((await res.json()) as RepoSummary[]) : []
  } finally {
    reposLoading.value = false
  }
}

async function loadDashboard(): Promise<void> {
  dashboardLoading.value = true
  try {
    const res = await request('/dashboard')
    dashboard.value = res.ok ? ((await res.json()) as DashboardResponse) : { repos: [] }
  } finally {
    dashboardLoading.value = false
  }
}

async function toggleRepo(repo: RepoSummary): Promise<void> {
  const next = !repo.enabled
  const segment = `/repos/${repo.fullName}/enabled`
  busyRepos.value = new Set(busyRepos.value).add(repo.fullName)
  try {
    const res = await request(segment, { method: next ? 'PUT' : 'DELETE' })
    if (res.ok) {
      repo.enabled = next
      await loadDashboard()
    }
  } finally {
    const updated = new Set(busyRepos.value)
    updated.delete(repo.fullName)
    busyRepos.value = updated
  }
}

async function onLogout(): Promise<void> {
  await logout()
}

onMounted(async () => {
  await fetchMe()
  if (user.value) {
    await Promise.all([loadRepos(), loadDashboard()])
  }
})
</script>
