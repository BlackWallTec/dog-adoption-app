import "dotenv/config";
import express from "express";
import cors from "cors";

// Import de routers de los módulos
import catalogoRoutes from "./modules/catalogo/http/catalog.router.js";
import { router as solicitudesRoutes } from "./modules/solicitudes/index.js";

const app = express();
const PORT = process.env.PORT || 3000;

//Middleware
app.use(express.json());

// Middleware to see what routes are asking
app.use((req, res, next) => {
  console.log(`Se solicitó: ${req.method} ${req.url}`);
  next();
});

app.use(
  cors({
    origin: "*", //Allows de domain of android
    credentials: true, //Requiered for cookies and credentials
  }),
);

//Routes
app.use("/api/catalogo", catalogoRoutes);
app.use("/api/solicitudes", solicitudesRoutes);

//Start server
app.listen(PORT, "0.0.0.0", () => {
  console.log(`Servidor escuchando en http://localhost:${PORT}`);
});
