const emailDomains = new Set([
  "gmail.com",
  "outlook.com",
  "yahoo.com",
  "hotmail.com",
  "icloud.com",
]);
const text = (value) => (typeof value === "string" ? value.trim() : "");

export function validateRegistration(body = {}, today = new Date()) {
  body ??= {};
  const value = {
    nombre: text(body.nombre),
    apellidos: text(body.apellidos),
    email: text(body.email).toLowerCase(),
    telefono: text(body.telefono),
    fechaNacimiento: text(body.fechaNacimiento),
    calle: text(body.calle),
    numeroExterior: text(body.numeroExterior),
    codigoPostal: text(body.codigoPostal),
    passwd: typeof body.passwd === "string" ? body.passwd : "",
    privacidad: body.privacidad === true,
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
  if (value.email.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value.email)) {
    errors.email = "Ingresa un correo válido.";
  } else if (!emailDomains.has(value.email.split("@")[1])) {
    errors.email = "Utiliza Gmail, Outlook, Yahoo, Hotmail o iCloud.";
  }
  const digits = value.telefono.replace(/\D/g, "");
  if (!/^\+?[\d ()-]+$/.test(value.telefono) || digits.length < 7 || digits.length > 15) {
    errors.telefono = "Ingresa un teléfono de entre 7 y 15 dígitos.";
  }
  const birth = new Date(`${value.fechaNacimiento}T00:00:00Z`);
  const validDate =
    /^\d{4}-\d{2}-\d{2}$/.test(value.fechaNacimiento) &&
    !Number.isNaN(birth.valueOf()) &&
    birth.getUTCFullYear() > 0 &&
    birth.toISOString().slice(0, 10) === value.fechaNacimiento;
  const birthdayPending = value.fechaNacimiento.slice(5) > today.toISOString().slice(5, 10);
  const age = today.getUTCFullYear() - birth.getUTCFullYear() - Number(birthdayPending);
  if (!validDate) errors.fechaNacimiento = "Ingresa una fecha de nacimiento válida.";
  else if (age < 18) errors.fechaNacimiento = "Debes ser mayor de edad para crear una cuenta.";
  if (!/^\d{1,9}$/.test(value.numeroExterior)) {
    errors.numeroExterior = "Ingresa el número exterior, únicamente con números.";
  }
  if (!/^\d{5}$/.test(value.codigoPostal))
    errors.codigoPostal = "Ingresa un código postal de 5 dígitos.";
  if (value.passwd.length < 8) errors.passwd = "Usa al menos 8 caracteres.";
  else if (new TextEncoder().encode(value.passwd).length > 72) {
    errors.passwd = "La contraseña es demasiado larga; utiliza menos caracteres.";
  }
  if (!body.confirmPasswd || body.confirmPasswd !== value.passwd) {
    errors.confirmPasswd = "Las contraseñas deben coincidir.";
  }
  if (!value.privacidad) errors.privacidad = "Acepta el uso de tus datos para crear tu cuenta.";
  return { value, errors };
}
