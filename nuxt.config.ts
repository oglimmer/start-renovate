// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss'],
  nitro: {
    preset: 'github-pages'
  },
  app: {
    baseURL: process.env.BASE_URL || '/',
    head: {
      title: 'Renovate Initializr',
      meta: [
        { name: 'description', content: 'Generate and customize a renovate.json configuration quickly.' }
      ]
    }
  }
})
