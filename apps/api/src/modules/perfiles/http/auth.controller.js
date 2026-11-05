import "dotenv/config";
import bcrypt from "bcryptjs";
import Usuario from "./modelosPerfiles/user.model.js";
import { validateRegistration } from "../../../../../shared/perfiles/registro.validation.js";
import { cookieBase, respondWithSession } from "./auth.session.js";

const internalError = (res, error) => {
  console.error("Error de autenticación:", error.code || "INTERNAL_ERROR");
  return res.status(500).json({ error: "No pudimos completar la solicitud. Inténtalo más tarde." });
};

export const postLogin = async (req, res) => {
  try {
    const { email, passwd } = req.body ?? {};
    if (
      typeof email !== "string" ||
      typeof passwd !== "string" ||
      !email.trim() ||
      email.length > 254 ||
      !passwd ||
      passwd.length > 1024
    ) {
      return res.status(400).json({ error: "Correo y contraseña son requeridos" });
    }
    const correo = email.trim().toLowerCase();
    const usuario = await Usuario.fetchByEmail(correo);
    if (!usuario?.contrasena || !(await bcrypt.compare(passwd, usuario.contrasena))) {
      return res.status(401).json({ error: "Credenciales inválidas" });
    }
    return respondWithSession(req, res, usuario, correo);
  } catch (error) {
    return internalError(res, error);
  }
};

export const postRegister = async (req, res) => {
  const { value, errors } = validateRegistration(req.body);
  if (Object.keys(errors).length) {
    return res.status(400).json({ error: "Revisa los campos indicados.", fields: errors });
  }
  try {
    const passwordHash = await bcrypt.hash(value.passwd, 12);
    const usuario = await Usuario.register(value, passwordHash);
    return respondWithSession(req, res, usuario, value.email, 201);
  } catch (error) {
    if (error.code === "23505") {
      return res.status(409).json({
        error: "Este correo ya está registrado.",
        fields: { email: "Este correo ya está registrado." },
      });
    }
    return internalError(res, error);
  }
};

export const postLogout = (_req, res) => {
  res.clearCookie("token", cookieBase);
  return res.status(200).json({ ok: true });
};

export const getMe = (req, res) => {
  const { uuid, email, rol } = req.user;
  return res.status(200).json({ user: { id: uuid, email, rol } });
};

export const getProfile = async (req, res) => {
  try {
    const profile = await Usuario.fetchProfile(req.user.uuid);
    if (!profile) return res.status(404).json({ error: "No encontramos tu perfil." });
    return res.json({ profile });
  } catch (error) {
    return internalError(res, error);
  }
};
