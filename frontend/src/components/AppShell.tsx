import AppBar from '@mui/material/AppBar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Container from '@mui/material/Container'
import Stack from '@mui/material/Stack'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import { Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../useAuth'
import { roleLabel } from '../format'

export function AppShell() {
  const { session, logout } = useAuth()
  const navigate = useNavigate()

  return (
    <Box sx={{ minHeight: '100svh', bgcolor: 'background.default' }}>
      <AppBar position="sticky" elevation={0} sx={{ bgcolor: 'primary.main', borderBottom: '3px solid #c4a15a' }}>
        <Toolbar sx={{ gap: 2 }}>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            Forex Backoffice
          </Typography>
          {session ? (
            <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
              <Typography variant="body2" sx={{ display: { xs: 'none', sm: 'block' } }}>
                {session.email}
              </Typography>
              <Chip
                size="small"
                label={roleLabel(session.role)}
                sx={{ bgcolor: 'rgba(255,255,255,0.16)', color: 'white' }}
              />
              <Button
                color="inherit"
                onClick={() => {
                  logout()
                  navigate('/', { replace: true })
                }}
              >
                Log out
              </Button>
            </Stack>
          ) : null}
        </Toolbar>
      </AppBar>
      <Container maxWidth="lg" sx={{ py: { xs: 3, sm: 4 } }}>
        <Outlet />
      </Container>
    </Box>
  )
}
