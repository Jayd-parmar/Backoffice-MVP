import { createRoot } from 'react-dom/client'
import { CssBaseline, ThemeProvider } from '@mui/material'
import App from './App.tsx'
import { AuthProvider } from './auth.tsx'
import { theme } from './theme.ts'
import './index.css'

createRoot(document.getElementById('root')!).render(
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AuthProvider>
        <App />
      </AuthProvider>
    </ThemeProvider>
)
