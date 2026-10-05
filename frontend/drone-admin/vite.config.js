import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * Vite 构建配置。
 * 开发服务器固定 3000 端口，并把 /api 前缀的请求代理到后端服务（端口与后端
 * application.yml 中的 server.port 保持一致），以规避开发环境的跨域问题。
 */
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8084',
        changeOrigin: true
      }
    }
  }
})
