import express from "express";
import cookieParser from "cookie-parser";

import { router as perfiles } from "./modules/perfiles/index.js";
import { router as donativos } from "./modules/donativos/index.js";
import { router as solicitudes } from "./modules/solicitudes/index.js";
import { router as catalogo } from "./modules/catalogo/index.js";
import { router as voluntarios } from "./modules/voluntarios/index.js";

export function createApp() {
  const app = express();

  app.use(
    cors({
      origin: process.env.CORS_ORIGIN || "http://localhost:5173",
      credentials: true,
    }),
  );

  app.use(express.json());
  app.use(cookieParser);

  app.get("/health", (req, res) => res.json({ status: "ok" }));

  app.use("/api/perfiles", perfiles);
  app.use("/api/donativos", donativos);
  app.use("/api/solicitudes", solicitudes);
  app.use("/api/catalogo", catalogo);
  app.use("/api/voluntarios", voluntarios);

  return app;
}
