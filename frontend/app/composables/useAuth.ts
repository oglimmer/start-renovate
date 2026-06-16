// Authentication state for the dashboard, backed by the backend session.

export interface AuthUser {
  id: number
  login: string
  name: string | null
  avatarUrl: string | null
}

export function useAuth() {
  const user = useState<AuthUser | null>('auth-user', () => null)
  const loading = useState<boolean>('auth-loading', () => true)
  const { request } = useApi()

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

  // Top-window navigation into the GitHub OAuth flow (relative path → same origin / dev proxy).
  function login(): void {
    window.location.href = '/api/oauth2/authorization/github'
  }

  async function logout(): Promise<void> {
    await request('/logout', { method: 'POST' })
    user.value = null
  }

  return { user, loading, fetchMe, login, logout }
}
