import { describe, it, expect, vi, beforeAll } from "vite-plus/test";
import jwt from "jsonwebtoken";

// Para que no se conecte a Supabase al importar las rutas
vi.mock("../../../config/supabaseClient.js", () => ({ supabase: {}, supabaseAdmin: {} }));

import { router } from "./routes.js";

const ID_USUARIO = "977abfc8-3a2c-45a2-8020-ff544e21ec7e";

// Las rutas de cliente de HU45, HU46 y HU49
const RUTAS_CLIENTE = [
  ["get", "/cliente/datos-fiscales"],
  ["put", "/cliente/datos-fiscales"],
  ["put", "/cliente/datos-fiscales/constancia"],
  ["post", "/cliente/donativos"],
  ["get", "/cliente/mis-donativos"],
  ["post", "/cliente/mis-donativos/:idDonativo/cfdi"],
];

const crearRes = () => {
  const res = {};
  res.status = vi.fn(() => res);
  res.json = vi.fn(() => res);
  return res;
};

// Corre los middlewares de la ruta uno por uno hasta que alguno conteste
const probarRuta = (metodo, ruta, token) => {
  const capa = router.stack.find((c) => c.route?.path === ruta && c.route.methods[metodo]);
  const req = {
    get: (h) => ({ "x-client": "mobile", authorization: token && `Bearer ${token}` })[h],
    cookies: {},
  };
  const res = crearRes();
  let paso = 0;
  const handlers = capa.route.stack.map((c) => c.handle);
  const next = () => {
    paso++;
  };
  // solo los dos primeros: verificarToken y requireRole
  for (const h of handlers.slice(0, 2)) {
    const antes = paso;
    h(req, res, next);
    if (paso === antes) break;
  }
  return { res, paso, req };
};

beforeAll(() => {
  process.env.JWT_SECRET = "secreto-de-prueba";
});

describe("rutas de cliente de donativos protegidas", () => {
  it.each(RUTAS_CLIENTE)("%s %s sin token regresa 401", (metodo, ruta) => {
    const { res, paso } = probarRuta(metodo, ruta);

    expect(paso).toBe(0);
    expect(res.status).toHaveBeenCalledWith(401);
  });

  it.each(RUTAS_CLIENTE)("%s %s con un administrador regresa 403", (metodo, ruta) => {
    const token = jwt.sign({ uuid: ID_USUARIO, rol: "Administrador" }, process.env.JWT_SECRET);
    const { res, paso } = probarRuta(metodo, ruta, token);

    expect(paso).toBe(1);
    expect(res.status).toHaveBeenCalledWith(403);
  });

  it.each(RUTAS_CLIENTE)("%s %s con un cliente sí pasa", (metodo, ruta) => {
    const token = jwt.sign({ uuid: ID_USUARIO, rol: "Cliente" }, process.env.JWT_SECRET);
    const { res, paso, req } = probarRuta(metodo, ruta, token);

    expect(paso).toBe(2);
    expect(res.status).not.toHaveBeenCalled();
    // el controlador va a usar este id, no uno de la URL
    expect(req.user.uuid).toBe(ID_USUARIO);
  });

  it("ya no existen las rutas viejas con :idUsuario", () => {
    const viejas = router.stack.filter((c) => c.route?.path.includes(":idUsuario"));
    expect(viejas).toHaveLength(0);
  });
});
