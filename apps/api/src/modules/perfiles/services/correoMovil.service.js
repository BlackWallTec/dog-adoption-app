import { createHmac, randomInt } from "node:crypto";
import { mkdirSync, readFileSync, renameSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import bcrypt from "bcryptjs";
import Usuario from "../http/modelosPerfiles/user.model.js";
import ROLES from "../http/modelosPerfiles/roles.model.js";

const MINUTO = 60_000;
const GENERICO = { message: "Si la cuenta corresponde, recibirás un código en tu correo." };
export class ErrorCorreoMovil extends Error {
  constructor(message, status = 400, code = "INVALID_EMAIL_CODE") {
    super(message);
    this.status = status;
    this.code = code;
  }
}

// Extensión local: no sustituye bcrypt/JWT ni modifica las cuentas anteriores.
// El almacenamiento transaccional en Supabase se realizará antes de desplegar este MVP.
export function crearCorreoMovil({
  archivo,
  usuarios = Usuario,
  enviar,
  secreto,
  ahora = () => Date.now(),
  codigo = () => String(randomInt(0, 1_000_000)).padStart(6, "0"),
}) {
  if (!secreto) throw new Error("Falta el secreto del servidor.");
  let estado = { cuentas: {} };
  try {
    estado = JSON.parse(readFileSync(archivo, "utf8"));
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }
  const guardar = () => {
    mkdirSync(dirname(archivo), { recursive: true });
    writeFileSync(`${archivo}.tmp`, JSON.stringify(estado), { mode: 0o600 });
    renameSync(`${archivo}.tmp`, archivo);
  };
  const hash = (id, tipo, valor) =>
    createHmac("sha256", secreto).update(`${id}:${tipo}:${valor}`).digest("hex");
  // Serializa emisiones y consumos para impedir reutilización simultánea de códigos.
  let cola = Promise.resolve();
  const serializar = (operacion) => {
    const resultado = cola.then(operacion);
    cola = resultado.catch(() => {});
    return resultado;
  };
  const invalido = () => {
    throw new ErrorCorreoMovil("El código no es válido o venció. Solicita uno nuevo.");
  };
  async function emitir(id, correo, tipo) {
    const cuenta = estado.cuentas[id];
    if (cuenta[tipo] && ahora() - cuenta[tipo].enviado < MINUTO)
      throw new ErrorCorreoMovil(
        "Espera un minuto antes de solicitar otro código.",
        429,
        "RATE_LIMITED",
      );
    const valor = codigo();
    cuenta[tipo] = {
      hash: hash(id, tipo, valor),
      enviado: ahora(),
      vence: ahora() + (tipo === "confirmar" ? 72 * 60 : 15) * MINUTO,
      intentos: 0,
    };
    guardar();
    await enviar({ para: correo, codigo: valor, tipo });
  }
  return {
    estado(id) {
      const cuenta = estado.cuentas[id];
      return { confirmado: cuenta?.confirmado ?? true, version: cuenta?.version ?? 0 };
    },
    registrar(id, correo) {
      return serializar(async () => {
        estado.cuentas[id] = { confirmado: false, version: 0 };
        guardar();
        try {
          await emitir(id, correo, "confirmar");
          return { enviado: true };
        } catch {
          console.error(
            "No se entregó la confirmación móvil; la cuenta puede solicitar otro código.",
          );
          return { enviado: false };
        }
      });
    },
    solicitar(correo, tipo) {
      return serializar(async () => {
        const usuario = await usuarios.fetchByEmail(correo);
        if (usuario?.rol === ROLES.CLIENT) {
          const id = usuario.id_usuario;
          estado.cuentas[id] ??= { confirmado: true, version: 0 };
          const cuenta = estado.cuentas[id];
          if (
            (tipo === "confirmar" && !cuenta.confirmado) ||
            (tipo === "recuperar" && cuenta.confirmado)
          ) {
            try {
              await emitir(id, correo, tipo);
            } catch (error) {
              // El tiempo de espera tampoco revela si el correo pertenece a una cuenta.
              if (error instanceof ErrorCorreoMovil) return GENERICO;
              console.error("No se entregó el código de correo móvil.");
            }
          }
        }
        return GENERICO;
      });
    },
    consumir(correo, valor, tipo, contrasena, confirmContrasena) {
      return serializar(async () => {
        if (typeof valor !== "string" || !/^\d{6}$/.test(valor)) invalido();
        const usuario = await usuarios.fetchByEmail(correo);
        if (usuario?.rol !== ROLES.CLIENT) invalido();
        const id = usuario.id_usuario;
        const cuenta = estado.cuentas[id];
        const desafio = cuenta?.[tipo];
        if (!desafio || desafio.vence <= ahora() || desafio.intentos >= 5) invalido();
        if (desafio.hash !== hash(id, tipo, valor)) {
          desafio.intentos += 1;
          guardar();
          invalido();
        }
        if (tipo === "recuperar") {
          if (
            typeof contrasena !== "string" ||
            contrasena.length < 8 ||
            Buffer.byteLength(contrasena, "utf8") > 72 ||
            /[\r\n]/.test(contrasena) ||
            contrasena !== confirmContrasena
          )
            throw new ErrorCorreoMovil(
              "Usa al menos 8 caracteres, máximo 72 bytes, y repite la misma contraseña.",
              422,
              "VALIDATION",
            );
          await usuarios.updatePassword(id, await bcrypt.hash(contrasena, 10));
          // No consume el código ni confirma éxito si la contraseña no quedó guardada.
          const actualizado = await usuarios.fetchByEmail(correo);
          if (
            actualizado?.id_usuario !== id ||
            !actualizado.contrasena ||
            !(await bcrypt.compare(contrasena, actualizado.contrasena))
          )
            throw new ErrorCorreoMovil(
              "No se pudo comprobar el cambio de contraseña. Inténtalo de nuevo.",
              503,
              "PASSWORD_UPDATE_FAILED",
            );
          cuenta.version += 1;
        } else cuenta.confirmado = true;
        delete cuenta[tipo];
        guardar();
        return { ok: true };
      });
    },
  };
}

let servicio;
export const correoMovilHabilitado = () => process.env.CORREO_MOVIL_MODO === "local";
export function servicioCorreoMovil() {
  if (!correoMovilHabilitado())
    throw new ErrorCorreoMovil(
      "El correo móvil todavía no está habilitado.",
      503,
      "EMAIL_UNAVAILABLE",
    );
  if (process.env.NODE_ENV === "production")
    throw new ErrorCorreoMovil(
      "El almacenamiento local de correo no está habilitado en producción.",
      503,
      "EMAIL_UNAVAILABLE",
    );
  servicio ??= crearCorreoMovil({
    archivo: resolve(process.env.CORREO_MOVIL_ARCHIVO || ".local/correo-movil.json"),
    secreto: process.env.JWT_SECRET,
    enviar: enviarCodigoResend,
  });
  return servicio;
}

// La clave se usa sólo en el servidor. El código nunca se devuelve en respuestas HTTP.
export async function enviarCodigoResend({ para, codigo, tipo }) {
  const remitente = process.env.MAIL_FROM?.trim();
  if (!process.env.RESEND_API_KEY || !/^[^\s@<>]+@[^\s@<>]+\.[^\s@<>]+$/.test(remitente || ""))
    throw new Error("Falta configurar Resend y su remitente.");
  const respuesta = await fetch("https://api.resend.com/emails", {
    method: "POST",
    signal: AbortSignal.timeout(8_000),
    headers: {
      Authorization: `Bearer ${process.env.RESEND_API_KEY}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      from: `Qariño Animal <${remitente}>`,
      to: [para],
      subject:
        tipo === "confirmar" ? "Confirma tu cuenta de Qariño Animal" : "Recupera tu contraseña",
      text: `Tu código es: ${codigo}\n\nEscríbelo en la aplicación Qariño Animal. ${tipo === "confirmar" ? "Vence en 72 horas." : "Vence en 15 minutos."}\n\nSi no lo solicitaste, ignora este correo.`,
    }),
  });
  if (!respuesta.ok) throw new Error(`Resend rechazó el envío (HTTP ${respuesta.status}).`);
  return respuesta.json();
}

export function verificarCorreoMovil(usuario) {
  if (!correoMovilHabilitado() || usuario.rol !== ROLES.CLIENT) return true;
  const estado = servicioCorreoMovil().estado(usuario.uuid);
  return estado.confirmado && estado.version === (usuario.correoVersionMovil ?? 0);
}
