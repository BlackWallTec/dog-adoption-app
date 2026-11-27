import { expect, test } from "vite-plus/test";
import { crearInicioRepositoryMock } from "./InicioRepositoryMock.js";
import { registrarContratoInicio } from "./InicioRepository.contract.js";

const ahora = new Date("2026-11-26T14:15:00-06:00");

registrarContratoInicio((escenario) => crearInicioRepositoryMock({ escenario, ahora }));

test("un escenario desconocido no crea el repositorio", () => {
  expect(() => crearInicioRepositoryMock({ escenario: "otro", ahora })).toThrow(/Escenario/);
});
