import { expect, test } from "vite-plus/test";
import { INTERNAL } from "@qarino/shared/errores.js";
import { mapearAgenda } from "./agenda.mapper.js";
import { mapearPendiente } from "./pendiente.mapper.js";
import { mapearPerroDestacado } from "./perroDestacado.mapper.js";
import { mapearResumen } from "./resumen.mapper.js";

test("el resumen ignora voluntariado y acepta una fecha nula", () => {
  const resumen = mapearResumen({
    data: {
      solicitudes: { porRevisar: 0, enRevision: 0, masAntiguaDesde: null },
      donativos: { porValidar: 0, montoPorValidar: 0, masAntiguoDesde: null },
      facturas: { pendientes: 0, requierenRevision: 0, correosConError: 0 },
      perros: { disponibles: 1, ingresosDelMes: 0 },
      voluntariado: { inscritosUltimos7Dias: 8 },
    },
    meta: { generadoEn: "2026-11-26T14:15:00-06:00" },
  });

  expect(resumen.solicitudes.masAntiguaDesde).toBeNull();
  expect(resumen.perros.disponibles).toBe(1);
  expect(resumen).not.toHaveProperty("voluntariado");
});

test("un pendiente recorta char, junta el nombre y conserva el perro nulo", () => {
  const tarea = mapearPendiente({
    id: " a1111111-1111-4111-8111-111111111111 ",
    tipo: "adopcion",
    creadaEn: "2026-11-25T15:04:00-06:00",
    folio: " QA-A1111111 ",
    persona: { nombre: "Pedro ", apellidos: " Nava" },
    perro: null,
    monto: null,
  });

  expect(tarea.id).toBe("a1111111-1111-4111-8111-111111111111");
  expect(tarea.folio).toBe("QA-A1111111");
  expect(tarea.persona.nombre).toBe("Pedro Nava");
  expect(tarea.perro).toBeNull();
});

test("una fecha imposible y un tipo ajeno no se convierten en entidad", () => {
  expect(() =>
    mapearPendiente({ id: "x", tipo: "otro", creadaEn: "ayer", persona: { nombre: "Ana" } }),
  ).toThrow(INTERNAL);
  expect(() =>
    mapearPendiente({ id: "x", tipo: "adopcion", creadaEn: "ayer", persona: { nombre: "Ana" } }),
  ).toThrow(INTERNAL);
});

test("la agenda lee la fecha del calendario y el perro vacío es null", () => {
  const agenda = mapearAgenda({
    success: true,
    data: {
      programas: [
        {
          idPrograma: "p8888888-8888-4888-8888-888888888888",
          nombre: "Paseo",
          tipo: null,
          inicio: "2026-11-28 09:00:00",
          fin: "2026-11-28T12:00:00-06:00",
          ubicacion: null,
          cupoMaximo: null,
          inscritos: 0,
          estado: "Disponible",
        },
      ],
      semanasSinCobertura: [{ inicio: "2026-11-30", fin: "2026-12-07" }],
    },
  });

  expect(agenda.jornadas[0].inicio).toEqual(new Date("2026-11-28T09:00:00-06:00"));
  expect(agenda.jornadas[0].cupoMaximo).toBeNull();
  expect(mapearPerroDestacado({ data: null })).toBeNull();
});
