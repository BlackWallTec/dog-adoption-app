// Vista previa estática para trabajar la interfaz sin instalar ni iniciar el backend.
import { createServer } from "node:http";
import { readFile, stat } from "node:fs/promises";
import { dirname, extname, isAbsolute, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const port = Number(process.env.WEB_PREVIEW_PORT || 5173);
const mimeTypes = {
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".webp": "image/webp",
  ".ico": "image/x-icon",
};

const server = createServer(async (req, res) => {
  if (!["GET", "HEAD"].includes(req.method)) {
    res.writeHead(405, { Allow: "GET, HEAD" }).end();
    return;
  }

  try {
    const pathname = decodeURIComponent(new URL(req.url, "http://localhost").pathname);
    let file = resolve(root, `.${pathname}`);
    const relativePath = relative(root, file);
    if (relativePath.startsWith("..") || isAbsolute(relativePath)) {
      res.writeHead(403).end("Acceso denegado");
      return;
    }
    if (pathname.startsWith("/api/")) {
      res.writeHead(501).end("Esta vista previa sólo sirve el frontend.");
      return;
    }
    let fileInfo;
    try {
      fileInfo = await stat(file);
    } catch (error) {
      if (error.code !== "ENOENT") throw error;
      file = resolve(root, "public", relativePath);
      fileInfo = await stat(file);
    }
    if (fileInfo.isDirectory()) file = resolve(file, "index.html");
    // Sólo se sirven recursos web; no archivos de configuración o variables de entorno.
    const type = mimeTypes[extname(file)];
    if (!type) {
      res.writeHead(404).end("No encontrado");
      return;
    }
    const content = await readFile(file);
    res.writeHead(200, { "Content-Type": type, "Cache-Control": "no-store" });
    res.end(req.method === "HEAD" ? undefined : content);
  } catch (error) {
    res.writeHead(error.code === "ENOENT" || error.code === "ENOTDIR" ? 404 : 400);
    res.end("No se pudo abrir el recurso");
  }
});

server.on("error", (error) => {
  console.error(`No se pudo iniciar la vista previa: ${error.message}`);
  process.exitCode = 1;
});
server.listen(port, "127.0.0.1", () => {
  console.log(`Vista previa web: http://127.0.0.1:${port}`);
});
process.on("SIGINT", () => server.close());
process.on("SIGTERM", () => server.close());
