import { useEffect, useMemo, useState, type ReactNode } from 'react'
import { refreshTokens } from './api'
import { accessTokenIsFresh, getSession, setSession, subscribe } from './session'
import type { Session } from './types'
import { AuthContext, type AuthContextValue } from './useAuth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<Session | null>(() => getSession())
  const [ready, setReady] = useState(false)

  useEffect(() => subscribe(() => setSessionState(getSession())), [])

  useEffect(() => {
    let cancelled = false
    async function boot() {
      const current = getSession()
      if (current && !accessTokenIsFresh(current.accessToken)) {
        const refreshed = await refreshTokens()
        if (!refreshed) setSession(null)
      }
      if (!cancelled) setReady(true)
    }
    void boot()
    return () => {
      cancelled = true
    }
  }, [])

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      ready,
      logout: () => setSession(null),
    }),
    [session, ready],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
