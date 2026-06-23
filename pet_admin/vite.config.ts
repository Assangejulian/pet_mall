import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"
export default defineConfig({
  plugins: [vue()],
  server: { proxy: { "/api": { target: "http://192.168.1.100:8080", changeOrigin: true } } }
})