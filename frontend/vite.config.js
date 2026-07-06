import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  // React plugin for JSX/TSX support and Fast Refresh
  plugins: [react()],
  server: {
    port: 5173,               // Development server port
    strictPort: true,         // Fail if port is already in use
    host: '0.0.0.0',          // Listen on all network interfaces (for Docker/network access)
    proxy: {
      '/api/v1': {            // Forward API requests to backend
        target: 'http://localhost:8080',
        changeOrigin: true,   // Rewrite origin header to match target
      },
    },
  },
  preview: {
    port: 4173,               // Preview (production build) server port
    strictPort: true,
    host: '0.0.0.0',
  },
});
