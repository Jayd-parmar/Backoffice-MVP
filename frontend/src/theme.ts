import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    primary: {
      main: '#0e3a5d',
      dark: '#08263d',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#8d6a2f',
    },
    background: {
      default: '#eef2f5',
      paper: '#ffffff',
    },
    text: {
      primary: '#1c2430',
      secondary: '#5c6775',
    },
    success: {
      main: '#1f6f4a',
    },
    error: {
      main: '#9b2c2c',
    },
    warning: {
      main: '#9a6700',
    },
    info: {
      main: '#1d4f73',
    },
  },
  typography: {
    fontFamily: '"Segoe UI", "Helvetica Neue", Arial, sans-serif',
    h4: { fontWeight: 600, letterSpacing: '-0.02em' },
    h5: { fontWeight: 600, letterSpacing: '-0.02em' },
    h6: { fontWeight: 600 },
  },
  shape: {
    borderRadius: 10,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: { textTransform: 'none', fontWeight: 600 },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: { backgroundImage: 'none' },
      },
    },
  },
})
