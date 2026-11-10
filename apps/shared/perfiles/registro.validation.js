import { VALIDATION } from "../errores.js";

export function limpiarTelefono(telefono) {
  return telefono.trim().replace(/^\+/, "");
}

const nada = (val) => {
  return val;
};

const desc = (min, max, args = "") => {
  let texto = "se espera un valor con lo siguiente:";
  let rango;

  if (min && max) {
    if (max < min) throw "máximo no puede ser menor al mínimo";
    if (min === max) rango = `se esperan exactamente ${min}`;
    else rango = `entre ${min} y ${max}`;
  } else if (!min && !max) {
    rango = "cualquier cantidad de";
  } else if (!min) {
    rango = `cáximo ${max}`;
  } else if (!max) {
    rango = `mínimo ${min}`;
  }

  if (rango) rango = rango + " caracteres\n";

  if (args) args = `y ${args}`;

  texto = `${texto}\n${rango}\n${args}`;
  return texto;
};

const patrones = {
  nombre: /^[\p{L}\p{M} '-]{1,100}$/u,
  apellidos: /^[\p{L}\p{M} '-]{1,150}$/u,
  calle: /^[\p{L}\p{M}\p{N} .,'/#-]{1,250}$/u,
  correo: /^(?=.{1,254}$)[^\s@]+@(gmail\.com|outlook\.com|yahoo\.com|hotmail\.com|icloud\.com)$/i,
  contrasena: /^.{8,72}$/,
  telefono: /^(\d{10}|\d{12})$/,
  numero_exterior: /^\d{1,9}$/,
  numero_interior: /^\s*\d{0,9}\s*$/,
  cp: /^\d{5}$/,
  date: /^\d{4}-\d{2}-\d{2}$/,
};

export function validateRegistration(body = {}, today = new Date()) {
  body ??= {};

  const errors = {};

  const value = {
    numero_exterior: typeof body.numero_exterior === "string" ? body.numero_exterior : null,
    numero_interior: typeof body.numero_interior === "string" ? body.numero_interior : null,
    nombre: typeof body.nombre === "string" ? body.nombre : null,
    apellidos: typeof body.apellidos === "string" ? body.apellidos : null,
    fecha_nacimiento: typeof body.fecha_nacimiento === "string" ? body.fecha_nacimiento : null,
    contrasena: typeof body.contrasena === "string" ? body.contrasena : null,
    correo: typeof body.correo === "string" ? body.correo : null,
    telefono: typeof body.telefono === "string" ? body.telefono : null,
    id_codigo_postal: typeof body.id_codigo_postal === "string" ? body.id_codigo_postal : null,
    calle: typeof body.calle === "string" ? body.calle : null,
    privacidad: typeof body.privacidad === "boolean" ? body.privacidad : null,
  };

  for (const [field, descript, fun] of [
    ["nombre", desc(1, 100), nada],
    ["apellidos", desc(1, 150), nada],
    ["correo", desc(1, 254, "sólo Gmail, Outlook, Yahoo, Hotmail o iCloud"), nada],
    ["telefono", desc(10, 12), limpiarTelefono],
    ["calle", desc(1, 250), nada],
    ["numero_exterior", desc(1, 9), nada],
    ["numero_interior", desc(1, 9), nada],
    ["contrasena", desc(8, 72), nada],
  ]) {
    const val = fun(value[field]);

    // Si no coincide con el patrón
    if (!patrones[field].test(val)) errors[field] = `Campo inválido,\n${String(descript)}\n`;
  }

  if (value.fecha_nacimiento) {
    const birth = new Date(`${value.fecha_nacimiento}T00:00:00Z`);

    const validDate =
      patrones.date.test(value.fecha_nacimiento) &&
      !Number.isNaN(birth.valueOf()) &&
      birth.getUTCFullYear() > 0 &&
      birth.toISOString().slice(0, 10) === value.fecha_nacimiento;

    const birthdayPending = value.fecha_nacimiento.slice(5) > today.toISOString().slice(5, 10);

    const age = today.getUTCFullYear() - birth.getUTCFullYear() - Number(birthdayPending);

    if (!validDate) errors.fecha_nacimiento = "Fecha de nacimiento inválida";
    else if (age < 18) errors.fecha_nacimiento = "No tienes la mayoría de edad";
  } else {
    errors.fecha_nacimiento = "Ingresa tu fecha de nacimiento";
  }

  if (!body.confirmContrasena || body.confirmContrasena !== value.contrasena) {
    errors.confirmContrasena = "Las contraseñas no coinciden;";
  }

  if (!value.privacidad) errors.privacidad = "Acepta la política de privacidad";

  if (Object.keys(errors).length > 0) {
    throw new VALIDATION("Error de validación en los datos", errors);
  }
}
