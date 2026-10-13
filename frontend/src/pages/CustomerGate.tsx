import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { ApiError, getApplication } from '../api'
import { ErrorState, LoadingState } from '../components/PageState'
import { customerDestination } from '../form'
import { forgetApplication, getStoredApplicationId } from '../session'
import { useAuth } from '../useAuth'

export function CustomerGate() {
  const { session } = useAuth()
  const [destination, setDestination] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [attempt, setAttempt] = useState(0)

  const userId = session?.role === 'CUSTOMER' ? session.userId : null
  const storedId = userId ? getStoredApplicationId(userId) : null

  useEffect(() => {
    if (!userId || !storedId) return
    let cancelled = false
    async function load(id: string, ownerId: string) {
      try {
        const application = await getApplication(id)
        if (!cancelled) setDestination(customerDestination(application.status, application.id))
      } catch (err) {
        if (cancelled) return
        if (err instanceof ApiError && (err.status === 404 || err.status === 400)) {
          forgetApplication(ownerId)
          setDestination('/apply')
          return
        }
        setError(err instanceof ApiError ? err.message : 'Could not load your application.')
      }
    }
    void load(storedId, userId)
    return () => {
      cancelled = true
    }
  }, [attempt, storedId, userId])

  if (!session) return null
  if (session.role !== 'CUSTOMER') return <Navigate to="/approvals" replace />
  if (!storedId) return <Navigate to="/apply" replace />
  if (error) {
    return (
      <ErrorState
        message={error}
        onRetry={() => {
          setError(null)
          setDestination(null)
          setAttempt((value) => value + 1)
        }}
      />
    )
  }
  if (!destination) return <LoadingState label="Loading your latest application" />
  return <Navigate to={destination} replace />
}
