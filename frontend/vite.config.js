import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true, // Forces Vite to use 5173 or fail, so you don't accidentally load an old cached instance
  },
  root: './', // Ensures it looks for index.html in the directory where npm run dev is executed
})