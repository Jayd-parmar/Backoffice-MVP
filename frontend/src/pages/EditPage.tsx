import { Navigate, useParams } from 'react-router-dom'
import { ApplicationForm } from '../components/ApplicationForm'
import { ErrorState, LoadingState } from '../components/PageState'
import { applicationToForm } from '../form'
import { useApplication } from '../hooks/useApplication'

export function EditPage() {
  const { id } = useParams()
  const { application, error, loading, reload } = useApplication(id)

  if (loading) return <LoadingState label="Loading application" />
  if (error || !application) return <ErrorState message={error ?? 'Application not found.'} onRetry={() => void reload()} />
  if (application.status !== 'DRAFT' && application.status !== 'REJECTED') {
    return <Navigate to={`/applications/${application.id}/submitted`} replace />
  }

  return (
    <ApplicationForm
      key={application.updatedAt}
      mode="edit"
      applicationId={application.id}
      initial={applicationToForm(application)}
    />
  )
}
