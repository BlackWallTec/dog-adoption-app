import { expect, test } from "vite-plus/test";
import { validateRegistration } from "../../shared/perfiles/registro.validation.js";
import { registration } from "./helpers/registration-data.js";

const today = new Date("2026-11-05T12:00:00Z");
test("normaliza datos pero conserva exactamente la contrasena", () => {
  const { errors, value } = validateRegistration(
    {
      ...registration,
      email: " ANA@GMAIL.COM ",
      nombre: " Ana ",
      passwd: " con espacios ",
      confirmPasswd: " con espacios ",
    },
    today,
  );
  expect(errors).toEqual({});
  expect(value.email).toBe("ana@gmail.com");
  expect(value.nombre).toBe("Ana");
  expect(value.passwd).toBe(" con espacios ");
});
test.each([
  ["2008-11-05", true],
  ["2008-11-06", false],
  ["2026-11-06", false],
  ["2000-02-30", false],
  ["2000-02-29", true],
  ["", false],
  ["0000-01-01", false],
])("mayoria de edad y fechas reales: %s", (birth, valid) => {
  const { errors } = validateRegistration({ ...registration, fechaNacimiento: birth }, today);
  expect(!errors.fechaNacimiento).toBe(valid);
});
test("un nacimiento bisiesto no cumple anos un dia antes", () => {
  const body = { ...registration, fechaNacimiento: "2008-02-29" };
  expect(validateRegistration(body, new Date("2026-02-28Z")).errors.fechaNacimiento).toBeTruthy();
  expect(
    validateRegistration(body, new Date("2026-03-01Z")).errors.fechaNacimiento,
  ).toBeUndefined();
});
test.each([
  [{ telefono: "123" }, "telefono"],
  [{ numeroExterior: "-1" }, "numeroExterior"],
  [{ codigoPostal: "6000" }, "codigoPostal"],
  [{ calle: "" }, "calle"],
  [{ email: "ana@dominio.test" }, "email"],
  [{ privacidad: "true" }, "privacidad"],
  [{ passwd: "á".repeat(37) }, "passwd"],
])("valida los datos del registro: %j", (change, field) => {
  expect(validateRegistration({ ...registration, ...change }, today).errors[field]).toBeTruthy();
});
