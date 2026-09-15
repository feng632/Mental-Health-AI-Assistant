import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import {resolve} from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    // 强制绑定 IPv4 回环地址，避免 Node 优先解析 IPv6(::1) 导致浏览器无法访问
    host: '127.0.0.1',
    port: 5173,
    proxy: {
      // target 不带 /api：vite 会保留原始路径拼上去（/api/user/login），
      // 否则会变成 /api/api/user/login，后端 403
      '/api': {
        target: 'http://159.75.169.224:1235',
        changeOrigin: true
      }
    }
  }
})
