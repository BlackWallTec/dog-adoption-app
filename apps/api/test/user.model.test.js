import { beforeEach, expect, test, vi } from "vite-plus/test";
import { supabaseAdmin } from "../src/config/supabaseClient.js";
import Usuario from "../src/modules/perfiles/http/modelosPerfiles/user.model.js";
import { registration } from "./helpers/registration-data.js";

vi.mock("../src/config/supabaseClient.js", () => ({
  supabaseAdmin: { from: vi.fn(), rpc: vi.fn() },
}));
beforeEach(() => vi.resetAllMocks());
test("el registro utiliza una sola RPC, sin enviar contrasena en texto plano ni rol", async () => {
  supabaseAdmin.rpc.mockResolvedValue({ data: "nuevo-id", error: null });
  expect(await Usuario.register(registration, "hash-bcrypt")).toEqual({
    id_usuario: "nuevo-id",
    rol: "Cliente",
  });
  const [name, args] = supabaseAdmin.rpc.mock.calls[0];
  expect(name).toBe("perfiles_registrar_cliente");
  expect(args.p_hash).toBe("hash-bcrypt");
  expect(args.p_numero_exterior).toBe(12);
  expect(Object.values(args)).not.toContain(registration.passwd);
  expect(args.p_rol).toBeUndefined();
  expect(supabaseAdmin.from.mock.calls).toHaveLength(0);
});
test("propaga el conflicto de correo para que HTTP lo convierta a 409", async () => {
  supabaseAdmin.rpc.mockResolvedValue({ data: null, error: { code: "23505" } });
  await expect(Usuario.register(registration, "hash")).rejects.toMatchObject({ code: "23505" });
});
test("la consulta de credenciales escapa comodines y usa el cliente privado", async () => {
  const query = {
    select: vi.fn().mockReturnThis(),
    ilike: vi.fn().mockReturnThis(),
    maybeSingle: vi.fn(async () => ({ data: null, error: null })),
  };
  supabaseAdmin.from.mockReturnValue(query);
  expect(await Usuario.fetchByEmail(" nombre_%@gmail.com ")).toBeNull();
  expect(query.ilike).toHaveBeenCalledWith("datos_confidenciales.correo", "nombre\\_\\%@gmail.com");
});
test("el perfil filtra por ID y no devuelve contrasenas ni datos fiscales", async () => {
  const query = {
    select: vi.fn().mockReturnThis(),
    eq: vi.fn().mockReturnThis(),
    maybeSingle: vi.fn(async () => ({
      error: null,
      data: {
        nombre: "Ana",
        apellidos: "Prueba",
        fecha_nacimiento: "2000-06-15T00:00:00",
        datos_confidenciales: { telefono: "5512345678", codigo_postal: "06000" },
        direcciones_fisicas: [{ calle: "Calle de prueba", numero_exterior: 12 }],
      },
    })),
  };
  supabaseAdmin.from.mockReturnValue(query);
  const profile = await Usuario.fetchProfile("id-propio");
  expect(query.eq).toHaveBeenCalledWith("id_usuario", "id-propio");
  expect(query.select.mock.calls[0][0]).not.toContain("contrasena");
  expect(profile).toMatchObject({ nombre: "Ana", calle: "Calle de prueba", codigoPostal: "06000" });
  expect(profile).not.toHaveProperty("rfc");
});
