import { useState } from 'react'
import { Link as RouterLink, useNavigate, useParams } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { ApiError, approveApplication, rejectApplication, verifyDocument } from '../api'
import { useAuth } from '../useAuth'
import { ApplicationDetails } from '../components/ApplicationDetails'
import { ErrorState, LoadingState } from '../components/PageState'
import { StatusChip } from '../components/StatusChip'
import { formatDate, formatDateTime, titleCaseToken } from '../format'
import { useApplication } from '../hooks/useApplication'
import type { Application, DocumentCode, PersonalDetails } from '../types'

function shown(value: string): string {
  const text = value.trim()
  return text || '—'
}

function documentDetails(code: DocumentCode, personal: PersonalDetails): { label: string; value: string }[] {
  const name = shown(`${personal.firstName} ${personal.lastName}`)
  const dateOfBirth = formatDate(personal.dateOfBirth)
  switch (code) {
    case 'AADHAAR':
      return [
        { label: 'Name', value: name },
        { label: 'Date of birth', value: dateOfBirth },
        { label: 'Gender', value: shown(titleCaseToken(personal.gender)) },
        { label: 'Aadhaar number', value: shown(personal.aadhaarNumber) },
      ]
    case 'PAN':
      return [
        { label: 'Name', value: name },
        { label: 'Date of birth', value: dateOfBirth },
        { label: 'PAN', value: shown(personal.panNumber) },
      ]
    case 'PASSPORT':
      return [
        { label: 'Name', value: name },
        { label: 'Date of birth', value: dateOfBirth },
        { label: 'Passport number', value: shown(personal.passportNumber) },
      ]
    case 'VOTER_ID':
      return [
        { label: 'Name', value: name },
        { label: 'Date of birth', value: dateOfBirth },
        { label: 'Voter ID', value: shown(personal.voterId) },
      ]
    case 'DRIVING_LICENSE':
      return [
        { label: 'Name', value: name },
        { label: 'Date of birth', value: dateOfBirth },
        { label: 'Driving licence', value: shown(personal.drivingLicense) },
      ]
  }
}

export function ApprovalDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { session } = useAuth()
  const { application, error, loading, reload } = useApplication(id)
  const [comment, setComment] = useState('')
  const [commentError, setCommentError] = useState<string | null>(null)
  const [actionError, setActionError] = useState<string | null>(null)
  const [working, setWorking] = useState<'approve' | 'reject' | DocumentCode | null>(null)

  if (loading) return <LoadingState label="Loading application" />
  if (error || !application) return <ErrorState message={error ?? 'Application not found.'} onRetry={() => void reload()} />

  const currentGroup = application.verificationGroups[application.currentGroupIndex]

  function finishReview(beforeIndex: number, updated: Application) {
    if (updated.status === 'APPROVED') {
      navigate('/approvals', { replace: true, state: { notice: 'Application approved.' } })
      return
    }
    if (updated.currentGroupIndex !== beforeIndex) {
      navigate('/approvals', { replace: true, state: { notice: 'Verified and sent to the next approver.' } })
      return
    }
    reload({ silent: true })
  }

  async function verify(code: DocumentCode) {
    if (!application) return
    const beforeIndex = application.currentGroupIndex
    setWorking(code)
    setActionError(null)
    try {
      const updated = await verifyDocument(application.id, code)
      finishReview(beforeIndex, updated)
    } catch (err) {
      setActionError(err instanceof ApiError ? err.message : 'Could not verify the document.')
    } finally {
      setWorking(null)
    }
  }

  async function approve() {
    if (!application || !session) return
    const beforeIndex = application.currentGroupIndex
    setWorking('approve')
    setActionError(null)
    try {
      const updated = await approveApplication(application.id)
      finishReview(beforeIndex, updated)
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
      {application.status === 'PENDING_APPROVAL' && currentGroup ? (
        <Stack spacing={2}>
          <Box>
            <Typography variant="h6">Verify {currentGroup.label}</Typography>
            <Typography color="text.secondary">
              Review each document in this group and approve it. The application stays with you until this group is complete.
            </Typography>
          </Box>
          {currentGroup.documents.map((document) => (
            <Paper key={document.code} variant="outlined" sx={{ p: { xs: 2, sm: 3 } }}>
              <Stack spacing={2}>
                <Stack direction="row" spacing={1} sx={{ alignItems: 'center', justifyContent: 'space-between' }}>
                  <Typography variant="h6">{document.label}</Typography>
                  {document.verified ? <Chip size="small" label="Approved" color="success" /> : null}
                </Stack>
                <Box
                  sx={{
                    display: 'grid',
                    gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' },
                    gap: 2,
                  }}
                >
                  {documentDetails(document.code, application.personalDetails).map((field) => (
                    <Box key={field.label}>
                      <Typography variant="caption" color="text.secondary">
                        {field.label}
                      </Typography>
                      <Typography variant="body1">{field.value}</Typography>
                    </Box>
                  ))}
                </Box>
                <Box>
                  <Button
                    variant={document.verified ? 'outlined' : 'contained'}
                    disabled={document.verified || working !== null}
                    onClick={() => void verify(document.code)}
                  >
                    {document.verified
                      ? 'Approved'
                      : working === document.code
                        ? 'Approving…'
                        : `Approve ${document.label}`}
                  </Button>
                </Box>
              </Stack>
            </Paper>
          ))}
          {currentGroup.documents.length > 1 ? (
            <Box>
              <Button variant="contained" color="success" disabled={working !== null} onClick={() => void approve()}>
                {working === 'approve' ? 'Approving…' : 'Approve all'}
              </Button>
            </Box>
          ) : null}
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
      ) : null}
    </Stack>
  )
}
