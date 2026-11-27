import { expect, test } from "vite-plus/test";
import { ordenarPendientes, prepararPendientes, urgenciaDe } from "./prioridad.js";

const ahora = new Date("2026-11-26T12:00:00-06:00");
const MS_POR_DIA = 24 * 60 * 60 * 1000;

/** @param {number} dias @param {number} [msExtra] */
function hace(dias, msExtra = 0) {
  return new Date(ahora.getTime() - dias * MS_POR_DIA - msExtra);
}

/** @param {Record<string, unknown>} parcial */
function tarea(parcial) {
  return {
    tipo: "adopcion",
    creadaEn: ahora,
    requiereRevision: false,
    correoEstado: null,
    ...parcial,
  };
}

test("el correo con error y el CFDI por revisar son críticos aunque sean recientes", () => {
  expect(urgenciaDe(tarea({ tipo: "correo_factura", creadaEn: ahora }), ahora)).toBe("critica");
  expect(
    urgenciaDe(tarea({ tipo: "factura", requiereRevision: true, creadaEn: ahora }), ahora),
  ).toBe("critica");
  expect(
    urgenciaDe(tarea({ tipo: "factura", correoEstado: "Error", creadaEn: ahora }), ahora),
  ).toBe("critica");
});

test("la antigüedad marca atrasada solo al pasar el SLA del tipo", () => {
  expect(urgenciaDe(tarea({ creadaEn: hace(3) }), ahora)).toBe("normal");
  expect(urgenciaDe(tarea({ creadaEn: hace(3, 1) }), ahora)).toBe("atrasada");
  expect(urgenciaDe(tarea({ tipo: "donativo", creadaEn: hace(2, 1) }), ahora)).toBe("atrasada");
  expect(
    urgenciaDe(tarea({ creadaEn: hace(1) }), ahora, {
      adopcion: 0,
      donativo: 2,
      factura: 3,
      correo_factura: 3,
    }),
  ).toBe("atrasada");
});

test("lo crítico va primero, después lo más viejo, y el empate lo gana la adopción", () => {
  const correo = tarea({ id: "correo", tipo: "correo_factura", creadaEn: hace(1) });
  const adopcionVieja = tarea({ id: "adopcion-vieja", creadaEn: hace(10) });
  const donativoViejo = tarea({ id: "donativo", tipo: "donativo", creadaEn: hace(10) });
  const adopcion = tarea({ id: "adopcion", creadaEn: hace(4) });
  const lista = [adopcion, donativoViejo, correo, adopcionVieja];

  expect(ordenarPendientes(lista, ahora).map((item) => item.id)).toEqual([
    "correo",
    "adopcion-vieja",
    "donativo",
    "adopcion",
  ]);
  expect(lista.map((item) => item.id)).toEqual([
    "adopcion",
    "donativo",
    "correo",
    "adopcion-vieja",
  ]);
});

test("un peso distinto cambia el empate y prepararPendientes escribe la urgencia", () => {
  const adopcion = tarea({ id: "adopcion", creadaEn: hace(4) });
  const donativo = tarea({ id: "donativo", tipo: "donativo", creadaEn: hace(4) });

  expect(
    ordenarPendientes([adopcion, donativo], ahora, { pesos: { adopcion: 2, donativo: 0 } }).map(
      (item) => item.id,
    ),
  ).toEqual(["donativo", "adopcion"]);

  const [primera] = prepararPendientes([tarea({ tipo: "correo_factura", creadaEn: ahora })], ahora);
  expect(primera.urgencia).toBe("critica");
});
