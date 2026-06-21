<template>
  <div class="space-y-3">
    <div v-for="option in options" :key="option.value" class="flex items-start">
      <input
        :id="`${name}-${option.value}`"
        type="radio"
        :name="name"
        :value="option.value"
        :checked="modelValue === option.value"
        class="mt-1 h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300"
        @change="emit('update:modelValue', option.value)"
      >
      <div class="ml-3 flex-1">
        <label :for="`${name}-${option.value}`" class="font-medium text-gray-900 cursor-pointer">
          {{ option.label }}
        </label>
        <!-- option.description is trusted static copy (see lib/formOptions.ts), never user input -->
        <!-- eslint-disable-next-line vue/no-v-html -->
        <p class="text-sm text-gray-600" v-html="option.description" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends string">
import type { RadioOption } from '../lib/formOptions'

defineProps<{
  modelValue: T
  // Radio group name; also the prefix for each option's input id (`${name}-${value}`).
  name: string
  options: RadioOption<T>[]
}>()

const emit = defineEmits<{ 'update:modelValue': [value: T] }>()
</script>
