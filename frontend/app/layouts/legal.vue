<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-3xl mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <NuxtLink to="/" class="inline-flex items-center text-sm text-indigo-600 hover:text-indigo-800 mb-4">
          <svg class="h-4 w-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
          </svg>
          Back to the configurator
        </NuxtLink>
        <p class="text-xs font-semibold uppercase tracking-wide text-indigo-600 mb-1">Legal</p>
        <h1 class="text-4xl font-bold text-gray-900 mb-2">
          <slot name="title">{{ title }}</slot>
        </h1>
        <p v-if="$slots.subtitle || subtitle" class="text-gray-600">
          <slot name="subtitle">{{ subtitle }}</slot>
        </p>
      </div>

      <!-- Content -->
      <section class="bg-white shadow-xl rounded-lg p-8 legal-prose">
        <slot />
      </section>

      <!-- Nav: home + the sibling legal pages (current page excluded) -->
      <nav class="mt-6 text-center text-sm text-gray-500 space-x-2">
        <NuxtLink to="/" class="text-indigo-600 hover:text-indigo-800 underline">← Back to home</NuxtLink>
        <template v-for="link in siblingLinks" :key="link.to">
          <span aria-hidden="true">·</span>
          <NuxtLink :to="link.to" class="text-indigo-600 hover:text-indigo-800 underline">{{ link.label }}</NuxtLink>
        </template>
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps<{
  // Plain-text title/subtitle for the common case; pages needing markup (e.g. an <em>) can use the
  // #title / #subtitle slots instead.
  title?: string
  subtitle?: string
}>()

// Single source of truth for the legal cross-links; each page shows the other two.
const legalLinks = [
  { to: '/privacy', label: 'Privacy' },
  { to: '/terms', label: 'Terms' },
  { to: '/imprint', label: 'Imprint' }
]

const route = useRoute()
const siblingLinks = computed(() => legalLinks.filter(link => link.to !== route.path))
</script>

<style scoped>
.legal-prose :deep(h2) {
  @apply text-xl font-semibold text-gray-900 mt-8 mb-3;
}
.legal-prose :deep(h3) {
  @apply text-base font-semibold text-gray-900 mt-4 mb-1;
}
.legal-prose :deep(p) {
  @apply text-gray-700 leading-relaxed mb-4;
}
.legal-prose :deep(a) {
  @apply text-indigo-600 hover:text-indigo-800 underline;
}
.legal-prose :deep(ul) {
  @apply list-disc pl-6 space-y-2 text-gray-700 mb-4;
}
.legal-prose :deep(strong) {
  @apply font-semibold text-gray-900;
}
.legal-prose :deep(code) {
  @apply bg-gray-100 px-1.5 py-0.5 rounded text-sm;
}
</style>
