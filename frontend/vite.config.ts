import react from '@vitejs/plugin-react'
import type { ProxyOptions } from 'vite'
import { defineConfig } from 'vite'

const proxyOptions: Pick<ProxyOptions, 'changeOrigin' | 'configure'> = {
  changeOrigin: true,
  configure: (proxy) => {
    proxy.on('proxyReq', (proxyReq) => {
      // The API allows browser calls from port 5173. Drop Origin so a
      // different Vite port still reaches the same-origin proxy.
      proxyReq.removeHeader('origin')
    })
  },
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/approvals': {
        target: 'http://localhost:8082',
        ...proxyOptions,
      },
      '/api': {
        target: 'http://localhost:8081',
        ...proxyOptions,
      },
      '/approval-api': {
        target: 'http://localhost:8082',
        rewrite: (path) => path.replace(/^\/approval-api/, '/api'),
        ...proxyOptions,
      },
    },
  },
})
