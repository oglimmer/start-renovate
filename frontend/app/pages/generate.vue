<template>
  <div class="min-h-screen bg-gray-950 text-gray-200 px-4 sm:px-6 lg:px-8 py-8">
    <div class="max-w-4xl mx-auto">
      <!-- Toolbar -->
      <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
        <div class="flex items-center gap-3 min-w-0">
          <span class="inline-flex items-center px-2 py-0.5 rounded text-xs font-bold bg-green-900 text-green-300">GET</span>
          <code class="text-sm text-gray-400 truncate">/generate{{ queryString }}</code>
        </div>
        <div class="flex items-center gap-4 text-sm">
          <button type="button" class="text-gray-300 hover:text-white transition-colors" @click="copy">
            {{ copied ? 'Copied!' : 'Copy JSON' }}
          </button>
          <NuxtLink to="/developers" class="text-indigo-400 hover:text-indigo-300">Docs</NuxtLink>
          <NuxtLink to="/" class="text-indigo-400 hover:text-indigo-300">Form</NuxtLink>
        </div>
      </div>

      <!-- Result JSON -->
      <ClientOnly>
        <pre class="bg-gray-900 text-green-400 p-5 rounded-lg overflow-x-auto text-sm leading-relaxed"><code>{{ json }}</code></pre>
        <template #fallback>
          <pre class="bg-gray-900 text-gray-500 p-5 rounded-lg text-sm">Generating…</pre>
        </template>
      </ClientOnly>

      <p class="text-xs text-gray-500 mt-4">
        This endpoint runs entirely in your browser. It maps the URL query parameters onto the default
        configuration and returns the generated <code class="text-gray-400">renovate.json</code> — the exact same
        logic used by the <NuxtLink to="/" class="text-indigo-400 hover:text-indigo-300">interactive form</NuxtLink>.
        See the <NuxtLink to="/developers" class="text-indigo-400 hover:text-indigo-300">developer docs</NuxtLink> for all parameters.
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { generateConfigJsonFromQuery } from '../lib/generateRenovateConfig'

useHead({
  title: 'Generate API — Renovate Initializr',
  meta: [{ name: 'robots', content: 'noindex' }]
})

const route = useRoute()

const json = computed(() => generateConfigJsonFromQuery(route.query))

const queryString = computed(() => {
  const qs = new URLSearchParams(
    Object.entries(route.query).flatMap(([k, v]) =>
      (Array.isArray(v) ? v : [v])
        .filter((x): x is string => x != null)
        .map(x => [k, x] as [string, string])
    )
  ).toString()
  return qs ? `?${qs}` : ''
})

const copied = ref(false)
let copyTimer: ReturnType<typeof setTimeout> | undefined

const copy = async () => {
  try {
    await navigator.clipboard.writeText(json.value)
    copied.value = true
    if (copyTimer) clearTimeout(copyTimer)
    copyTimer = setTimeout(() => { copied.value = false }, 2000)
  } catch {
    // Clipboard unavailable (non-secure context) — ignore.
  }
}
</script>
