import type { Role, Session } from './types'

const SESSION_KEY = 'backoffice.session'

const listeners = new Set<() => void>()

function isRole(value: unknown): value is Role {
  return value === 'CUSTOMER' || value === 'APPROVER_1' || value === 'APPROVER_2' || value === 'APPROVER_3'
}

export function isApprover(role: Role | undefined): boolean {
  return role === 'APPROVER_1' || role === 'APPROVER_2' || role === 'APPROVER_3'
}

function decodeBase64Url(value: string): string {
  const padded = value.replace(/-/g, '+').replace(/_/g, '/') + '='.repeat((4 - (value.length % 4)) % 4)
  return atob(padded)
}

export function decodeAccessToken(token: string): { userId: string; role: Role; exp: number } | null {
  try {
    const payload = token.split('.')[1]
    if (!payload) return null
    const claims = JSON.parse(decodeBase64Url(payload)) as {
      sub?: unknown
      role?: unknown
      exp?: unknown
    }
    if (typeof claims.sub !== 'string' || !isRole(claims.role) || typeof claims.exp !== 'number') {
      return null
    }
    return { userId: claims.sub, role: claims.role, exp: claims.exp }
  } catch {
    return null
  }
}

function readStoredSession(): Session | null {
  try {
    const raw = localStorage.getItem(SESSION_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as Partial<Session>
    if (!parsed.accessToken || !parsed.refreshToken || !parsed.userId || !isRole(parsed.role)) {
      return null
    }
    return {
      accessToken: parsed.accessToken,
      refreshToken: parsed.refreshToken,
      userId: parsed.userId,
      role: parsed.role,
      email: parsed.email ?? '',
    }
  } catch {
    return null
  }
}

let memory: Session | null = readStoredSession()

export function getSession(): Session | null {
  return memory
}

export function setSession(session: Session | null): void {
  memory = session
  if (session) {
    localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  } else {
    localStorage.removeItem(SESSION_KEY)
  }
  listeners.forEach((listener) => listener())
}

export function subscribe(listener: () => void): () => void {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function accessTokenIsFresh(token: string, skewMs = 10_000): boolean {
  const claims = decodeAccessToken(token)
  if (!claims) return false
  return claims.exp * 1000 > Date.now() + skewMs
}

function applicationKey(userId: string): string {
  return `backoffice.application.${userId}`
}

export function rememberApplication(userId: string, applicationId: string): void {
  localStorage.setItem(applicationKey(userId), applicationId)
}

export function getStoredApplicationId(userId: string): string | null {
  return localStorage.getItem(applicationKey(userId))
}

export function forgetApplication(userId: string): void {
  localStorage.removeItem(applicationKey(userId))
}
