import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    // 在这里添加 allowedHosts
    //allowedHosts: ['testlinkhub.cspioneer.tech'],

    // 保留您原来的 proxy 配置
    proxy: {
      '/api': {
        target: 'http://localhost:8087',
        changeOrigin: true,
      },
    }
  }
})
