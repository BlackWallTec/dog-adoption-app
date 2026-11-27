import { expect, test } from "vite-plus/test";
import { FORBIDDEN, UNAUTHENTICATED } from "@qarino/shared/errores.js";
import { crearInicio, debeRefrescar, elegirFuenteInicio } from "./inicio.js";
import { COPY } from "./inicio.presentar.js";

const AHORA = new Date("2026-11-26T14:15:00-06:00");

/** @param {Record<string, unknown>} [parcial] */
function tarea(parcial = {}) {
  return {
    id: "a1111111-1111-4111-8111-111111111111",
    tipo: "adopcion",
    persona: { nombre: "Sofía García" },
    perro: { id: "b6666666-6666-4666-8666-666666666666", nombre: "Bruno" },
    folio: "QA-A1111111",
    monto: null,
    creadaEn: new Date("2026-11-20T14:15:00-06:00"),
    urgencia: "atrasada",
    ...parcial,
  };
}

function resumenLleno() {
  return {
    solicitudes: {
      porRevisar: 1,
      enRevision: 1,
      masAntiguaDesde: new Date("2026-11-20T14:15:00-06:00"),
    },
    donativos: {
      porValidar: 1,
      montoPorValidar: 1500,
      masAntiguoDesde: new Date("2026-11-23T14:15:00-06:00"),
    },
    facturas: { pendientes: 2, requierenRevision: 1, correosConError: 1 },
    perros: { disponibles: 18, ingresosDelMes: 3 },
    generadoEn: AHORA,
  };
}

function resumenVacio() {
  return {
    solicitudes: { porRevisar: 0, enRevision: 0, masAntiguaDesde: null },
    donativos: { porValidar: 0, montoPorValidar: 0, masAntiguoDesde: null },
    facturas: { pendientes: 0, requierenRevision: 0, correosConError: 0 },
    perros: { disponibles: 0, ingresosDelMes: 0 },
    generadoEn: AHORA,
  };
}

/** @param {Date} inicio @param {string} [estado] */
function jornada(inicio, estado = "Disponible") {
  return {
    id: `p-${inicio.getTime()}`,
    nombre: "Paseo",
    tipo: "Paseo",
    inicio,
    fin: new Date(inicio.getTime() + 60 * 60 * 1000),
    ubicacion: "Parque",
    cupoMaximo: 10,
    inscritos: 4,
    estado,
  };
}

/**
 * @param {Record<string, Function>} metodos
 * @param {{ redirigir?: (ruta: string) => void }} [extra]
 */
function crear(metodos, extra = {}) {
  const repositorio = {
    obtenerResumen: async () => resumenVacio(),
    obtenerPendientes: async () => [],
    obtenerAgenda: async () => ({ jornadas: [], semanasSinCobertura: [] }),
    obtenerPerroDestacado: async () => null,
    ...metodos,
  };
  return crearInicio({
    repositorio,
    obtenerUsuario:
      extra.obtenerUsuario ??
      (async () => ({ nombre: "Ana", apellidos: "Martínez", rol: "Administrador" })),
    ahora: () => AHORA,
    redirigir: extra.redirigir ?? (() => {}),
    enConstruccion: extra.enConstruccion ?? false,
  });
}

test("iniciar pasa de carga a listo y publica textos, tono y enlaces", async () => {
  let resolver = () => {};
  const resumen = new Promise((ok) => {
    resolver = ok;
  });
  const vm = crear({
    obtenerResumen: () => resumen,
    obtenerPendientes: async () => [
      tarea(),
      tarea({
        id: "d3333333-3333-4333-8333-333333333333",
        tipo: "donativo",
        persona: { nombre: "Mariana Ruiz" },
        perro: null,
        folio: null,
        monto: 1500,
        urgencia: "normal",
      }),
    ],
    obtenerAgenda: async () => ({
      jornadas: [
        jornada(new Date("2026-11-25T09:00:00-06:00"), "Finalizado"),
        jornada(new Date("2026-11-28T09:00:00-06:00")),
        jornada(new Date("2026-11-29T09:00:00-06:00")),
        jornada(new Date("2026-11-30T09:00:00-06:00")),
        jornada(new Date("2026-12-01T09:00:00-06:00")),
      ],
      semanasSinCobertura: [{ inicio: "2026-11-30", fin: "2026-12-07" }],
    }),
    obtenerPerroDestacado: async () => ({
      id: "e7777777-7777-4777-8777-777777777777",
      nombre: "Canela",
      sexo: "Hembra",
      edadAprox: 4,
      tamano: "Mediano",
      fotoUrl: null,
      fechaIngreso: new Date("2026-04-26T00:00:00-06:00"),
      diasEnRefugio: 214,
      solicitudesActivas: 0,
    }),
  });

  const carga = vm.iniciar();
  expect(vm.estado().resumen.estado).toBe("cargando");
  expect(vm.estado().usuario.estado).toBe("cargando");
  resolver(resumenLleno());
  await carga;

  const listo = vm.estado();
  expect(listo.usuario.datos?.saludo).toBe("¡Buenas tardes, Ana!");
  expect(listo.usuario.datos?.acciones[1].href).toBe("/admin/voluntariado/nueva");
  expect(listo.resumen.estado).toBe("listo");
  expect(listo.resumen.datos?.map((kpi) => kpi.tono)).toEqual([
    "warning",
    "warning",
    "danger",
    "neutral",
  ]);
  expect(listo.resumen.datos?.[0].href).toBe("/admin/solicitudes?estado=pendientes");
  expect(listo.resumen.datos?.[0].antiguedad).toBe("Más antigua: hace 6 d");
  expect(listo.resumen.datos?.[0].detalle).toBe("1 nueva · 1 en revisión");
  expect(listo.resumen.datos?.[3].href).toBe("/admin/perros?estado=disponible");
  expect(listo.pendientes.datos[0].titulo).toBe("Revisar solicitud de Sofía García");
  expect(listo.pendientes.datos[0].meta).toContain("hace 6 días");
  expect(listo.pendientes.datos[0].href).toBe(
    "/admin/solicitudes?abrir=a1111111-1111-4111-8111-111111111111",
  );
  expect(listo.agenda.datos).toHaveLength(3);
  expect(
    listo.agenda.datos.map((jornada) => jornada.nombre).every((nombre) => nombre === "Paseo"),
  ).toBe(true);
  expect(listo.agenda.datos.every((item) => item.href === "/admin/voluntariado/calendario")).toBe(
    true,
  );
  expect(listo.agenda.alertaCobertura?.href).toBe("/admin/voluntariado/nueva?fecha=2026-11-30");
  expect(listo.destacado.estado).toBe("listo");
  expect(listo.destacado.datos?.href).toBe(
    "/admin/perros?abrir=e7777777-7777-4777-8777-777777777777",
  );
  expect(listo.conteos).toEqual({ solicitudes: 2, donativos: 3 });
});

test("vacio deja la bandeja y la agenda vacías y oculta el destacado", async () => {
  const vm = crear({});
  await vm.iniciar();
  const estado = vm.estado();
  expect(estado.resumen.datos?.[0]).toMatchObject({
    valor: "0",
    detalle: COPY.todoAlDia,
    tono: "neutral",
  });
  expect(estado.pendientes.estado).toBe("vacio");
  expect(estado.pendientes.textoVacio).toBe(COPY.bandejaVacia);
  expect(estado.agenda.estado).toBe("vacio");
  expect(estado.agenda.textoVacio).toBe(COPY.agendaVacia);
  expect(estado.destacado.estado).toBe("oculto");
});

test("el error de un widget no apaga los demás", async () => {
  const vm = crear({
    obtenerResumen: async () => {
      throw new Error("falló");
    },
    obtenerPendientes: async () => [tarea()],
  });
  await vm.iniciar();
  expect(vm.estado().resumen.estado).toBe("error");
  expect(vm.estado().resumen.error?.texto).toBe(COPY.errores.resumen);
  expect(vm.estado().pendientes.estado).toBe("listo");
  expect(vm.estado().usuario.estado).toBe("listo");
  expect(vm.estado().destacado.estado).toBe("oculto");
});

test("reintentar solo vuelve a consultar el widget que falló", async () => {
  let fallos = 0;
  let lecturasPendientes = 0;
  const vm = crear({
    obtenerResumen: async () => {
      fallos += 1;
      if (fallos === 1) throw new Error("falló");
      return resumenLleno();
    },
    obtenerPendientes: async () => {
      lecturasPendientes += 1;
      return [];
    },
  });
  await vm.iniciar();
  expect(vm.estado().resumen.estado).toBe("error");
  await vm.reintentar("resumen");
  expect(vm.estado().resumen.estado).toBe("listo");
  expect(lecturasPendientes).toBe(1);
});

test("filtrar la bandeja no vuelve a consultar", async () => {
  let lecturas = 0;
  const vm = crear({
    obtenerPendientes: async () => {
      lecturas += 1;
      return [
        tarea(),
        tarea({
          id: "d3333333-3333-4333-8333-333333333333",
          tipo: "donativo",
          persona: { nombre: "Mariana Ruiz" },
          perro: null,
          folio: null,
          monto: 400,
          urgencia: "normal",
        }),
        tarea({
          id: "c5555555-5555-4555-8555-555555555555",
          tipo: "correo_factura",
          persona: { nombre: "Luis Torres" },
          perro: null,
          folio: null,
          monto: 200,
          urgencia: "critica",
        }),
      ];
    },
  });
  await vm.iniciar();
  vm.filtrarPendientes("factura");
  expect(lecturas).toBe(1);
  expect(vm.estado().pendientes.datos.map((item) => item.tipo)).toEqual(["correo_factura"]);
  expect(vm.estado().pendientes.conteos).toEqual({
    todos: 3,
    adopcion: 1,
    donativo: 1,
    factura: 1,
  });
  vm.filtrarPendientes("adopcion");
  vm.filtrarPendientes("donativo");
  expect(vm.estado().pendientes.datos).toHaveLength(1);
  vm.filtrarPendientes("adopcion");
  vm.filtrarPendientes("todos");
  const sinDonativos = crear({
    obtenerPendientes: async () => [tarea()],
  });
  await sinDonativos.iniciar();
  sinDonativos.filtrarPendientes("donativo");
  expect(sinDonativos.estado().pendientes.textoVacio).toBe("No hay pendientes de Donativos.");
});

test("actualizar no regresa los widgets a carga", async () => {
  let resolver = () => {};
  let primera = true;
  const vm = crear({
    obtenerResumen: () => {
      if (primera) {
        primera = false;
        return Promise.resolve(resumenVacio());
      }
      return new Promise((ok) => {
        resolver = ok;
      });
    },
  });
  await vm.iniciar();
  const refresco = vm.actualizar();
  expect(vm.estado().refrescando).toBe(true);
  expect(vm.estado().resumen.estado).toBe("listo");
  resolver(resumenLleno());
  await refresco;
  expect(vm.estado().refrescando).toBe(false);
  expect(vm.estado().resumen.datos?.[0].valor).toBe("2");
});

test("fuera de desarrollo no publica el ejemplo ni los conteos", async () => {
  let consultas = 0;
  const vm = crear(
    {
      obtenerResumen: async () => {
        consultas += 1;
        return resumenLleno();
      },
      obtenerPendientes: async () => {
        consultas += 1;
        return [tarea()];
      },
    },
    { enConstruccion: true },
  );
  await vm.iniciar();
  expect(consultas).toBe(0);
  expect(vm.estado().enConstruccion).toBe(true);
  expect(vm.estado().conteos).toEqual({ solicitudes: 0, donativos: 0 });
  expect(vm.estado().pendientes.textoVacio).toBe(COPY.proximamente);
  expect(vm.estado().destacado.estado).toBe("oculto");
  expect(vm.estado().usuario.datos?.saludo).toBe("¡Buenas tardes, Ana!");
});

test("elegirFuenteInicio ignora el ejemplo fuera de desarrollo", async () => {
  const fuente = elegirFuenteInicio(new URLSearchParams("escenario=ok&fuente=mock"), {
    DEV: false,
  });
  expect(fuente.enConstruccion).toBe(true);
  await expect(fuente.repositorio.obtenerPendientes()).resolves.toEqual([]);
  await expect(fuente.repositorio.obtenerPerroDestacado()).resolves.toBeNull();
});

test("elegirFuenteInicio en desarrollo usa el ejemplo con pendientes", async () => {
  const fuente = elegirFuenteInicio(new URLSearchParams(), { DEV: true });
  expect(fuente.enConstruccion).toBe(false);
  await expect(fuente.repositorio.obtenerPendientes()).resolves.not.toEqual([]);
});

test("un 401 al leer la sesión redirige al login y no publica el ejemplo", async () => {
  const rutas = [];
  let consultas = 0;
  const vm = crear(
    {
      obtenerPendientes: async () => {
        consultas += 1;
        return [tarea()];
      },
    },
    {
      redirigir: (ruta) => rutas.push(ruta),
      obtenerUsuario: async () => {
        throw new UNAUTHENTICATED();
      },
    },
  );
  await vm.iniciar();
  expect(rutas).toEqual(["/admin/login"]);
  expect(consultas).toBe(0);
  expect(vm.estado().pendientes.datos).toEqual([]);
});

test("un Cliente no entra al panel y no se publica el ejemplo", async () => {
  const rutas = [];
  let consultas = 0;
  const vm = crear(
    {
      obtenerPendientes: async () => {
        consultas += 1;
        return [tarea()];
      },
    },
    {
      redirigir: (ruta) => rutas.push(ruta),
      obtenerUsuario: async () => {
        throw new FORBIDDEN("Tu cuenta no tiene acceso al panel administrativo.");
      },
    },
  );
  await vm.iniciar();
  expect(rutas).toEqual(["/admin/login?acceso=denegado"]);
  expect(consultas).toBe(0);
});

test("un 401 redirige al login una sola vez", async () => {
  const rutas = [];
  const vm = crear(
    {
      obtenerResumen: async () => {
        throw new UNAUTHENTICATED();
      },
      obtenerPendientes: async () => {
        throw new UNAUTHENTICATED();
      },
    },
    { redirigir: (ruta) => rutas.push(ruta) },
  );
  await vm.iniciar();
  expect(rutas).toEqual(["/admin/login"]);
  expect(vm.estado().usuario.estado).toBe("listo");
});

test("destruir ignora la respuesta que llega después", async () => {
  let resolver = () => {};
  const vm = crear({
    obtenerResumen: () =>
      new Promise((ok) => {
        resolver = ok;
      }),
  });
  const carga = vm.iniciar();
  vm.destruir();
  resolver(resumenLleno());
  await carga;
  expect(vm.estado().resumen.estado).toBe("cargando");
});

test("debeRefrescar espera cinco minutos desde la última lectura", () => {
  expect(debeRefrescar(null, AHORA)).toBe(false);
  expect(debeRefrescar(new Date(AHORA.getTime() - 4 * 60 * 1000), AHORA)).toBe(false);
  expect(debeRefrescar(new Date(AHORA.getTime() - 5 * 60 * 1000), AHORA)).toBe(true);
});
