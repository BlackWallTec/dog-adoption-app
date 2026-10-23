import { defineConfig } from "vite-plus";

export default defineConfig({
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:3000",
        changeOrigin: true,
      },
    },
  },
});

// Config temporal para algunas pruebas en local. Se debe cambiar por la URL del servidor backend en producción.
