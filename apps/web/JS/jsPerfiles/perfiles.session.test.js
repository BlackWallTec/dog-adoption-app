import { expect, test, vi } from "vite-plus/test";
import { createSessionView } from "./perfiles.session.js";

function fixture(api) {
  const ids =
    "login-panel session-panel session-email session-role session-feedback session-title session-logout login-form login-feedback login-email".split(
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
  const view = createSessionView(api, { getElementById: (id) => elements[id] });
  const logout = elements["session-logout"].addEventListener.mock.calls[0][1];
  return { elements, view, logout };
}
const session = { user: { email: "ana@example.test", rol: "Administrador" } };

test("mostrar la sesion y cerrarla limpia los datos visibles y el formulario", async () => {
  const api = { logout: vi.fn(async () => ({ ok: true })) };
  const { elements, view, logout } = fixture(api);
  view.show(session);
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
  view.show(session);
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
