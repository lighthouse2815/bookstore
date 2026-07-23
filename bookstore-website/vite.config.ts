import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'node:path'

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    rolldownOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('/src/locales/messages.ts')) {
            return 'i18n'
          }

          if (!id.includes('node_modules')) {
            return
          }

          if (id.includes('react-router-dom')) {
            return 'router'
          }

          if (id.includes('react-dom') || id.includes('/react/')) {
            return 'react-vendor'
          }

          if (id.includes('lucide-react')) {
            return 'icons'
          }

          if (
            id.includes('@base-ui') ||
            id.includes('class-variance-authority') ||
            id.includes('clsx') ||
            id.includes('tailwind-merge') ||
            id.includes('sonner') ||
            id.includes('styled-components')
          ) {
            return 'ui-vendor'
          }

          if (id.includes('axios')) {
            return 'network'
          }

          return 'vendor'
        },
      },
    },
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src/'),
    },
  },
  server: {
    host: '127.0.0.1',
    allowedHosts: ['localhost'],
  },
})
