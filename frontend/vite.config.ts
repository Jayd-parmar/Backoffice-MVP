import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyReq', (proxyReq) => {
            // The API allows browser calls from port 5173. Drop Origin so a
            // different Vite port still reaches the same-origin proxy.
            proxyReq.removeHeader('origin')
          })
        },
      },
    },
  },
})
