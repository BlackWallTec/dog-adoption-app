import { afterEach, beforeEach, expect, test, vi } from "vite-plus/test";
import {
  obtenerDetalleSolicitudCfdi,
  obtenerSolicitudesCfdiPendientes,
} from "./adminSolicitudesCfdi.js";

const fetchMock = vi.fn();

beforeEach(() => {
  vi.stubGlobal("fetch", fetchMock);
});

afterEach(() => {
  vi.unstubAllGlobals();
  fetchMock.mockReset();
});

test("consulta la bandeja con cookie de sesión y sin caché", async () => {
  fetchMock.mockResolvedValue({
    ok: true,
    status: 200,
    headers: { get: () => "application/json" },
    json: async () => [],
  });

  await expect(obtenerSolicitudesCfdiPendientes()).resolves.toEqual([]);
  expect(fetchMock).toHaveBeenCalledWith("/api/donativos/admin/solicitudes-cfdi", {
    credentials: "same-origin",
    cache: "no-store",
    headers: { Accept: "application/json" },
  });
});

test("consulta solo el detalle seleccionado", async () => {
  fetchMock.mockResolvedValue({
    ok: true,
    status: 200,
    headers: { get: () => "application/json" },
    json: async () => ({ datos_fiscales: { constancia_url: "https://storage.example/csf" } }),
  });

  await obtenerDetalleSolicitudCfdi("solicitud-1");
  expect(fetchMock).toHaveBeenCalledWith("/api/donativos/admin/solicitudes-cfdi/solicitud-1", {
    credentials: "same-origin",
    cache: "no-store",
    headers: { Accept: "application/json" },
  });
});

test("rechaza enlaces de constancia que no usan HTTPS", async () => {
  fetchMock.mockResolvedValue({
    ok: true,
    status: 200,
    headers: { get: () => "application/json" },
    json: async () => ({ datos_fiscales: { constancia_url: "javascript:alert(1)" } }),
  });

  await expect(obtenerDetalleSolicitudCfdi("solicitud-1")).rejects.toThrow(
    "La URL de la constancia fiscal no es segura.",
  );
});
