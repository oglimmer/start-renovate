// Thin fetch wrapper for the authenticated backend API.
//
// Everything goes through the relative `/api` path so the browser treats it as same-origin: in
// production the frontend and backend share a host; in local dev a Nuxt devProxy (see
// nuxt.config.ts) forwards `/api` to the backend. Same-origin is what makes the session cookie and
// the CSRF cookie/header flow work without SameSite=None gymnastics.

function readCookie(name: string): string | null {
  if (typeof document === 'undefined') return null
  const match = document.cookie.match(new RegExp('(?:^|; )' + name + '=([^;]*)'))
  return match ? decodeURIComponent(match[1]!) : null
}

export function useApi() {
  const base = '/api'

  async function request(path: string, options: RequestInit = {}): Promise<Response> {
    const headers = new Headers(options.headers || {})
    // Marks the call as XHR so the backend returns 401 (not a 302 to GitHub) when unauthenticated.
    headers.set('X-Requested-With', 'XMLHttpRequest')

    const method = (options.method || 'GET').toUpperCase()
    if (method !== 'GET' && method !== 'HEAD') {
      const csrf = readCookie('XSRF-TOKEN')
      if (csrf) headers.set('X-XSRF-TOKEN', csrf)
    }

    return fetch(`${base}${path}`, { ...options, headers, credentials: 'include' })
  }

  return { base, request }
}
