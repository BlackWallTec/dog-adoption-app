import express from "express"; // Vercel lo usa para detectar el framework
import { createApp } from "./api/src/app.js";

if (!process.env.JWT_SECRET) throw new Error("Falta JWT_SECRET");

const app = createApp(); // tu función ya devuelve la app de Express
export default app; // este es el export que Vercel busca
