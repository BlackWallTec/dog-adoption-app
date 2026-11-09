import { expect, test, vi } from "vite-plus/test";
import { createSessionView } from "./perfiles.session.js";

function fixture(api) {
  const ids =
    "login-panel session-panel session-email session-role session-feedback session-title session-logout login-form login-feedback login-email profile-name profile-phone profile-birth profile-address profile-loading profile-feedback profile-retry".split(
      " ",
    );
  const elements = Object.fromEntries(
    ids.map((id) => [
      id,
      {
        hidden: false,
        textContent: "",
        disabled: false,
        focus: vi.fn(),
        reset: vi.fn(),
        addEventListener: vi.fn(),
      },
    ]),
  );
  api.getProfile ??= vi.fn(async () => ({ profile: { nombre: "Ana", apellidos: "Prueba" } }));
  const view = createSessionView(api, { getElementById: (id) => elements[id] });
  const logout = elements["session-logout"].addEventListener.mock.calls[0][1];
  return { elements, view, logout };
}
const session = { user: { email: "ana@example.test", rol: "Administrador" } };

test("mostrar la sesion y cerrarla limpia los datos visibles y el formulario", async () => {
  const api = { logout: vi.fn(async () => ({ ok: true })) };
  const { elements, view, logout } = fixture(api);
  await view.show(session);
  expect(elements["login-panel"].hidden).toBe(true);
  expect(elements["session-email"].textContent).toBe(session.user.email);
  await logout();
  expect(api.logout).toHaveBeenCalledOnce();
  expect(elements["login-form"].reset).toHaveBeenCalledOnce();
  expect(elements["session-panel"].hidden).toBe(true);
  expect(elements["session-email"].textContent).toBe("");
  expect(elements["login-panel"].hidden).toBe(false);
  expect(elements["login-email"].focus).toHaveBeenCalledOnce();
});

test("un fallo al cerrar mantiene la sesion visible y permite reintentar", async () => {
  const api = {
    logout: vi.fn(async () => {
      throw new Error("Sin conexion");
    }),
  };
  const { elements, view, logout } = fixture(api);
  await view.show(session);
  await logout();
  expect(elements["session-panel"].hidden).toBe(false);
  expect(elements["login-form"].reset).not.toHaveBeenCalled();
  expect(elements["session-feedback"].textContent).toBe("Sin conexion");
  expect(elements["session-feedback"].hidden).toBe(false);
  expect(elements["session-logout"].disabled).toBe(false);
});

test("un doble clic no duplica la peticion de cierre de sesion", async () => {
  let finish;
  const api = {
    logout: vi.fn(
      () =>
        new Promise((resolve) => {
          finish = resolve;
        }),
    ),
  };
  const { logout } = fixture(api);
  const first = logout();
  await logout();
  expect(api.logout).toHaveBeenCalledOnce();
  finish({ ok: true });
  await first;
});

test("presenta solamente los datos recibidos y permite reintentar una consulta fallida", async () => {
  const api = {
    getProfile: vi
      .fn()
      .mockRejectedValueOnce(new Error("Sin conexión"))
      .mockResolvedValue({
        profile: { nombre: "<Ana>", telefono: "5512345678", fechaNacimiento: "2000-06-15" },
      }),
  };
  const { elements, view } = fixture(api);
  await view.show(session);
  expect(elements["profile-feedback"].textContent).toBe("Sin conexión");
  expect(elements["profile-retry"].hidden).toBe(false);
  const retry = elements["profile-retry"].addEventListener.mock.calls[0][1];
  await retry();
  expect(elements["profile-name"].textContent).toBe("<Ana>");
  expect(elements["profile-birth"].textContent).toBe("15/06/2000");
  expect(elements["profile-feedback"].hidden).toBe(true);
});

test("una respuesta tardia del perfil no vuelve a mostrar datos despues de cerrar sesion", async () => {
  let complete;
  const api = {
    logout: vi.fn(async () => ({ ok: true })),
    getProfile: () =>
      new Promise((resolve) => {
        complete = resolve;
      }),
  };
  const { elements, view, logout } = fixture(api);
  const loading = view.show(session);
  await logout();
  complete({ profile: { nombre: "Dato privado" } });
  await loading;
  expect(elements["profile-name"].textContent).toBe("");
  expect(elements["session-panel"].hidden).toBe(true);
});
