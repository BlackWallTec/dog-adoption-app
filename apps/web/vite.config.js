import { defineConfig } from "vite-plus"; // o 'vite', según lo que ya importes
import { resolve } from "node:path";
import { fileURLToPath } from "node:url";

const r = (p) => resolve(fileURLToPath(new URL(".", import.meta.url)), p);

export default defineConfig({
  build: {
    rollupOptions: {
      input: {
        main: r("index.html"),
        adminPerros: r("HTML/htmlCatalogo/adminPerros.html"),
      },
    },
  },
});
