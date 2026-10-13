import { useEffect, useState } from 'react'
import { Link as RouterLink, useLocation } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { ApiError, listPending } from '../api'
import { useAuth } from '../useAuth'
import { ErrorState, LoadingState } from '../components/PageState'
import { StatusChip } from '../components/StatusChip'
import { formatAmount, formatDate, roleLabel } from '../format'
import type { Application } from '../types'

export function ApprovalsPage() {
  const { session } = useAuth()
  const location = useLocation()
  const notice = (location.state as { notice?: string } | null)?.notice
  const [applications, setApplications] = useState<Application[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    async function load() {
      setLoading(true)
      setError(null)
      try {
        const pending = await listPending()
        if (!cancelled) setApplications(pending)
      } catch (err) {
        if (!cancelled) setError(err instanceof ApiError ? err.message : 'Could not load the queue.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }
    void load()
    return () => {
      cancelled = true
    }
  }, [])

  const title = session ? `${roleLabel(session.role)} queue` : 'Approval queue'

  return (
    <Stack spacing={2.5}>
      <Box>
        <Typography variant="h4">{title}</Typography>
        <Typography color="text.secondary" sx={{ mt: 1 }}>
          {session?.role === 'APPROVER_2'
            ? 'Applications Approver 1 has approved and sent to you.'
            : 'Applications waiting for your approval. Open one to see the full details.'}
        </Typography>
      </Box>
      {notice ? <Alert severity="success">{notice}</Alert> : null}
      {loading ? <LoadingState label="Loading pending applications" /> : null}
      {error ? <ErrorState message={error} onRetry={() => window.location.reload()} /> : null}
      {!loading && !error && applications.length === 0 ? (
        <Paper variant="outlined" sx={{ p: 3 }}>
          <Typography>No applications are waiting for you.</Typography>
        </Paper>
      ) : null}
      {!loading && !error
        ? applications.map((application) => (
            <Paper key={application.id} variant="outlined" sx={{ p: { xs: 2, sm: 2.5 } }}>
              <Stack
                direction={{ xs: 'column', sm: 'row' }}
                spacing={2}
                sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
              >
                <Box>
                  <Stack direction="row" spacing={1} sx={{ mb: 0.5, alignItems: 'center' }}>
                    <Typography variant="h6">
                      {application.personalDetails.firstName} {application.personalDetails.lastName}
                    </Typography>
                    <StatusChip status={application.status} />
                  </Stack>
                  <Typography variant="body2" color="text.secondary">
                    {application.personalDetails.email} · PAN {application.personalDetails.panNumber}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {formatAmount(application.forexCardDetails.amount, application.forexCardDetails.currency)} ·{' '}
                    {application.travelDetails.destinationCountry} ·{' '}
                    {formatDate(application.travelDetails.departureDate)}
                  </Typography>
                </Box>
                <Button variant="contained" component={RouterLink} to={`/approvals/${application.id}`}>
                  View details
                </Button>
              </Stack>
            </Paper>
          ))
        : null}
    </Stack>
  )
}
