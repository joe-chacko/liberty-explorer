import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  base: '/liberty-explorer/static/',
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:9080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
})

// Made with Bob
