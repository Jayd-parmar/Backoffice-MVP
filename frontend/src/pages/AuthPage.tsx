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
import Typography from '@mui/material/Typography'
import { ApiError, login, registerAccount } from '../api'
import { useAuth } from '../useAuth'
import { LoadingState } from '../components/PageState'
import { roleLabel } from '../format'
import type { Role } from '../types'

const roles: Role[] = ['CUSTOMER', 'APPROVER_1', 'APPROVER_2']

export function AuthPage() {
  const { session, ready } = useAuth()
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
      if (tab === 'register') {
        await registerAccount({ email, password, role, name, phone })
      }
      await login(email, password)
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
            Customers save a draft, review it, and submit it for two levels of approval.
          </Typography>
        </Box>
        <Stack spacing={2}>
          <Typography variant="body2">Customer — fill the application, review it, then submit.</Typography>
          <Typography variant="body2">Approver 1 — review applications waiting for the first decision.</Typography>
          <Typography variant="body2">Approver 2 — review applications Approver 1 has already approved.</Typography>
        </Stack>
      </Box>

      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', p: { xs: 2, sm: 4 } }}>
        <Paper variant="outlined" sx={{ width: '100%', maxWidth: 460, p: { xs: 2.5, sm: 4 } }}>
          <Typography variant="h5" sx={{ display: { md: 'none' }, mb: 2 }}>
            Forex Backoffice
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
                <TextField
                  select
                  label="Role"
                  value={role}
                  onChange={(event) => setRole(event.target.value as Role)}
                  fullWidth
                >
                  {roles.map((item) => (
                    <MenuItem key={item} value={item}>
                      {roleLabel(item)}
                    </MenuItem>
                  ))}
                </TextField>
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
