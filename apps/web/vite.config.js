import { copyFileSync, existsSync, mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig } from "vite-plus"; // o 'vite', según lo que ya importes
import { PANTALLAS, archivoDeRuta } from "./JS/rutas.js";

const r = (p) => resolve(fileURLToPath(new URL(".", import.meta.url)), p);

function reescribirRutas(req, _res, next) {
  const destino = archivoDeRuta(req.url ?? "");
  if (!destino) {
    next();
    return;
  }

  const archivo = destino.split("?")[0].replace(/^\//, "");
  if (!existsSync(r(archivo))) {
    next();
    return;
  }

  req.url = destino;
  next();
}

function pluginRutas() {
  return {
    name: "rutas-admin",
    configureServer(server) {
      server.middlewares.use(reescribirRutas);
    },
    configurePreviewServer(server) {
      server.middlewares.use(reescribirRutas);
    },
    writeBundle(outputOptions) {
      const dir = outputOptions.dir ?? r("dist");
      for (const pantalla of PANTALLAS) {
        if (!pantalla.archivo) continue;
        const origen = resolve(dir, pantalla.archivo.replace(/^\//, ""));
        if (!existsSync(origen)) continue;
        const destino = resolve(dir, pantalla.ruta.replace(/^\//, ""), "index.html");
        mkdirSync(dirname(destino), { recursive: true });
        copyFileSync(origen, destino);
      }
    },
  };
}

export default defineConfig({
  appType: "mpa",
  plugins: [pluginRutas()],
  build: {
    rollupOptions: {
      input: {
        main: r("index.html"),
        login: r("HTML/htmlPerfiles/login.html"),
        registro: r("HTML/htmlPerfiles/registro.html"),
        adminPerros: r("HTML/htmlCatalogo/adminPerros.html"),
        adminDonativos: r("HTML/htmlDonativos/adminDonativos.html"),
        resumenDonativos: r("HTML/htmlDonativos/resumenDonativos.html"),
        adminSolicitudesCfdi: r("HTML/htmlDonativos/adminSolicitudesCfdi.html"),
        adminVoluntariado: r("HTML/htmlVoluntariado/adminVoluntariado.html"),
        adminInscritos: r("HTML/htmlVoluntariado/adminInscritos.html"),
        createVolunteering: r("HTML/htmlVoluntariado/createVolunteering.html"),
        adminSolicitudes: r("HTML/htmlSolicitudes/adminSolicitudes.html"),
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
