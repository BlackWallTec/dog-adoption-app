import express from "express"; // para que Vercel detecte el framework
import { createApp } from "./apps/api/src/app.js";

if (!process.env.JWT_SECRET) throw new Error("Falta JWT_SECRET");

export default createApp();
