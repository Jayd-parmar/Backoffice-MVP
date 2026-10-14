import { useState, type FormEvent } from 'react'
import { Navigate } from 'react-router-dom'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import TextField from '@mui/material/TextField'
import ToggleButton from '@mui/material/ToggleButton'
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup'
import Typography from '@mui/material/Typography'
import { ApiError, login, registerAccount, type AuthPortal } from '../api'
import { useAuth } from '../useAuth'
import { LoadingState } from '../components/PageState'
import { roleLabel } from '../format'
import type { Role } from '../types'

const approverRoles: Role[] = ['APPROVER_1', 'APPROVER_2', 'APPROVER_3']

export function AuthPage() {
  const { session, ready } = useAuth()
  const [portal, setPortal] = useState<AuthPortal>('customer')
  const [tab, setTab] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [phone, setPhone] = useState('')
  const [role, setRole] = useState<Role>('CUSTOMER')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (!ready) return <LoadingState label="Checking your session" />
  if (session?.role === 'CUSTOMER') return <Navigate to="/home" replace />
  if (session) return <Navigate to="/approvals" replace />

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    if (tab === 'register') {
      if (!name.trim()) {
        setError('Name is required')
        return
      }
      if (password.length < 8 || password.length > 72) {
        setError('Password must be 8 to 72 characters')
        return
      }
      if (phone.trim() && !/^[0-9+\- ]{7,20}$/.test(phone.trim())) {
        setError('Phone is invalid')
        return
      }
    }
    setSubmitting(true)
    try {
      const accountRole = portal === 'customer' ? 'CUSTOMER' : role
      if (tab === 'register') {
        await registerAccount({ email, password, role: accountRole, name, phone, portal })
      }
      await login(email, password, portal)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Could not sign you in.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Box
      sx={{
        minHeight: '100svh',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: 'minmax(280px, 420px) 1fr' },
      }}
    >
      <Box
        sx={{
          display: { xs: 'none', md: 'flex' },
          flexDirection: 'column',
          justifyContent: 'space-between',
          bgcolor: 'primary.main',
          color: 'primary.contrastText',
          p: 5,
          borderRight: '4px solid #c4a15a',
        }}
      >
        <Box>
          <Typography variant="h4" sx={{ color: 'inherit' }}>
            Forex Backoffice
          </Typography>
          <Typography sx={{ mt: 2, color: 'rgba(255,255,255,0.82)' }}>
            Customers save a draft, review it, and submit it. Approvers verify documents in the order set for the destination country.
          </Typography>
        </Box>
        <Stack spacing={2}>
          <Typography variant="body2">Customer — fill the application, review it, then submit.</Typography>
          <Typography variant="body2">Approver 1 — verifies the first document group.</Typography>
          <Typography variant="body2">Approver 2 — verifies the next group after Approver 1 finishes.</Typography>
          <Typography variant="body2">Approver 3 — verifies the last group after Approver 2 finishes.</Typography>
        </Stack>
      </Box>

      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', p: { xs: 2, sm: 4 } }}>
        <Paper variant="outlined" sx={{ width: '100%', maxWidth: 460, p: { xs: 2.5, sm: 4 } }}>
          <Typography variant="h5" sx={{ display: { md: 'none' }, mb: 2 }}>
            Forex Backoffice
          </Typography>
          <ToggleButtonGroup
            exclusive
            fullWidth
            value={portal}
            onChange={(_event, value: AuthPortal | null) => {
              if (!value) return
              setPortal(value)
              setRole(value === 'customer' ? 'CUSTOMER' : 'APPROVER_1')
              setError(null)
            }}
            sx={{ mb: 1 }}
          >
            <ToggleButton value="customer">Customer</ToggleButton>
            <ToggleButton value="approver">Approver</ToggleButton>
          </ToggleButtonGroup>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            {portal === 'customer'
              ? 'Customer sign-in uses the application service on port 8081.'
              : 'Approver sign-in uses the approval service on port 8082.'}
          </Typography>
          <Tabs
            value={tab}
            onChange={(_event, value: 'login' | 'register') => {
              setTab(value)
              setError(null)
            }}
            sx={{ mb: 3 }}
          >
            <Tab value="login" label="Log in" />
            <Tab value="register" label="Register" />
          </Tabs>
          <Stack component="form" spacing={2} onSubmit={onSubmit} noValidate>
            {error ? <Alert severity="error">{error}</Alert> : null}
            {tab === 'register' ? (
              <>
                <TextField
                  label="Name"
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  slotProps={{ htmlInput: { maxLength: 100 } }}
                  autoComplete="name"
                  fullWidth
                  required
                />
                {portal === 'approver' ? (
                  <TextField
                    select
                    label="Role"
                    value={role}
                    onChange={(event) => setRole(event.target.value as Role)}
                    fullWidth
                  >
                    {approverRoles.map((item) => (
                      <MenuItem key={item} value={item}>
                        {roleLabel(item)}
                      </MenuItem>
                    ))}
                  </TextField>
                ) : null}
                <TextField
                  label="Phone"
                  value={phone}
                  onChange={(event) => setPhone(event.target.value)}
                  helperText="Optional"
                  autoComplete="tel"
                  fullWidth
                />
              </>
            ) : null}
            <TextField
              label="Email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              fullWidth
              required
            />
            <TextField
              label="Password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              helperText={tab === 'register' ? '8 to 72 characters' : undefined}
              autoComplete={tab === 'register' ? 'new-password' : 'current-password'}
              fullWidth
              required
            />
            <Button type="submit" variant="contained" size="large" disabled={submitting}>
              {submitting ? 'Please wait…' : tab === 'register' ? 'Create account' : 'Log in'}
            </Button>
          </Stack>
        </Paper>
      </Box>
    </Box>
  )
}
