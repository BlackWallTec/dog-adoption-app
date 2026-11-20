import { validateEmail } from "@qarino/shared/perfiles/registro.validation.js";
import { errorResponse } from "@qarino/shared/errores.js";
import { correoMovilHabilitado, servicioCorreoMovil } from "../services/correoMovil.service.js";

// El controlador atiende HTTP; shared valida los datos y el servicio resuelve los códigos.
const atender =
  (operacion, status = 200) =>
  async (req, res) => {
    res.set("Cache-Control", "no-store");
    try {
      const correo = validateEmail(req.body?.correo);
      const resultado = await operacion(servicioCorreoMovil(), correo, req.body);
      return res.status(status).json(resultado);
    } catch (error) {
      const respuesta = errorResponse(error);
      if (respuesta.status >= 500) console.error("No se completó la operación de correo móvil.");
      return res.status(respuesta.status).json(respuesta.body);
    }
  };

export const getConfiguracion = (_req, res) =>
  res.set("Cache-Control", "no-store").json({ habilitado: correoMovilHabilitado() });
export const postReenviar = atender((s, correo) => s.solicitar(correo, "confirmar"), 202);
export const postRecuperar = atender((s, correo) => s.solicitar(correo, "recuperar"), 202);
export const postConfirmar = atender((s, correo, b) => s.consumir(correo, b.codigo, "confirmar"));
export const postRestablecer = atender((s, correo, b) =>
  s.consumir(correo, b.codigo, "recuperar", b.contrasena, b.confirmContrasena),
);
