import { decodeAccessToken, getSession, setSession } from './session'
import type { Application, ApplicationRequest, Role, Session } from './types'

const API_BASE = (import.meta.env.VITE_API_BASE ?? '').replace(/\/$/, '')

export class ApiError extends Error {
  status: number

  constructor(status: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

type RequestOptions = RequestInit & {
  auth?: boolean
  retry?: boolean
}

let refreshInFlight: Promise<boolean> | null = null

function sessionFromTokens(accessToken: string, refreshToken: string, email: string): Session {
  const claims = decodeAccessToken(accessToken)
  if (!claims) {
    throw new ApiError(401, 'The server returned an unreadable access token.')
  }
  return {
    accessToken,
    refreshToken,
    userId: claims.userId,
    role: claims.role,
    email,
  }
}

async function readError(response: Response): Promise<string> {
  const text = await response.text()
  if (!text.trim()) return `Request failed (${response.status})`
  try {
    const body = JSON.parse(text) as { message?: unknown }
    if (typeof body.message === 'string' && body.message.trim()) return body.message
  } catch {
    return text.trim()
  }
  return `Request failed (${response.status})`
}

export async function refreshTokens(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = actuallyRefresh().finally(() => {
      refreshInFlight = null
    })
  }
  return refreshInFlight
}

async function actuallyRefresh(): Promise<boolean> {
  const session = getSession()
  if (!session) return false
  try {
    const response = await fetch(`${API_BASE}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken: session.refreshToken }),
    })
    if (!response.ok) return false
    const tokens = (await response.json()) as { accessToken?: string; refreshToken?: string }
    if (!tokens.accessToken || !tokens.refreshToken) return false
    setSession(sessionFromTokens(tokens.accessToken, tokens.refreshToken, session.email))
    return true
  } catch {
    return false
  }
}

export async function api<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { auth = true, retry = true, headers, ...init } = options
  const requestHeaders = new Headers(headers)
  if (init.body && !requestHeaders.has('Content-Type')) {
    requestHeaders.set('Content-Type', 'application/json')
  }
  const session = getSession()
  if (auth && session?.accessToken) {
    requestHeaders.set('Authorization', `Bearer ${session.accessToken}`)
  }

  let response: Response
  try {
    response = await fetch(`${API_BASE}${path}`, { ...init, headers: requestHeaders })
  } catch {
    throw new ApiError(0, 'Cannot reach the server. Check that the backend is running on port 8081.')
  }

  if (response.status === 401 && auth && retry && session?.refreshToken) {
    const refreshed = await refreshTokens()
    if (refreshed) return api<T>(path, { ...options, retry: false })
    setSession(null)
    throw new ApiError(401, 'Your session has expired. Please log in again.')
  }

  if (!response.ok) {
    throw new ApiError(response.status, await readError(response))
  }

  if (response.status === 204) return undefined as T
  return (await response.json()) as T
}

type TokenPair = {
  accessToken: string
  refreshToken: string
}

export async function login(email: string, password: string): Promise<Session> {
  const tokens = await api<TokenPair>('/api/auth/login', {
    method: 'POST',
    auth: false,
    body: JSON.stringify({ email: email.trim(), password }),
  })
  const session = sessionFromTokens(tokens.accessToken, tokens.refreshToken, email.trim().toLowerCase())
  setSession(session)
  return session
}

export async function registerAccount(input: {
  email: string
  password: string
  role: Role
  name: string
  phone?: string
}): Promise<void> {
  const body: {
    email: string
    password: string
    role: Role
    name: string
    phone?: string
  } = {
    email: input.email.trim(),
    password: input.password,
    role: input.role,
    name: input.name.trim(),
  }
  if (input.phone?.trim()) body.phone = input.phone.trim()
  await api<{ message: string }>('/api/auth/register', {
    method: 'POST',
    auth: false,
    body: JSON.stringify(body),
  })
}

function asDate(value: unknown): string {
  if (typeof value === 'string') return value.slice(0, 10)
  if (Array.isArray(value) && value.length >= 3) {
    const year = Number(value[0])
    const month = Number(value[1])
    const day = Number(value[2])
    if (!year || !month || !day) return ''
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  }
  return ''
}

function asText(value: unknown): string {
  return typeof value === 'string' ? value : ''
}

function normalizeDecision(value: unknown): Application['approvalHistory']['approver1'] {
  const decision = (value ?? {}) as Record<string, unknown>
  const action = decision.action
  return {
    userId: typeof decision.userId === 'string' ? decision.userId : null,
    action: action === 'APPROVED' || action === 'REJECTED' ? action : null,
    comment: typeof decision.comment === 'string' ? decision.comment : null,
    actionAt: typeof decision.actionAt === 'string' ? decision.actionAt : null,
  }
}

export function normalizeApplication(raw: unknown): Application {
  const application = (raw ?? {}) as Record<string, unknown>
  const personal = (application.personalDetails ?? {}) as Record<string, unknown>
  const forex = (application.forexCardDetails ?? {}) as Record<string, unknown>
  const address = (application.addressDetails ?? {}) as Record<string, unknown>
  const travel = (application.travelDetails ?? {}) as Record<string, unknown>
  const approval = (application.approvalHistory ?? {}) as Record<string, unknown>
  const status = application.status
  return {
    id: asText(application.id),
    customerId: asText(application.customerId),
    status:
      status === 'DRAFT' ||
      status === 'PENDING_APPROVER_1' ||
      status === 'PENDING_APPROVER_2' ||
      status === 'APPROVED' ||
      status === 'REJECTED'
        ? status
        : 'DRAFT',
    personalDetails: {
      firstName: asText(personal.firstName),
      lastName: asText(personal.lastName),
      dateOfBirth: asDate(personal.dateOfBirth),
      gender: asText(personal.gender),
      panNumber: asText(personal.panNumber),
      email: asText(personal.email),
      mobile: asText(personal.mobile),
    },
    forexCardDetails: {
      cardType: asText(forex.cardType),
      currency: asText(forex.currency),
      amount: Number(forex.amount),
      purpose: asText(forex.purpose),
    },
    addressDetails: {
      addressLine1: asText(address.addressLine1),
      city: asText(address.city),
      state: asText(address.state),
      country: asText(address.country),
      pincode: asText(address.pincode),
    },
    travelDetails: {
      destinationCountry: asText(travel.destinationCountry),
      departureDate: asDate(travel.departureDate),
      returnDate: asDate(travel.returnDate),
      purpose: asText(travel.purpose),
    },
    approvalHistory: {
      approver1: normalizeDecision(approval.approver1),
      approver2: normalizeDecision(approval.approver2),
    },
    createdAt: asText(application.createdAt),
    updatedAt: asText(application.updatedAt),
  }
}

export async function createApplication(body: ApplicationRequest): Promise<Application> {
  const created = await api<unknown>('/api/applications', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return normalizeApplication(created)
}

export async function getApplication(id: string): Promise<Application> {
  const application = await api<unknown>(`/api/applications/${id}`)
  return normalizeApplication(application)
}

export async function updateApplication(id: string, body: ApplicationRequest): Promise<Application> {
  const updated = await api<unknown>(`/api/applications/${id}`, {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return normalizeApplication(updated)
}

export async function submitApplication(id: string): Promise<Application> {
  const submitted = await api<unknown>(`/api/applications/${id}/submit`, { method: 'POST' })
  return normalizeApplication(submitted)
}

export async function listPending(): Promise<Application[]> {
  const pending = await api<unknown>('/api/approvals/pending')
  if (!Array.isArray(pending)) {
    throw new ApiError(500, 'The pending list was not a list.')
  }
  return pending.map((item) => normalizeApplication(item))
}

export async function approveApplication(id: string): Promise<Application> {
  const approved = await api<unknown>(`/api/approvals/${id}/approve`, { method: 'POST' })
  return normalizeApplication(approved)
}

export async function rejectApplication(id: string, comment: string): Promise<Application> {
  const rejected = await api<unknown>(`/api/approvals/${id}/reject`, {
    method: 'POST',
    body: JSON.stringify({ comment }),
  })
  return normalizeApplication(rejected)
}
