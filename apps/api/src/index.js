import "dotenv/config";
import express from "express";
import cors from "cors";

// Import de routers de los módulos
import catalogoRoutes from "./modules/catalogo/http/catalog.router.js";

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(express.json());

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
app.listen(PORT, () => {
  console.log(`API corriendo en http://localhost:${PORT}`);
});
