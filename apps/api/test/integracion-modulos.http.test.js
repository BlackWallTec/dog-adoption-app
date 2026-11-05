import { beforeEach, expect, test, vi } from "vite-plus/test";
import { post, request, tokenFor } from "./helpers/auth-server.js";
import profileModel from "../src/modules/solicitudes/http/modelosSolicitudes/profile.model.js";
import adoptionModel from "../src/modules/solicitudes/http/modelosSolicitudes/adoptionRequest.model.js";
import * as inscripciones from "../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js";

vi.mock("../src/modules/solicitudes/http/modelosSolicitudes/profile.model.js", () => ({
  default: {
    getProfile: vi.fn(async (id) => ({
      id_usuario: id,
      nombre: "Ana",
      apellidos: "Prueba",
      fecha_nacimiento: "2000-01-01",
      cliente: { datos_confidenciales: { correo: "ana@example.test", telefono: "0000000000" } },
      direcciones_fisicas: [],
    })),
  },
}));
vi.mock("../src/modules/solicitudes/http/modelosSolicitudes/adoptionRequest.model.js", () => ({
  default: { getActiveRequest: vi.fn(async () => null) },
}));
vi.mock("../src/modules/voluntarios/http/modelosVoluntariado/program.model.js", () => ({
  default: { getCurrentPrograms: vi.fn(async () => []) },
}));
vi.mock("../src/modules/voluntarios/http/modelosVoluntariado/inscripcion.model.js", () => ({
  getProgramaInscripcionModel: vi.fn(async () => ({
    estado_del_programa: "Disponible",
    fecha_inicio: "2999-01-01",
    cupo_maximo: null,
  })),
  contarInscritosModel: vi.fn(async () => 0),
  existeInscripcionModel: vi.fn(async () => false),
  crearInscripcionModel: vi.fn(async (idPrograma) => ({
    id_programa: idPrograma,
    estado: "Inscrito",
  })),
}));

const headersFor = (rol = "Cliente") => ({
  "x-client": "mobile",
  Authorization: `Bearer ${tokenFor(rol)}`,
});
const programa = "11111111-1111-4111-8111-111111111111";
beforeEach(() => {
  profileModel.getProfile.mockClear();
  adoptionModel.getActiveRequest.mockClear();
  for (const spy of Object.values(inscripciones)) spy.mockClear();
});

test.each([
  ["GET", "/perros"],
  ["POST", "/perros"],
  ["POST", "/solicitar-subida"],
  ["GET", "/tamanos"],
  ["GET", "/caracteristicas"],
  ["GET", "/cuidado_especial"],
  ["GET", "/razas"],
])("catalogo administrativo %s %s conserva el control de acceso", async (method, path) => {
  const url = "/api/catalogo/admin" + path;
  expect((await request(url, { method })).status).toBe(401);
  expect((await request(url, { method, headers: headersFor() })).status).toBe(403);
});

test.each([
  ["/cliente/perfil/usuario-prueba", false, 401],
  ["/cliente/activa?idUsuario=usuario-prueba&idPerro=perro-prueba", false, 401],
  ["/cliente/perfil/otra-cuenta", true, 403],
  ["/cliente/activa?idUsuario=otra-cuenta&idPerro=perro-prueba", true, 403],
])("solicitudes rechaza lectura ajena o anonima %s", async (path, authenticated, status) => {
  expect(
    (await request("/api/solicitudes" + path, { headers: authenticated ? headersFor() : {} }))
      .status,
  ).toBe(status);
  expect(profileModel.getProfile.mock.calls).toHaveLength(0);
  expect(adoptionModel.getActiveRequest.mock.calls).toHaveLength(0);
});

test.each(["web", "mobile"])(
  "solicitudes consulta el perfil propio con sesion %s",
  async (client) => {
    const headers = client === "web" ? { Cookie: `token=${tokenFor("Cliente")}` } : headersFor();
    const response = await request("/api/solicitudes/cliente/perfil/usuario-prueba", { headers });
    expect(response.status).toBe(200);
    expect((await response.json()).data.id_usuario).toBe("usuario-prueba");
    expect(profileModel.getProfile.mock.calls).toEqual([["usuario-prueba"]]);
  },
);

test("el borrador comprueba solicitudes activas del usuario autenticado", async () => {
  const response = await request(
    "/api/solicitudes/cliente/activa?idUsuario=usuario-prueba&idPerro=perro-prueba",
    { headers: headersFor() },
  );
  expect(response.status).toBe(200);
  expect((await response.json()).data).toBeNull();
  expect(adoptionModel.getActiveRequest.mock.calls).toEqual([["usuario-prueba", "perro-prueba"]]);
});

test("inscribirse requiere sesion antes de consultar programas o escribir datos", async () => {
  expect((await post(`/api/voluntarios/programas/${programa}/inscripciones`)).status).toBe(401);
  expect(inscripciones.getProgramaInscripcionModel).not.toHaveBeenCalled();
  expect(inscripciones.crearInscripcionModel).not.toHaveBeenCalled();
});

test("la inscripcion usa la identidad del JWT y descarta el usuario de desarrollo", async () => {
  const original = process.env.DEV_USUARIO_ID;
  process.env.DEV_USUARIO_ID = "otra-cuenta";
  try {
    const response = await post(
      `/api/voluntarios/programas/${programa}/inscripciones`,
      { idUsuario: "otra-cuenta" },
      headersFor(),
    );
    expect(response.status).toBe(201);
    expect(inscripciones.existeInscripcionModel).toHaveBeenCalledExactlyOnceWith(
      programa,
      "usuario-prueba",
    );
    expect(inscripciones.crearInscripcionModel).toHaveBeenCalledWith(
      programa,
      "usuario-prueba",
      expect.any(String),
    );
  } finally {
    if (original === undefined) delete process.env.DEV_USUARIO_ID;
    else process.env.DEV_USUARIO_ID = original;
  }
});

test("el catalogo de voluntariados sigue disponible sin iniciar sesion", async () => {
  const response = await request("/api/voluntarios/cliente/programas");
  expect(response.status).toBe(200);
  expect(await response.json()).toEqual({ success: true, data: [] });
});
