export function validateRegistration(body = {}, today = new Date()) {
  body ??= {};
  const value = {
    nombre: typeof body.nombre === "string" ? body.nombre : null,
    apellidos: typeof body.apellidos === "string" ? body.apellidos : null,
    fecha_nacimiento: typeof body.fecha_nacimiento === "string" ? body.fecha_nacimiento : null,
    contrasena: typeof body.contrasena === "string" ? body.contrasena : null,
    correo: typeof body.correo === "string" ? body.correo : null,
    telefono: typeof body.telefono === "string" ? body.telefono : null,
    id_codigo_postal: typeof body.id_codigo_postal === "string" ? body.id_codigo_postal : null,
    calle: typeof body.calle === "string" ? body.calle : null,
    numero_exterior: typeof body.numero_exterior === "number" ? body.numero_exterior : null,
    numero_interior: typeof body.numero_exterior === "number" ? body.numero_exterior : null,
    privacidad: typeof body.privacidad === "boolean" ? body.privacidad : null,
  };

  const errors = {};
  for (const [field, label, max] of [
    ["nombre", "tu nombre", 100],
    ["apellidos", "tus apellidos", 150],
    ["calle", "tu dirección", 250],
  ]) {
    if (!value[field] || value[field].length > max) {
      errors[field] = `Ingresa ${label} (máximo ${max} caracteres).`;
    }
  }

  if (
    value.correo.length > 254 ||
    !/^[^\s@]+@(gmail\.com|outlook\.com|yahoo\.com|hotmail\.com|icloud\.com)$/.test(value.correo)
  ) {
    errors.correo = "Utiliza Gmail, Outlook, Yahoo, Hotmail o iCloud.";
  }

  // Sin espacios ni +, 10 o 12 caracteres
  const telefono = value.telefono.trim().replace(/^\+/, "");

  if (!/^(\d{10}|\d{12})$/.test(telefono)) {
    errors.telefono = "Ingresa un teléfono válido.";
  }

  const birth = new Date(`${value.fecha_nacimiento}T00:00:00Z`);

  const validDate =
    /^\d{4}-\d{2}-\d{2}$/.test(value.fecha_nacimiento) &&
    !Number.isNaN(birth.valueOf()) &&
    birth.getUTCFullYear() > 0 &&
    birth.toISOString().slice(0, 10) === value.fecha_nacimiento;

  const birthdayPending = value.fecha_nacimiento.slice(5) > today.toISOString().slice(5, 10);

  const age = today.getUTCFullYear() - birth.getUTCFullYear() - Number(birthdayPending);

  if (!validDate) errors.fecha_nacimiento = "Ingresa una fecha de nacimiento válida.";
  else if (age < 18) errors.fecha_nacimiento = "Debes ser mayor de edad para crear una cuenta.";

  if (!/^\d{1,9}$/.test(value.numero_exterior)) {
    errors.numero_exterior = "Ingresa el número exterior";
  }

  // Números o nada
  if (value.numero_interior && !/^\d{1,9}$/.test(value.numero_interior)) {
    errors.numero_interior = "Ingresa el número interior";
  }

  /*
  if (!/^\d{5}$/.test(value.codigoPostal))
    errors.codigoPostal = "Ingresa un código postal de 5 dígitos.";
  */

  if (value.contrasena.length < 8)
    errors.contrasena = "Tu contraseña debe contener al menos 8 caracteres";
  else if (new TextEncoder().encode(value.contrasena).length > 72) {
    errors.contrasena = "Tu contraseña debe ser menor a 72 caracteres";
  }

  if (!body.confirmPasswd || body.confirmPasswd !== value.contrasena) {
    errors.confirmPasswd = "Las contraseñas no coinciden";
  }

  if (!value.privacidad) errors.privacidad = "Acepta la política de privacidad";
  return { value, errors };
}

/*
Se requiere obtener lista de códigos postales, mostrarlos en un
desplegable y a partir de eso mandar la id de ese código postal en
el body.

Ello requiere un nuevo endpoint, nuevo modelo y correcciones en
interfaz.
*/
