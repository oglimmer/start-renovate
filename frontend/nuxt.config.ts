// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss', '@nuxt/eslint'],
  nitro: {
    preset: 'github-pages',
    // Local dev: forward /api to the backend so the SPA, the session cookie and the CSRF
    // cookie/header are all same-origin (http://localhost:3000). In production the frontend and
    // backend already share an origin, so /api is reached directly.
    devProxy: {
      '/api': {
        target: 'http://localhost:8080/api',
        changeOrigin: true
      }
    },
    prerender: {
      crawlLinks: true,
      routes: ['/', '/developers', '/generate', '/dashboard', '/imprint', '/privacy', '/terms']
    }
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
