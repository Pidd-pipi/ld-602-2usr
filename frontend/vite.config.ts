import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// 本地开发时将 /api 代理到后端（默认 21102），与 nginx 生产代理保持一致。
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 20102,
    host: "0.0.0.0",
    proxy: {
      "/api": {
        target: `http://localhost:${process.env.BACKEND_PORT ?? 21102}`,
        changeOrigin: true
      }
    }
  }
});
