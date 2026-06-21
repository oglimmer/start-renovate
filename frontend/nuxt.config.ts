// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: true },
  modules: ['@nuxtjs/tailwindcss', '@nuxt/eslint'],
  nitro: {
    // node-server (not the old static github-pages preset): the build now ships a
    // small Nitro runtime so /api/generate can be served server-side (curl-able).
    // Every page listed under prerender below is still prerendered to static HTML
    // at build time and served as a file, so the form and docs keep their
    // static-fast behaviour — only /api/generate runs dynamically.
    preset: 'node-server',
    // Local dev: forward the BACKEND /api routes to the Spring backend so the SPA,
    // the session cookie and the CSRF cookie/header are all same-origin
    // (http://localhost:3000). We enumerate the backend prefixes instead of a blanket
    // /api so that /api/generate is NOT proxied — it is served by this app's own Nitro
    // route (server/api/generate.get.ts). In production the ingress does the same split
    // (see helm ingress-generate.yaml). Each key strips off before being appended to its
    // target, so target = host + key keeps the path intact (backend context-path is /api).
    devProxy: {
      '/api/feedback': { target: 'http://localhost:8080/api/feedback', changeOrigin: true },
      '/api/me': { target: 'http://localhost:8080/api/me', changeOrigin: true },
      '/api/repos': { target: 'http://localhost:8080/api/repos', changeOrigin: true },
      '/api/dashboard': { target: 'http://localhost:8080/api/dashboard', changeOrigin: true },
      '/api/providers': { target: 'http://localhost:8080/api/providers', changeOrigin: true },
      '/api/oauth2': { target: 'http://localhost:8080/api/oauth2', changeOrigin: true },
      '/api/login': { target: 'http://localhost:8080/api/login', changeOrigin: true },
      '/api/logout': { target: 'http://localhost:8080/api/logout', changeOrigin: true },
      '/api/actuator': { target: 'http://localhost:8080/api/actuator', changeOrigin: true }
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
