import { createContext, useContext } from 'react'
import type { Session } from './types'

export type AuthContextValue = {
  session: Session | null
  ready: boolean
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | null>(null)

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used within AuthProvider')
  return value
}
