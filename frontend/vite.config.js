import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            const cookies = proxyRes.headers['set-cookie']
            if (cookies) {
              proxyRes.headers['set-cookie'] = cookies.map(c => c.replace(/; Domain=[^;]+/i, ''))
            }
          })
        }
      }
    }
  }
})
