import { useState } from 'react'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { ApiError, approveApplication, rejectApplication } from '../api'
import { useAuth } from '../useAuth'
import { ApplicationDetails } from '../components/ApplicationDetails'
import { ErrorState, LoadingState } from '../components/PageState'
import { StatusChip } from '../components/StatusChip'
import { formatDateTime } from '../format'
import { useApplication } from '../hooks/useApplication'

export function ApprovalDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { session } = useAuth()
  const { application, error, loading, reload } = useApplication(id)
  const [comment, setComment] = useState('')
  const [commentError, setCommentError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [working, setWorking] = useState<'approve' | 'reject' | null>(null)

  if (loading) return <LoadingState label="Loading application" />
  if (error || !application) return <ErrorState message={error ?? 'Application not found.'} onRetry={() => void reload()} />

  async function approve() {
    if (!application || !session) return
    setWorking('approve')
    setActionError(null)
    try {
      await approveApplication(application.id)
      const notice =
        session.role === 'APPROVER_1' ? 'Approved and sent to Approver 2.' : 'Application approved.'
      navigate('/approvals', { replace: true, state: { notice } })
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : 'Could not approve the application.')
    } finally {
      setWorking(null)
    }
  }

  async function reject() {
    if (!application) return
    const reason = comment.trim()
    if (!reason) {
      setCommentError('Comment is required')
      return
    }
    if (reason.length > 500) {
      setCommentError('Comment must be at most 500 characters')
      return
    }
    setWorking('reject')
    setActionError(null)
    setCommentError(null)
    try {
      await rejectApplication(application.id, reason)
      navigate('/approvals', { replace: true, state: { notice: 'Application rejected.' } })
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : 'Could not reject the application.')
    } finally {
      setWorking(null)
    }
  }

  return (
    <Stack spacing={3} sx={{ maxWidth: 880 }}>
      <Box>
        <Button component={RouterLink} to="/approvals" sx={{ mb: 1, px: 0 }}>
          Back to queue
        </Button>
        <Stack direction="row" spacing={1.5} sx={{ mb: 1, alignItems: 'center' }}>
          <Typography variant="h4">
            {application.personalDetails.firstName} {application.personalDetails.lastName}
          </Typography>
          <StatusChip status={application.status} />
        </Stack>
        <Typography color="text.secondary">
          Latest details for this application, updated {formatDateTime(application.updatedAt)}.
        </Typography>
      </Box>
      {actionError ? <Alert severity="error">{actionError}</Alert> : null}
      <ApplicationDetails application={application} />
      <Stack spacing={2} sx={{ p: { xs: 2, sm: 3 }, border: '1px solid', borderColor: 'divider', borderRadius: 2, bgcolor: 'background.paper' }}>
        <Typography variant="h6">Decision</Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
          <Button variant="contained" color="success" disabled={working !== null} onClick={() => void approve()}>
            {working === 'approve' ? 'Approving…' : 'Approve'}
          </Button>
        </Stack>
        <TextField
          label="Rejection comment"
          value={comment}
          onChange={(event) => {
            setComment(event.target.value)
            setCommentError(null)
          }}
          error={Boolean(commentError)}
          helperText={commentError ?? 'Required when you reject'}
          multiline
          minRows={3}
          slotProps={{ htmlInput: { maxLength: 500 } }}
          fullWidth
        />
        <Box>
          <Button variant="outlined" color="error" disabled={working !== null} onClick={() => void reject()}>
            {working === 'reject' ? 'Rejecting…' : 'Reject'}
          </Button>
        </Box>
      </Stack>
    </Stack>
  )
}
