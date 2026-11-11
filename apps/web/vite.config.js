import { defineConfig } from "vite-plus"; // o 'vite', según lo que ya importes
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const r = (p) => resolve(fileURLToPath(new URL(".", import.meta.url)), p);

export default defineConfig({
  build: {
    rollupOptions: {
      input: {
        main: r("index.html"),
        login: r("HTML/htmlPerfiles/login.html"),
        registro: r("HTML/htmlPerfiles/registro.html"),
        adminPerros: r("HTML/htmlCatalogo/adminPerros.html"),
        adminVoluntariado: r("HTML/htmlVoluntariado/adminVoluntariado.html"),
        adminInscritos: r("HTML/htmlVoluntariado/adminInscritos.html"),
        createVolunteering: r("HTML/htmlVoluntariado/createVolunteering.html"),
      },
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:3000",
        changeOrigin: true,
      },
    },
  },
});
