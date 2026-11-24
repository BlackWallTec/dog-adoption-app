import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { fetchEmailByUUIDMock } = vi.hoisted(() => ({
  fetchEmailByUUIDMock: vi.fn(),
}));

vi.mock("../../src/modules/perfiles/http/modelosPerfiles/user.model.js", () => ({
  default: { fetchEmailByUUID: fetchEmailByUUIDMock },
}));
vi.mock("../../src/modules/perfiles/services/correoMovil.service.js", () => ({
  correoMovilHabilitado: vi.fn(() => false),
  servicioCorreoMovil: vi.fn(),
}));

import { getMe } from "../../src/modules/perfiles/http/auth.controller.js";

function crearRes() {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
}

describe("HU63-E - consulta de la sesion web", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("devuelve la cuenta validada por el token sin exponer el correo", async () => {
    fetchEmailByUUIDMock.mockResolvedValue("ana@example.com");
    const res = crearRes();

    await getMe({ user: { uuid: "cliente-1", rol: "Cliente" } }, res);

    expect(fetchEmailByUUIDMock).toHaveBeenCalledWith("cliente-1");
    expect(res.status).toHaveBeenCalledWith(200);
    expect(res.json).toHaveBeenCalledWith({
      user: { uuid: "cliente-1", rol: "Cliente" },
    });
    expect(res.json.mock.calls[0][0]).not.toHaveProperty("correo");
  });
});
