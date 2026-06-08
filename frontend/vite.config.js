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
        rewrite: (path) => path.replace(/^\/api/, '/webApp_project_war_exploded/api'),
        configure: (proxy) => {
          proxy.on('proxyRes', (proxyRes) => {
            // 确保 Set-Cookie 正确传递到前端
            const cookies = proxyRes.headers['set-cookie'];
            if (cookies) {
              proxyRes.headers['set-cookie'] = cookies.map(cookie => {
                // 移除 Domain 属性，使 Cookie 适用于前端域名
                return cookie.replace(/; Domain=[^;]+/i, '');
              });
            }
          });
        }
      }
    }
  }
})
