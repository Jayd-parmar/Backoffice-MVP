import { Link as RouterLink, Navigate, useParams } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { ApplicationDetails } from '../components/ApplicationDetails'
import { ErrorState, LoadingState } from '../components/PageState'
import { StatusChip } from '../components/StatusChip'
import { formatDateTime } from '../format'
import { useApplication } from '../hooks/useApplication'
import type { Application } from '../types'

function submittedCopy(application: Application): { title: string; body: string } {
  switch (application.status) {
    case 'PENDING_APPROVER_1':
      return {
        title: 'Submitted for approval',
        body: 'Your application has been submitted for backoffice approval. Approver 1 will review it next.',
      }
    case 'PENDING_APPROVER_2':
      return {
        title: 'With Approver 2',
        body: 'Approver 1 approved your application. It is now waiting for Approver 2.',
      }
    case 'APPROVED':
      return {
        title: 'Application approved',
        body: 'Both approvers have approved your forex application.',
      }
    case 'REJECTED':
      return {
        title: 'Application rejected',
        body: 'An approver rejected this application. You can update the details and submit it again.',
      }
    case 'DRAFT':
      return {
        title: 'Draft',
        body: 'This application is still a draft.',
      }
  }
}

export function SubmittedPage() {
  const { id } = useParams()
  const { application, error, loading, reload } = useApplication(id)

  if (loading) return <LoadingState label="Loading latest details" />
  if (error || !application) return <ErrorState message={error ?? 'Application not found.'} onRetry={() => void reload()} />
  if (application.status === 'DRAFT') return <Navigate to={`/applications/${application.id}`} replace />

  const copy = submittedCopy(application)
  const rejectionComment =
    application.approvalHistory.approver2.comment || application.approvalHistory.approver1.comment

  return (
    <Stack spacing={3} sx={{ maxWidth: 880 }}>
      <Box>
        <Stack direction="row" spacing={1.5} sx={{ mb: 1, alignItems: 'center' }}>
          <Typography variant="h4">{copy.title}</Typography>
          <StatusChip status={application.status} />
        </Stack>
        <Typography color="text.secondary">{copy.body}</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          Latest details, updated {formatDateTime(application.updatedAt)}.
        </Typography>
      </Box>
      {application.status === 'REJECTED' && rejectionComment ? (
        <Alert severity="error">{rejectionComment}</Alert>
      ) : null}
      <ApplicationDetails application={application} />
      <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
        <Button variant="outlined" onClick={() => void reload()}>
          Refresh details
        </Button>
        {application.status === 'REJECTED' ? (
          <Button variant="contained" component={RouterLink} to={`/applications/${application.id}`}>
            Update and resubmit
          </Button>
        ) : null}
      </Stack>
    </Stack>
  )
}
