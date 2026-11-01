import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

// Funciones falsas que reemplazan a los modelos, para no conectarse a Supabase.
// vi.hoisted las crea antes de que se ejecuten los vi.mock.
const mocks = vi.hoisted(() => ({
  getProgramaById: vi.fn(),
  getInscritosByPrograma: vi.fn(),
  obtenerNombres: vi.fn(),
  obtenerContacto: vi.fn(),
}));

vi.mock("./modelosVoluntariado/programa.model.js", () => ({
  default: { getProgramaById: mocks.getProgramaById },
}));
vi.mock("./modelosVoluntariado/inscripcion.model.js", () => ({
  default: { getInscritosByPrograma: mocks.getInscritosByPrograma },
}));
vi.mock("./modelosVoluntariado/perfiles.adapter.js", () => ({
  default: {
    obtenerNombres: mocks.obtenerNombres,
    obtenerContacto: mocks.obtenerContacto,
  },
}));

import { armarListaInscritos, getInscritos } from "./inscritosAdmin.controller.js";

const ID_PROGRAMA = "977abfc8-3a2c-45a2-8020-ff544e21ec7e";

// Datos con la misma forma que regresa Supabase.
const programaBD = {
  id_programa: ID_PROGRAMA,
  nombre: "Paseo dominical",
  tipo: "Paseo",
  fecha_inicio: "2026-11-15T09:00:00",
  fecha_fin: "2026-11-15T12:00:00",
  ubicacion: "Refugio",
  cupo_maximo: 10,
  estado_del_programa: "Disponible          ", // char(20) con espacios de relleno
};

const inscripcionesBD = [
  { id_usuario: "u1", fecha_inscripcion: "2026-10-14T03:18:11", asistencia: true },
  { id_usuario: "u2", fecha_inscripcion: "2026-10-15T01:37:25", asistencia: null },
];

const nombresBD = [
  { id_usuario: "u1", nombre: "Ana", apellidos: "López" },
  { id_usuario: "u2", nombre: "Luis", apellidos: "Pérez" },
];

// u2 no tiene registro en datos_confidenciales.
const contactosBD = [{ id_usuario: "u1", correo: "ana@gmail.com", telefono: "4421234567" }];

// Respuesta falsa de Express: guarda el status y el JSON que manda el controller.
const crearRes = () => {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
};

describe("armarListaInscritos", () => {
  it("junta el nombre y el contacto de cada inscrito", () => {
    const resultado = armarListaInscritos(programaBD, inscripcionesBD, nombresBD, contactosBD);

    expect(resultado.totalInscritos).toBe(2);
    expect(resultado.inscritos[0]).toEqual({
      idUsuario: "u1",
      nombreCompleto: "Ana López",
      correo: "ana@gmail.com",
      telefono: "4421234567",
      fechaInscripcion: "2026-10-14T03:18:11",
      asistencia: true,
    });
  });

  it("regresa null en correo, teléfono y asistencia cuando no hay datos", () => {
    const resultado = armarListaInscritos(programaBD, inscripcionesBD, nombresBD, contactosBD);

    expect(resultado.inscritos[1].correo).toBeNull();
    expect(resultado.inscritos[1].telefono).toBeNull();
    expect(resultado.inscritos[1].asistencia).toBeNull();
  });

  it("recorta los espacios del estado del programa", () => {
    const resultado = armarListaInscritos(programaBD, [], [], []);

    expect(resultado.programa.estado).toBe("Disponible");
  });

  it("regresa una lista vacía si no hay inscritos", () => {
    const resultado = armarListaInscritos(programaBD, [], [], []);

    expect(resultado.totalInscritos).toBe(0);
    expect(resultado.inscritos).toEqual([]);
  });
});

describe("getInscritos", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    // Evita que el console.error del caso 500 ensucie la salida de las pruebas.
    vi.spyOn(console, "error").mockImplementation(() => {});
  });

  it("responde 404 sin consultar la BD si el ID no es un UUID", async () => {
    const res = crearRes();
    await getInscritos({ params: { idPrograma: "abc" } }, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(mocks.getProgramaById).not.toHaveBeenCalled();
  });

  it("responde 404 si el programa no existe", async () => {
    mocks.getProgramaById.mockResolvedValue(null);
    const res = crearRes();
    await getInscritos({ params: { idPrograma: ID_PROGRAMA } }, res);

    expect(res.status).toHaveBeenCalledWith(404);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ success: false, code: "PROGRAMA_NO_ENCONTRADO" }),
    );
  });

  it("responde 200 con la lista de inscritos", async () => {
    mocks.getProgramaById.mockResolvedValue(programaBD);
    mocks.getInscritosByPrograma.mockResolvedValue(inscripcionesBD);
    mocks.obtenerNombres.mockResolvedValue(nombresBD);
    mocks.obtenerContacto.mockResolvedValue(contactosBD);
    const res = crearRes();
    await getInscritos({ params: { idPrograma: ID_PROGRAMA } }, res);

    expect(res.status).toHaveBeenCalledWith(200);
    const respuesta = res.json.mock.calls[0][0];
    expect(respuesta.success).toBe(true);
    expect(respuesta.data.totalInscritos).toBe(2);
    expect(mocks.obtenerNombres).toHaveBeenCalledWith(["u1", "u2"]);
  });

  it("responde 500 si falla la base de datos", async () => {
    mocks.getProgramaById.mockRejectedValue(new Error("Sin conexión"));
    const res = crearRes();
    await getInscritos({ params: { idPrograma: ID_PROGRAMA } }, res);

    expect(res.status).toHaveBeenCalledWith(500);
    expect(res.json).toHaveBeenCalledWith(
      expect.objectContaining({ success: false, code: "ERROR_INTERNO" }),
    );
  });
});
