// Authentication state for the dashboard, backed by the backend session.

export type AuthProvider = 'github' | 'gitlab'

export interface AuthUser {
  id: string
  login: string
  name: string | null
  avatarUrl: string | null
  // Which OAuth provider the session belongs to, and the base URL for linking to its repos.
  provider: AuthProvider
  webBaseUrl: string
}

export interface ProviderInfo {
  id: AuthProvider
  displayName: string
}

export function useAuth() {
  const user = useState<AuthUser | null>('auth-user', () => null)
  const loading = useState<boolean>('auth-loading', () => true)
  const providers = useState<ProviderInfo[]>('auth-providers', () => [])
  const { request } = useApi()

  // The configured login providers (public endpoint); drives which login buttons are shown.
  async function fetchProviders(): Promise<void> {
    try {
      const res = await request('/providers')
      providers.value = res.ok ? ((await res.json()) as ProviderInfo[]) : []
    } catch {
      providers.value = []
    }
  }

  async function fetchMe(): Promise<void> {
    loading.value = true
    try {
      const res = await request('/me')
      user.value = res.ok ? ((await res.json()) as AuthUser) : null
    } catch {
      user.value = null
    } finally {
      loading.value = false
    }
  }

  // Top-window navigation into the provider's OAuth flow (relative path → same origin / dev proxy).
  function login(provider: AuthProvider = 'github'): void {
    window.location.href = `/api/oauth2/authorization/${provider}`
  }

  async function logout(): Promise<void> {
    await request('/logout', { method: 'POST' })
    user.value = null
  }

  return { user, loading, providers, fetchMe, fetchProviders, login, logout }
}
