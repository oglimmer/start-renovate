<template>
  <footer class="border-t border-gray-200 bg-white/60 backdrop-blur-sm">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 flex flex-col sm:flex-row items-center justify-between gap-3 text-sm text-gray-500">
      <p class="flex items-center gap-2">
        <span>© {{ year }} oglimmer.com · Renovate Initializr</span>
        <span aria-hidden="true">·</span>
        <a
          href="https://github.com/oglimmer/start-renovate/blob/main/LICENSE.TXT"
          target="_blank"
          rel="noopener noreferrer"
          class="hover:text-indigo-600 transition-colors"
        >Apache&nbsp;2.0</a>
      </p>
      <nav class="flex items-center gap-4">
        <NuxtLink to="/imprint" class="hover:text-indigo-600 transition-colors">Imprint</NuxtLink>
        <NuxtLink to="/privacy" class="hover:text-indigo-600 transition-colors">Privacy</NuxtLink>
        <NuxtLink to="/terms" class="hover:text-indigo-600 transition-colors">Terms</NuxtLink>
        <a
          href="https://github.com/oglimmer/start-renovate"
          target="_blank"
          rel="noopener noreferrer"
          aria-label="Source code on GitHub"
          title="Source code on GitHub"
          class="hover:text-indigo-600 transition-colors"
        >
          <svg class="h-5 w-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path fill-rule="evenodd" clip-rule="evenodd" d="M12 2C6.48 2 2 6.58 2 12.26c0 4.5 2.87 8.32 6.84 9.67.5.1.68-.22.68-.49l-.01-1.7c-2.78.62-3.37-1.37-3.37-1.37-.46-1.18-1.11-1.5-1.11-1.5-.91-.63.07-.62.07-.62 1 .07 1.53 1.06 1.53 1.06.9 1.57 2.36 1.12 2.94.86.09-.67.35-1.12.63-1.38-2.22-.26-4.55-1.14-4.55-5.07 0-1.12.39-2.03 1.03-2.75-.1-.26-.45-1.3.1-2.7 0 0 .84-.28 2.75 1.05A9.36 9.36 0 0 1 12 6.84c.85 0 1.71.12 2.51.34 1.91-1.33 2.75-1.05 2.75-1.05.55 1.4.2 2.44.1 2.7.64.72 1.03 1.63 1.03 2.75 0 3.94-2.34 4.81-4.57 5.06.36.32.68.94.68 1.9l-.01 2.82c0 .27.18.6.69.49A10.02 10.02 0 0 0 22 12.26C22 6.58 17.52 2 12 2Z" />
          </svg>
        </a>
      </nav>
    </div>
  </footer>
</template>

<script setup lang="ts">
// Static year so the prerendered output stays deterministic across builds.
const year = 2026
</script>
