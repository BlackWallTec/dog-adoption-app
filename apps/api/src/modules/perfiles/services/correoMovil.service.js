import { createHmac, randomInt } from "node:crypto";
import { mkdirSync, readFileSync, renameSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import bcrypt from "bcryptjs";
import Usuario from "../http/modelosPerfiles/user.model.js";
import ROLES from "../../../config/roles.js";
import {
  BAD_REQUEST,
  RATE_LIMITED,
  VALIDATION,
  INTERNAL,
  SERVICE_UNAVAILABLE,
  ERROR_CODES,
} from "@qarino/shared/errores.js";
import { enviarCorreo } from "../../../utils/correo.js";
import { correoMovilHabilitado, modoCorreoMovil } from "./correoMovil.config.js";

export { correoMovilHabilitado };

const MINUTO = 60_000;
const GENERICO = { message: "Si la cuenta corresponde, recibirás un código en tu correo." };
const TIPOS = new Set(["confirmar", "recuperar"]);
const generarCodigo = () => String(randomInt(0, 1_000_000)).padStart(6, "0");
const MENSAJE_CONTRASENA =
  "Usa al menos 8 caracteres, máximo 72 bytes, y repite la misma contraseña.";

function validarContrasena(contrasena, confirmContrasena) {
  if (
    typeof contrasena !== "string" ||
    contrasena.length < 8 ||
    Buffer.byteLength(contrasena, "utf8") > 72 ||
    /[\r\n]/.test(contrasena) ||
    contrasena !== confirmContrasena
  )
    throw new VALIDATION(MENSAJE_CONTRASENA, { contrasena: MENSAJE_CONTRASENA });
}
const codigoInvalido = () => {
  throw new BAD_REQUEST(
    "El código no es válido o venció. Solicita uno nuevo.",
    ERROR_CODES.INVALID_EMAIL_CODE,
  );
};
const hmac = (secreto) => (id, tipo, valor) =>
  createHmac("sha256", secreto).update(`${id}:${tipo}:${valor}`).digest("hex");

// Modo local (sólo desarrollo y pruebas): el estado vive en un archivo de esta máquina.
// No sustituye bcrypt/JWT; Supabase guarda el plazo pendiente para la limpieza de 72 horas.
export function crearCorreoMovil({
  archivo,
  usuarios = Usuario,
  enviar,
  secreto,
  ahora = () => Date.now(),
  codigo = generarCodigo,
}) {
  if (!secreto) throw new INTERNAL("Falta el secreto del servidor.");
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
  const hash = hmac(secreto);
  // Serializa emisiones y consumos para impedir reutilización simultánea de códigos.
  let cola = Promise.resolve();
  const serializar = (operacion) => {
    const resultado = cola.then(operacion);
    cola = resultado.catch(() => {});
    return resultado;
  };
  const invalido = codigoInvalido;
  async function emitir(id, correo, tipo) {
    const cuenta = estado.cuentas[id];
    if (cuenta[tipo] && ahora() - cuenta[tipo].enviado < MINUTO)
      throw new RATE_LIMITED("Espera un minuto antes de solicitar otro código.");
    const valor = codigo();
    // Reenviar permite cambiar el código, pero nunca prolongar las 72 horas del registro.
    if (tipo === "confirmar") {
      cuenta.limiteConfirmacion ??= ahora() + 72 * 60 * MINUTO;
      if (cuenta.limiteConfirmacion <= ahora()) invalido();
    }
    cuenta[tipo] = {
      hash: hash(id, tipo, valor),
      enviado: ahora(),
      vence: tipo === "confirmar" ? cuenta.limiteConfirmacion : ahora() + 15 * MINUTO,
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
          if (usuario.correo_pendiente_desde) {
            cuenta.confirmado = false;
            cuenta.limiteConfirmacion =
              Date.parse(usuario.correo_pendiente_desde) + 72 * 60 * MINUTO;
          }
          if (
            (tipo === "confirmar" && !cuenta.confirmado) ||
            (tipo === "recuperar" && cuenta.confirmado)
          ) {
            try {
              await emitir(id, correo, tipo);
            } catch (error) {
              // El tiempo de espera tampoco revela si el correo pertenece a una cuenta.
              if (error instanceof BAD_REQUEST || error instanceof RATE_LIMITED) return GENERICO;
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
          validarContrasena(contrasena, confirmContrasena);
          await usuarios.updatePassword(id, await bcrypt.hash(contrasena, 10));
          // No consume el código ni confirma éxito si la contraseña no quedó guardada.
          const actualizado = await usuarios.fetchByEmail(correo);
          if (
            actualizado?.id_usuario !== id ||
            !actualizado.contrasena ||
            !(await bcrypt.compare(contrasena, actualizado.contrasena))
          )
            throw new SERVICE_UNAVAILABLE(
              "No se pudo comprobar el cambio de contraseña. Inténtalo de nuevo.",
              ERROR_CODES.PASSWORD_UPDATE_FAILED,
            );
          cuenta.version += 1;
        } else {
          // No consume el código si Supabase falla o si la cuenta ya venció o fue eliminada.
          if (!(await usuarios.confirmarCorreo(id))) invalido();
          cuenta.confirmado = true;
        }
        delete cuenta[tipo];
        guardar();
        return { ok: true };
      });
    },
  };
}

// Modo supabase (producción/Vercel): el estado vive en public.correo_movil_estado y las RPC
// validan, cuentan intentos y consumen el código en una sola transacción. Por eso no hace falta
// la cola en memoria del modo local: dos instancias distintas no pueden reutilizar un código.
export function crearCorreoMovilSupabase({
  usuarios = Usuario,
  enviar,
  secreto,
  codigo = generarCodigo,
}) {
  if (!secreto) throw new INTERNAL("Falta el secreto del servidor.");
  const hash = hmac(secreto);

  async function emitir(id, correo, tipo) {
    const valor = codigo();
    const resultado = await usuarios.emitirCodigoCorreoMovil(id, tipo, hash(id, tipo, valor));
    if (resultado === "rate_limited")
      throw new RATE_LIMITED("Espera un minuto antes de solicitar otro código.");
    if (resultado === "expired" || resultado === "not_eligible") codigoInvalido();
    if (resultado !== "sent")
      throw new SERVICE_UNAVAILABLE(
        "No se pudo preparar el código de correo.",
        ERROR_CODES.EMAIL_UNAVAILABLE,
      );
    await enviar({ para: correo, codigo: valor, tipo });
  }

  return {
    estado: (id) => usuarios.obtenerEstadoCorreoMovil(id),
    async registrar(id, correo) {
      try {
        await emitir(id, correo, "confirmar");
        return { enviado: true };
      } catch {
        console.error(
          "No se entregó la confirmación móvil; la cuenta puede solicitar otro código.",
        );
        return { enviado: false };
      }
    },
    async solicitar(correo, tipo) {
      if (!TIPOS.has(tipo)) return GENERICO;
      const usuario = await usuarios.fetchByEmail(correo);
      if (usuario?.rol !== ROLES.CLIENT) return GENERICO;
      const pendiente = Boolean(usuario.correo_pendiente_desde);
      if ((tipo === "confirmar") !== pendiente) return GENERICO;
      try {
        await emitir(usuario.id_usuario, correo, tipo);
      } catch (error) {
        // El tiempo de espera tampoco revela si el correo pertenece a una cuenta.
        if (!(error instanceof BAD_REQUEST || error instanceof RATE_LIMITED))
          console.error("No se entregó el código de correo móvil.");
      }
      return GENERICO;
    },
    async consumir(correo, valor, tipo, contrasena, confirmContrasena) {
      if (!TIPOS.has(tipo) || typeof valor !== "string" || !/^\d{6}$/.test(valor)) codigoInvalido();
      const usuario = await usuarios.fetchByEmail(correo);
      if (usuario?.rol !== ROLES.CLIENT) codigoInvalido();
      let contrasenaHash = null;
      if (tipo === "recuperar") {
        validarContrasena(contrasena, confirmContrasena);
        contrasenaHash = await bcrypt.hash(contrasena, 10);
      }
      const resultado = await usuarios.consumirCodigoCorreoMovil(
        usuario.id_usuario,
        tipo,
        hash(usuario.id_usuario, tipo, valor),
        contrasenaHash,
      );
      if (resultado !== "ok") codigoInvalido();
      return { ok: true };
    },
  };
}

let servicioLocal;
export function servicioCorreoMovil() {
  if (!correoMovilHabilitado())
    throw new SERVICE_UNAVAILABLE(
      "El correo móvil todavía no está habilitado.",
      ERROR_CODES.EMAIL_UNAVAILABLE,
    );
  if (modoCorreoMovil() === "supabase")
    // Sin estado en memoria: cada instancia de Vercel consulta la misma base.
    return crearCorreoMovilSupabase({
      secreto: process.env.JWT_SECRET,
      enviar: enviarCodigoCorreo,
    });
  if (process.env.NODE_ENV === "production")
    throw new SERVICE_UNAVAILABLE(
      "El almacenamiento local de correo no está habilitado en producción.",
      ERROR_CODES.EMAIL_UNAVAILABLE,
    );
  servicioLocal ??= crearCorreoMovil({
    archivo: resolve(process.env.CORREO_MOVIL_ARCHIVO || ".local/correo-movil.json"),
    secreto: process.env.JWT_SECRET,
    enviar: enviarCodigoCorreo,
  });
  return servicioLocal;
}

// El código nunca se devuelve en respuestas HTTP; sólo viaja en el correo.
export function enviarCodigoCorreo({ para, codigo, tipo }) {
  const confirmar = tipo === "confirmar";
  return enviarCorreo({
    para,
    asunto: confirmar ? "Confirma tu cuenta de Qariño Animal" : "Recupera tu contraseña",
    texto: `Tu código es: ${codigo}\n\nEscríbelo en la aplicación Qariño Animal. ${confirmar ? "Confirma tu cuenta dentro de las 72 horas posteriores al registro." : "Vence en 15 minutos."}\n\nSi no lo solicitaste, ignora este correo.`,
  });
}

// Asíncrona porque en modo supabase el estado se consulta en la base.
export async function verificarCorreoMovil(usuario) {
  if (!correoMovilHabilitado() || usuario.rol !== ROLES.CLIENT) return true;
  const estado = await servicioCorreoMovil().estado(usuario.uuid);
  return estado.confirmado && estado.version === (usuario.correoVersionMovil ?? 0);
}
