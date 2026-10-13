import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'

export function LoadingState({ label }: { label: string }) {
  return (
    <Stack spacing={2} sx={{ py: 8, alignItems: 'center' }}>
      <CircularProgress size={28} />
      <Typography color="text.secondary">{label}</Typography>
    </Stack>
  )
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <Stack spacing={2} sx={{ py: 2, maxWidth: 640 }}>
      <Alert severity="error">{message}</Alert>
      {onRetry ? (
        <Box>
          <Button variant="outlined" onClick={onRetry}>
            Try again
          </Button>
        </Box>
      ) : null}
    </Stack>
  )
}
