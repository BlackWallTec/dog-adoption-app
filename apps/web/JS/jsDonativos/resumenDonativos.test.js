import { afterEach, beforeEach, expect, test, vi } from "vite-plus/test";
import { obtenerResumenPublicoDonativos } from "./resumenDonativos.js";

const fetchMock = vi.fn();

beforeEach(() => {
  vi.stubGlobal("fetch", fetchMock);
});

afterEach(() => {
  vi.unstubAllGlobals();
  fetchMock.mockReset();
});

test("obtiene el total agregado del resumen público", async () => {
  fetchMock.mockResolvedValue({
    ok: true,
    json: async () => ({ totalPendientes: 7 }),
  });

  await expect(obtenerResumenPublicoDonativos()).resolves.toBe(7);
  expect(fetchMock).toHaveBeenCalledWith("/api/donativos/pendientes/resumen", {
    headers: { Accept: "application/json" },
  });
});

test("rechaza una respuesta que no contiene un conteo válido", async () => {
  fetchMock.mockResolvedValue({
    ok: true,
    json: async () => ({ donativos: [{ id: 1 }] }),
  });

  await expect(obtenerResumenPublicoDonativos()).rejects.toThrow(
    "La respuesta del resumen de donativos tiene un formato inválido.",
  );
});
