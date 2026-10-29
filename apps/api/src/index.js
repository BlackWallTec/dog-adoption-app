import "dotenv/config";
import express from "express";
import cors from "cors";

// Import de routers de los módulos
import catalogoRoutes from "./modules/catalogo/http/catalog.router.js";

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

// Middleware para ver qué rutas se piden
app.use((req, res, next) => {
  console.log(`Se solicitó: ${req.method} ${req.url}`);
  next();
});

// Config del cors
app.use(
  cors({
    origin: "*", // Permite el origen de vite y otros dominios como android
    credentials: true, // Requerido para cookies y credenciales
  }),
);

// Rutas
app.use("/api/catalogo", catalogoRoutes);

// Iniciar el servidor
//Añadimos el 0.0.0.0 para que sea accesible desde cualquier IP, no solo localhost
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Servidor escuchando en http://localhost:${PORT}`);
});
