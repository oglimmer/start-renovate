<template>
  <div class="relative mb-4">
    <div class="flex items-center justify-between bg-gray-800 text-gray-300 text-xs px-4 py-2 rounded-t-lg">
      <span class="font-mono uppercase tracking-wide">{{ lang }}</span>
      <button
        type="button"
        class="text-gray-300 hover:text-white transition-colors font-medium"
        @click="emit('copy', copyKey, code)"
      >
        {{ copied === copyKey ? 'Copied!' : 'Copy' }}
      </button>
    </div>
    <pre class="bg-gray-900 text-green-400 p-4 rounded-b-lg overflow-x-auto text-sm leading-relaxed"><code>{{ code }}</code></pre>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  code: string
  lang: string
  copyKey: string
  copied: string | null
}>()

const emit = defineEmits<{
  copy: [key: string, code: string]
}>()
</script>
