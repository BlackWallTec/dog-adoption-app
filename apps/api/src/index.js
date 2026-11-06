import "dotenv/config";
import { createApp } from "./app.js";

const app = createApp();

if (process.env.NODE_ENV !== "production") {
  if (!process.env.JWT_SECRET) {
    throw new Error("No existe el secreto de JWT en tu .env");
  }

  const PORT = process.env.PORT || 3000;

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Servidor escuchando en http://localhost:${PORT}`);
  });
}

export default app;
