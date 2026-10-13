import { useEffect, useState } from 'react'
import { ApiError, getApplication } from '../api'
import { rememberApplication } from '../session'
import type { Application } from '../types'
import { useAuth } from '../useAuth'

export function useApplication(id: string | undefined) {
  const { session } = useAuth()
  const [application, setApplication] = useState<Application | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(Boolean(id))
  const [requestId, setRequestId] = useState(0)

  useEffect(() => {
    if (!id) return
    let cancelled = false
    async function load(applicationId: string) {
      try {
        const next = await getApplication(applicationId)
        if (cancelled) return
        if (session?.role === 'CUSTOMER') rememberApplication(session.userId, next.id)
        setApplication(next)
        setError(null)
      } catch (err) {
        if (cancelled) return
        setApplication(null)
        setError(err instanceof ApiError ? err.message : 'Could not load the application.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void load(id)
    return () => {
      cancelled = true
    }
  }, [id, requestId, session])

  function reload() {
    setLoading(true)
    setError(null)
    setRequestId((value) => value + 1)
  }

  return {
    application: id ? application : null,
    error: id ? error : 'This application is missing an id.',
    loading: id ? loading : false,
    reload,
  }
}
