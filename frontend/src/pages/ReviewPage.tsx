import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { ApiError, submitApplication } from '../api'
import { ApplicationDetails } from '../components/ApplicationDetails'
import { ErrorState, LoadingState } from '../components/PageState'
import { StatusChip } from '../components/StatusChip'
import { formatDateTime } from '../format'
import { useApplication } from '../hooks/useApplication'

export function ReviewPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { application, error, loading, reload } = useApplication(id)
  const [confirming, setConfirming] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState<string | null>(null)

  if (loading) return <LoadingState label="Loading application" />
  if (error || !application) return <ErrorState message={error ?? 'Application not found.'} onRetry={() => void reload()} />

  const canChange = application.status === 'DRAFT' || application.status === 'REJECTED'

  async function submit() {
    if (!application) return
    setSubmitting(true)
    setSubmitError(null)
    try {
      await submitApplication(application.id)
      navigate(`/applications/${application.id}/submitted`, { replace: true })
    } catch (err) {
      setSubmitError(err instanceof ApiError ? err.message : 'Could not submit the application.')
      setConfirming(false)
    } finally {
      setSubmitting(false)
    }
  }

  const rejection =
    application.approvalHistory.approver1.action === 'REJECTED'
      ? application.approvalHistory.approver1.comment
      : application.approvalHistory.approver2.action === 'REJECTED'
        ? application.approvalHistory.approver2.comment
        : null

  return (
    <Stack spacing={3} sx={{ maxWidth: 880 }}>
      <Box>
        <Stack direction="row" spacing={1.5} sx={{ mb: 1, alignItems: 'center' }}>
          <Typography variant="h4">Review application</Typography>
          <StatusChip status={application.status} />
        </Stack>
        <Typography color="text.secondary">
          {canChange
            ? `Latest details, updated ${formatDateTime(application.updatedAt)}. Edit them, or submit for backoffice approval.`
            : `Latest details, updated ${formatDateTime(application.updatedAt)}.`}
        </Typography>
      </Box>
      {application.status === 'REJECTED' ? (
        <Alert severity="error">
          An approver rejected this application.{rejection ? ` ${rejection}` : ''} Update the details and submit again.
        </Alert>
      ) : null}
      {submitError ? <Alert severity="error">{submitError}</Alert> : null}
      <ApplicationDetails application={application} />
      {canChange ? (
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <Button variant="outlined" onClick={() => navigate(`/applications/${application.id}/edit`)}>
            Edit
          </Button>
          <Button variant="contained" onClick={() => setConfirming(true)}>
            Submit for approval
          </Button>
        </Stack>
      ) : null}
      <Dialog open={confirming} onClose={() => (submitting ? undefined : setConfirming(false))}>
        <DialogTitle>Submit for approval?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            This sends the application to Approver 1. You can change it again only if an approver rejects it.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirming(false)} disabled={submitting}>
            Cancel
          </Button>
          <Button variant="contained" onClick={() => void submit()} disabled={submitting}>
            {submitting ? 'Submitting…' : 'Submit'}
          </Button>
        </DialogActions>
      </Dialog>
    </Stack>
  )
}
