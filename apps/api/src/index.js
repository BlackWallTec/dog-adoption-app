import "dotenv/config";
import express from "express";
import cors from "cors";
import { router as solicitudesRoutes } from "./modules/solicitudes/http/routes.js";
import { router as authRoutes } from "./modules/solicitudes/http/routes.js";
import { createApp } from "./app.js";

if (!process.env.JWT_SECRET) {
  throw new Error("No existe el secreto de JWT en tu .env");
}

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
app.use("/api/auth", authRoutes);
app.use("/api/solicitudes", solicitudesRoutes);

// Iniciar el servidor
//Añadimos el 0.0.0.0 para que sea accesible desde cualquier IP, no solo localhost
createApp().listen(PORT, "0.0.0.0", () => {
  console.log(`Servidor escuchando en http://localhost:${PORT}`);
});
