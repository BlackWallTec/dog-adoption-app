/*
 * PRUEBAS DEL CONTROLADOR: POST /api/voluntarios/programas/:idPrograma/inscripciones
 *
 * Aquí viven las reglas definitivas: Android también valida, pero quien decide es el
 * servidor. Probamos con las mismas 4 técnicas que en Android:
 *   - RANGOS: agrupar los valores en familias y probar todas las familias.
 *   - NULOS/VACÍOS: ¿qué pasa si falta el dato?
 *   - VALORES LÍMITE: justo antes, justo en y justo después de cada frontera.
 *   - ESTRUCTURAL: que cada `if` tome sus dos caminos, y cada condición también.
 *
 * Para correrlas, desde la raíz del repositorio:
 *   vp test run apps/api/tests/voluntarios
 */
import { afterEach, beforeEach, describe, expect, it, vi } from "vite-plus/test";

/*
 * `vi.mock` reemplaza el archivo del modelo por funciones de mentira ("mocks").
 * Así las pruebas no tocan Supabase: son rápidas, no requieren internet y no ensucian
 * la base de datos. Además, cada prueba decide qué "contesta" la base de datos.
 */
vi.mock("../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  getProgramaInscripcionModel: vi.fn(),
  contarInscritosModel: vi.fn(),
  existeInscripcionModel: vi.fn(),
  crearInscripcionModel: vi.fn(),
}));

import { inscribirse } from "../../src/modules/voluntarios/http/voluntarios.controller.js";
import {
  contarInscritosModel,
  crearInscripcionModel,
  existeInscripcionModel,
  getProgramaInscripcionModel,
} from "../../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

const ID_PROGRAMA = "11111111-2222-3333-4444-555555555555";
const ID_USUARIO = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee";

// Hora fija: las 18:00 en UTC son las 12:00 en Ciudad de México.
const AHORA_UTC = "2026-11-05T18:00:00Z";
const AHORA_MEXICO = "2026-11-05T12:00:00";

// Un programa en el que SÍ se puede inscribir. Cada prueba cambia solo lo que le interesa.
const programaValido = (cambios = {}) => ({
  id_programa: ID_PROGRAMA,
  fecha_inicio: "2026-12-01T09:00:00",
  cupo_maximo: 10,
  estado_del_programa: "Disponible",
  ...cambios,
});

/*
 * Express le pasa al controlador los objetos `req` (petición) y `res` (respuesta).
 * Aquí ejecutamos el controlador con unos de mentira y regresamos lo que respondió.
 */
const ejecutar = async (idPrograma = ID_PROGRAMA) => {
  const res = {};
  res.status = vi.fn(() => res); // regresa `res` para permitir res.status().json()
  res.json = vi.fn();

  await inscribirse({ params: { idPrograma } }, res);

  return { status: res.status.mock.calls[0][0], cuerpo: res.json.mock.calls[0][0] };
};

const expectError = (respuesta, status, code) => {
  expect(respuesta.status).toBe(status);
  expect(respuesta.cuerpo.sucess).toBe(false);
  expect(respuesta.cuerpo.code).toBe(code);
};

const expectExito = (respuesta) => {
  expect(respuesta.status).toBe(201);
  expect(respuesta.cuerpo.sucess).toBe(true);
};

// `beforeEach` corre antes de CADA prueba: todas empiezan desde un mundo limpio y feliz.
beforeEach(() => {
  vi.clearAllMocks();

  // Detenemos solo el reloj (Date). Las promesas siguen funcionando con normalidad.
  vi.useFakeTimers({ toFake: ["Date"] });
  vi.setSystemTime(new Date(AHORA_UTC));

  vi.stubEnv("DEV_USUARIO_ID", ID_USUARIO);

  getProgramaInscripcionModel.mockResolvedValue(programaValido());
  existeInscripcionModel.mockResolvedValue(false);
  contarInscritosModel.mockResolvedValue(0);
  crearInscripcionModel.mockResolvedValue({
    id_programa: ID_PROGRAMA,
    fecha_inscripcion: AHORA_MEXICO,
    estado: "Activa",
  });
});

// `afterEach` deshace lo anterior para no afectar a otras pruebas.
afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllEnvs();
  vi.restoreAllMocks();
});

describe("cobertura de rangos", () => {
  // `it.each` repite una prueba con cada valor de la tabla. `$valor` o `%s` se sustituyen
  // en el nombre para saber cuál falló.
  it("cualquier número de inscritos por debajo del cupo permite inscribirse", async () => {
    getProgramaInscripcionModel.mockResolvedValue(programaValido({ cupo_maximo: 200 }));

    // Prueba de estrés: recorremos todo el rango en lugar de un solo valor.
    for (let inscritos = 0; inscritos < 200; inscritos++) {
      contarInscritosModel.mockResolvedValue(inscritos);
      expectExito(await ejecutar());
    }
  });

  it("cualquier número de inscritos igual o mayor al cupo da SIN_CUPO", async () => {
    getProgramaInscripcionModel.mockResolvedValue(programaValido({ cupo_maximo: 200 }));

    for (let inscritos = 200; inscritos <= 400; inscritos++) {
      contarInscritosModel.mockResolvedValue(inscritos);
      expectError(await ejecutar(), 409, "SIN_CUPO");
    }
  });

  it.each(["Cerrado", "No disponible", "Disponibles", "Dispon ible", "123"])(
    "el estado '%s' no está abierto a inscripciones",
    async (estado) => {
      getProgramaInscripcionModel.mockResolvedValue(
        programaValido({ estado_del_programa: estado }),
      );

      expectError(await ejecutar(), 409, "PROGRAMA_NO_DISPONIBLE");
    },
  );

  it.each(["2020-01-01T00:00:00", "2026-11-04T12:00:00", "2026-11-05T00:00:00"])(
    "una fecha de inicio pasada (%s) da PROGRAMA_INICIADO",
    async (fecha) => {
      getProgramaInscripcionModel.mockResolvedValue(programaValido({ fecha_inicio: fecha }));

      expectError(await ejecutar(), 409, "PROGRAMA_INICIADO");
    },
  );

  it.each(["2026-11-05T23:59:59", "2026-11-06T00:00:00", "2099-12-31T23:59:59"])(
    "una fecha de inicio futura (%s) permite inscribirse",
    async (fecha) => {
      getProgramaInscripcionModel.mockResolvedValue(programaValido({ fecha_inicio: fecha }));

      expectExito(await ejecutar());
    },
  );

  it.each([
    ID_PROGRAMA,
    "ABCDEFAB-CDEF-ABCD-EFAB-CDEFABCDEFAB", // mayúsculas
    "abcdefab-CDEF-abcd-EFAB-cdefABCDefab", // mezcla
    "00000000-0000-0000-0000-000000000000",
  ])("el UUID %s tiene formato válido", async (id) => {
    expectExito(await ejecutar(id));
  });

  it.each(["no-es-un-uuid", "123", "' OR 1=1 --", "../../etc/passwd"])(
    "el id '%s' se rechaza sin consultar la base de datos",
    async (id) => {
      expectError(await ejecutar(id), 404, "PROGRAMA_NO_ENCONTRADO");
      expect(getProgramaInscripcionModel).not.toHaveBeenCalled();
    },
  );
});

describe("cobertura de valores nulos y vacíos", () => {
  it.each([[undefined], [""]])("DEV_USUARIO_ID=%j da NO_AUTENTICADO", async (valor) => {
    vi.stubEnv("DEV_USUARIO_ID", valor);

    expectError(await ejecutar(), 401, "NO_AUTENTICADO");
    expect(getProgramaInscripcionModel).not.toHaveBeenCalled();
  });

  // Ojo: usamos `req` directo porque `ejecutar(undefined)` activaría el valor por defecto.
  it.each([[undefined], [null], [""], ["   "]])("idPrograma=%j da 404", async (id) => {
    const res = { status: vi.fn(() => res), json: vi.fn() };

    await inscribirse({ params: { idPrograma: id } }, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json.mock.calls[0][0].code).toBe("PROGRAMA_NO_ENCONTRADO");
  });

  it("un programa que no existe (null) da 404", async () => {
    getProgramaInscripcionModel.mockResolvedValue(null);

    expectError(await ejecutar(), 404, "PROGRAMA_NO_ENCONTRADO");
  });

  // `?.` evita que el código truene si el estado es null o undefined.
  it.each([[null], [undefined], [""], ["   "]])(
    "estado_del_programa=%j da PROGRAMA_NO_DISPONIBLE",
    async (estado) => {
      getProgramaInscripcionModel.mockResolvedValue(
        programaValido({ estado_del_programa: estado }),
      );

      expectError(await ejecutar(), 409, "PROGRAMA_NO_DISPONIBLE");
    },
  );

  it.each([[null], [undefined]])(
    "cupo_maximo=%j significa sin límite y ni siquiera cuenta inscritos",
    async (cupo) => {
      getProgramaInscripcionModel.mockResolvedValue(programaValido({ cupo_maximo: cupo }));

      expectExito(await ejecutar());
      expect(contarInscritosModel).not.toHaveBeenCalled();
    },
  );

  it.each([[null], [undefined]])(
    "fecha_inicio=%j da PROGRAMA_NO_DISPONIBLE y no crea la inscripción",
    async (fecha) => {
      // ¿Qué bug había? En JavaScript, `null <= "2026-..."` es falso, así que la regla 3 no
      // bloqueaba y alguien podía inscribirse a un programa sin fecha de inicio.
      // ¿Qué valida ahora? Que sin fecha se rechace con 409 y nunca se llegue a escribir.
      getProgramaInscripcionModel.mockResolvedValue(programaValido({ fecha_inicio: fecha }));

      expectError(await ejecutar(), 409, "PROGRAMA_NO_DISPONIBLE");
      expect(crearInscripcionModel).not.toHaveBeenCalled();
    },
  );
});

describe("análisis de valores límite", () => {
  it.each([
    { descripcion: "35 caracteres (falta uno)", id: ID_PROGRAMA.slice(0, -1) },
    { descripcion: "37 caracteres (sobra uno)", id: `${ID_PROGRAMA}5` },
    { descripcion: "un carácter hexadecimal inválido (g)", id: `g${ID_PROGRAMA.slice(1)}` },
    { descripcion: "salto de línea al final", id: `${ID_PROGRAMA}\n` },
    { descripcion: "espacio al inicio", id: ` ${ID_PROGRAMA}` },
    { descripcion: "guion en otra posición", id: "1111111-12222-3333-4444-555555555555" },
  ])("rechaza un UUID con $descripcion", async ({ id }) => {
    expectError(await ejecutar(id), 404, "PROGRAMA_NO_ENCONTRADO");
  });

  it("acepta un UUID con exactamente 36 caracteres y el último valor hexadecimal (f)", async () => {
    expectExito(await ejecutar("ffffffff-ffff-ffff-ffff-ffffffffffff"));
  });

  it.each([
    { inscritos: 0, cupo: 1, permitido: true }, // queda el único lugar
    { inscritos: 1, cupo: 1, permitido: false }, // el único lugar ya se ocupó
    { inscritos: 9, cupo: 10, permitido: true }, // justo antes: el último lugar
    { inscritos: 10, cupo: 10, permitido: false }, // justo en la frontera
    { inscritos: 11, cupo: 10, permitido: false }, // justo después: ya está sobrepasado
    { inscritos: 0, cupo: 0, permitido: false }, // cupo cero: nadie cabe
    { inscritos: 0, cupo: -1, permitido: false }, // dato raro: tampoco debe entrar nadie
    { inscritos: Number.MAX_SAFE_INTEGER - 1, cupo: Number.MAX_SAFE_INTEGER, permitido: true },
    { inscritos: Number.MAX_SAFE_INTEGER, cupo: Number.MAX_SAFE_INTEGER, permitido: false },
  ])("con $inscritos inscritos y cupo $cupo: permitido=$permitido", async (caso) => {
    getProgramaInscripcionModel.mockResolvedValue(programaValido({ cupo_maximo: caso.cupo }));
    contarInscritosModel.mockResolvedValue(caso.inscritos);

    const respuesta = await ejecutar();

    if (caso.permitido) {
      expectExito(respuesta);
    } else {
      expectError(respuesta, 409, "SIN_CUPO");
    }
  });

  it.each([
    { fecha: "2026-11-05T11:59:59", permitido: false }, // un segundo antes de ahora
    { fecha: AHORA_MEXICO, permitido: false }, // exactamente ahora: ya comenzó
    { fecha: "2026-11-05T12:00:00.001", permitido: true }, // un milisegundo después
    { fecha: "2026-11-05T12:00:01", permitido: true }, // un segundo después
  ])("con fecha de inicio $fecha: permitido=$permitido", async ({ fecha, permitido }) => {
    getProgramaInscripcionModel.mockResolvedValue(programaValido({ fecha_inicio: fecha }));

    const respuesta = await ejecutar();

    if (permitido) {
      expectExito(respuesta);
    } else {
      expectError(respuesta, 409, "PROGRAMA_INICIADO");
    }
  });

  it("calcula la hora en México también al cruzar la medianoche", async () => {
    // En UTC ya es 6 de noviembre, pero en México aún son las 23:59:59 del día 5.
    vi.setSystemTime(new Date("2026-11-06T05:59:59Z"));

    await ejecutar();

    expect(crearInscripcionModel).toHaveBeenCalledWith(
      ID_PROGRAMA,
      ID_USUARIO,
      "2026-11-05T23:59:59",
    );
  });

  it.each(["Disponible", " Disponible", "Disponible ", "\tDisponible\n"])(
    "el estado %j se acepta: los espacios de las orillas no cuentan",
    async (estado) => {
      getProgramaInscripcionModel.mockResolvedValue(
        programaValido({ estado_del_programa: estado }),
      );

      expectExito(await ejecutar());
    },
  );

  it.each(["disponible", "DISPONIBLE", "  DiSpOnIbLe  "])(
    "el estado '%s' se acepta porque las mayúsculas ya no importan",
    async (estado) => {
      // ¿Qué bug había? Android aceptaba estas variantes (ver ProgramaReglasTest.kt) y la API
      // no: el botón se veía habilitado y el servidor respondía 409.
      // ¿Qué valida ahora? Que la API también ignore mayúsculas y espacios, igual que Android.
      getProgramaInscripcionModel.mockResolvedValue(
        programaValido({ estado_del_programa: estado }),
      );

      expectExito(await ejecutar());
    },
  );
});

describe("cobertura estructural (decisión / condición)", () => {
  it("camino feliz: crea la inscripción con el programa, el usuario y la hora correctos", async () => {
    const respuesta = await ejecutar();

    expect(respuesta).toEqual({
      status: 201,
      cuerpo: {
        sucess: true,
        data: { id_programa: ID_PROGRAMA, fecha_inscripcion: AHORA_MEXICO, estado: "Activa" },
      },
    });
    expect(crearInscripcionModel).toHaveBeenCalledWith(ID_PROGRAMA, ID_USUARIO, AHORA_MEXICO);
  });

  it("el usuario ya inscrito (regla 4) da YA_INSCRITO", async () => {
    existeInscripcionModel.mockResolvedValue(true);

    expectError(await ejecutar(), 409, "YA_INSCRITO");
    expect(existeInscripcionModel).toHaveBeenCalledWith(ID_PROGRAMA, ID_USUARIO);
  });

  /*
   * ORDEN DE LAS REGLAS. Si un programa tiene varios problemas, gana la regla que va
   * primero. Además, una regla que falla debe DETENER el resto: `toHaveBeenCalled` revisa
   * que ninguna consulta ni inscripción se haya ejecutado después.
   */
  it("sin usuario gana sobre un id inválido", async () => {
    vi.stubEnv("DEV_USUARIO_ID", "");

    expectError(await ejecutar("id-invalido"), 401, "NO_AUTENTICADO");
  });

  it("programa no disponible gana sobre programa iniciado", async () => {
    getProgramaInscripcionModel.mockResolvedValue(
      programaValido({ estado_del_programa: "Cerrado", fecha_inicio: "2020-01-01T00:00:00" }),
    );

    expectError(await ejecutar(), 409, "PROGRAMA_NO_DISPONIBLE");
    expect(existeInscripcionModel).not.toHaveBeenCalled();
  });

  it("programa iniciado gana sobre ya inscrito", async () => {
    getProgramaInscripcionModel.mockResolvedValue(
      programaValido({ fecha_inicio: "2020-01-01T00:00:00" }),
    );
    existeInscripcionModel.mockResolvedValue(true);

    expectError(await ejecutar(), 409, "PROGRAMA_INICIADO");
    expect(existeInscripcionModel).not.toHaveBeenCalled();
  });

  it("ya inscrito gana sobre sin cupo", async () => {
    existeInscripcionModel.mockResolvedValue(true);
    contarInscritosModel.mockResolvedValue(10);

    expectError(await ejecutar(), 409, "YA_INSCRITO");
    expect(contarInscritosModel).not.toHaveBeenCalled();
  });

  it("sin cupo no crea la inscripción", async () => {
    contarInscritosModel.mockResolvedValue(10);

    expectError(await ejecutar(), 409, "SIN_CUPO");
    expect(crearInscripcionModel).not.toHaveBeenCalled();
  });

  /*
   * EL BLOQUE try/catch. Tiene tres caminos: 23505, 23503 y "cualquier otro error".
   * `mockRejectedValue` simula que la base de datos falla.
   */
  it("23505 (llave primaria duplicada por un doble toque) da YA_INSCRITO", async () => {
    crearInscripcionModel.mockRejectedValue(Object.assign(new Error("dup"), { code: "23505" }));

    expectError(await ejecutar(), 409, "YA_INSCRITO");
  });

  it("23503 (el usuario configurado no existe) da NO_AUTENTICADO", async () => {
    crearInscripcionModel.mockRejectedValue(Object.assign(new Error("fk"), { code: "23503" }));

    expectError(await ejecutar(), 401, "NO_AUTENTICADO");
  });

  it.each([
    { descripcion: "otro código", codigo: "42P01" },
    { descripcion: "código parecido", codigo: "23504" },
    { descripcion: "código vacío", codigo: "" },
    { descripcion: "código null", codigo: null },
    { descripcion: "sin código", codigo: undefined },
  ])("un error con $descripcion da 500 ERROR_INTERNO", async ({ codigo }) => {
    // Silenciamos console.error para no ensuciar la salida, y revisamos que sí se registró.
    const consola = vi.spyOn(console, "error").mockImplementation(() => {});
    crearInscripcionModel.mockRejectedValue(Object.assign(new Error("x"), { code: codigo }));

    expectError(await ejecutar(), 500, "ERROR_INTERNO");
    expect(consola).toHaveBeenCalledOnce();
  });

  // Cualquier paso dentro del `try` puede fallar: el error siempre debe atraparse.
  it.each([
    ["getProgramaInscripcionModel", getProgramaInscripcionModel],
    ["existeInscripcionModel", existeInscripcionModel],
    ["contarInscritosModel", contarInscritosModel],
    ["crearInscripcionModel", crearInscripcionModel],
  ])("si %s falla, se responde 500", async (_nombre, modelo) => {
    vi.spyOn(console, "error").mockImplementation(() => {});
    modelo.mockRejectedValue(new Error("base de datos caída"));

    expectError(await ejecutar(), 500, "ERROR_INTERNO");
  });
});
