import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError, getApplication } from '../api'
import { ApplicationForm } from '../components/ApplicationForm'
import { ErrorState, LoadingState } from '../components/PageState'
import { customerDestination, emptyForm } from '../form'
import { forgetApplication, getStoredApplicationId } from '../session'
import { useAuth } from '../useAuth'

export function ApplyPage() {
  const { session } = useAuth()
  const navigate = useNavigate()
  const storedId = session ? getStoredApplicationId(session.userId) : null
  const [cleared, setCleared] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (!session || !storedId || cleared) return
    let cancelled = false
    async function load(id: string, ownerId: string) {
      try {
        const application = await getApplication(id)
        if (!cancelled) navigate(customerDestination(application.status, application.id), { replace: true })
      } catch (err) {
        if (cancelled) return
        if (err instanceof ApiError && (err.status === 404 || err.status === 400)) {
          forgetApplication(ownerId)
          setCleared(true)
          return
        }
        setError(err instanceof ApiError ? err.message : 'Could not load your application.')
      }
    }
    void load(storedId, session.userId)
    return () => {
      cancelled = true
    }
  }, [attempt, cleared, navigate, session, storedId])

  if (!session) return null
  if (error) {
    return (
      <ErrorState
        message={error}
        onRetry={() => {
          setError(null)
          setAttempt((value) => value + 1)
        }}
      />
    )
  }
  if (storedId && !cleared) return <LoadingState label="Checking your application" />
  return <ApplicationForm mode="create" initial={emptyForm(session.email)} />
}
