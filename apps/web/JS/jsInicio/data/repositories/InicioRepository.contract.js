import { expect, test } from "vite-plus/test";
import { esInicioRepository } from "../../domain/repositories/InicioRepository.js";

const RANGO = { desde: "2026-11-26", hasta: "2026-12-24" };

/**
 * @param {(escenario: "ok" | "vacio" | "parcial" | "error" | "lento" | "atrasado") => import("../../domain/repositories/InicioRepository.js").InicioRepository} crear
 */
export function registrarContratoInicio(crear) {
  test("ok prioriza, recorta a 6 y normaliza folio, nombre y perro", async () => {
    const repo = crear("ok");
    expect(esInicioRepository(repo)).toBe(true);

    const resumen = await repo.obtenerResumen();
    expect(resumen.perros).toEqual({ disponibles: 18, ingresosDelMes: 3 });
    expect(resumen.generadoEn).toEqual(new Date("2026-11-26T14:15:00-06:00"));
    expect(resumen).not.toHaveProperty("voluntariado");

    const tareas = await repo.obtenerPendientes();
    expect(tareas.map((tarea) => tarea.id)).toEqual([
      "c5555555-5555-4555-8555-555555555555",
      "f4444444-4444-4444-8444-444444444444",
      "a2222222-2222-4222-8222-222222222222",
      "d3333333-3333-4333-8333-333333333333",
      "a1111111-1111-4111-8111-111111111111",
      "f9999999-9999-4999-8999-999999999999",
    ]);
    expect(tareas.map((tarea) => tarea.urgencia)).toEqual([
      "critica",
      "critica",
      "atrasada",
      "atrasada",
      "normal",
      "normal",
    ]);
    expect(tareas[2].folio).toBe("QA-A2222222");
    expect(tareas[3].persona.nombre).toBe("Mariana Ruiz");
    expect(tareas[2].perro).toEqual({
      id: "b6666666-6666-4666-8666-666666666666",
      nombre: "Bruno",
    });
    expect(tareas.every((tarea) => !("requiereRevision" in tarea))).toBe(true);
    expect((await repo.obtenerPendientes({ limite: 2 })).map((tarea) => tarea.urgencia)).toEqual([
      "critica",
      "critica",
    ]);

    const agenda = await repo.obtenerAgenda(RANGO);
    expect(agenda.jornadas[0].nombre).toBe("Paseo en el parque");
    expect(agenda.jornadas[0].inicio).toEqual(new Date("2026-11-28T09:00:00-06:00"));
    expect(agenda.semanasSinCobertura).toEqual([{ inicio: "2026-11-30", fin: "2026-12-07" }]);

    const perro = await repo.obtenerPerroDestacado();
    expect(perro?.nombre).toBe("Canela");
    expect(perro?.diasEnRefugio).toBe(214);
    expect(perro?.fechaIngreso).toEqual(new Date("2026-04-26T00:00:00-06:00"));
  });

  test("vacio devuelve ceros, lista vacía y perro nulo", async () => {
    const repo = crear("vacio");
    const resumen = await repo.obtenerResumen();
    expect(resumen.solicitudes).toEqual({ porRevisar: 0, enRevision: 0, masAntiguaDesde: null });
    expect(resumen.donativos.masAntiguoDesde).toBeNull();
    expect(await repo.obtenerPendientes()).toEqual([]);
    expect(await repo.obtenerAgenda(RANGO)).toEqual({ jornadas: [], semanasSinCobertura: [] });
    expect(await repo.obtenerPerroDestacado()).toBeNull();
  });

  test("atrasado no deja tareas en urgencia normal", async () => {
    const tareas = await crear("atrasado").obtenerPendientes({ limite: 10 });
    expect(tareas.some((tarea) => tarea.urgencia === "critica")).toBe(true);
    expect(
      tareas.every((tarea) => tarea.urgencia === "critica" || tarea.urgencia === "atrasada"),
    ).toBe(true);
  });

  test("parcial conserva el resumen y falla solo la agenda", async () => {
    const repo = crear("parcial");
    await expect(repo.obtenerResumen()).resolves.toMatchObject({ perros: { disponibles: 18 } });
    await expect(repo.obtenerAgenda(RANGO)).rejects.toMatchObject({
      name: "INTERNAL",
      code: "INTERNAL_ERROR",
    });
  });

  test("error rechaza los cuatro métodos", async () => {
    const repo = crear("error");
    const esperado = { name: "INTERNAL", code: "INTERNAL_ERROR" };
    await expect(repo.obtenerResumen()).rejects.toMatchObject(esperado);
    await expect(repo.obtenerPendientes()).rejects.toMatchObject(esperado);
    await expect(repo.obtenerAgenda(RANGO)).rejects.toMatchObject(esperado);
    await expect(repo.obtenerPerroDestacado()).rejects.toMatchObject(esperado);
  });

  test("lento responde el mismo resumen después de la espera", async () => {
    const inicio = Date.now();
    const resumen = await crear("lento").obtenerResumen();
    expect(Date.now() - inicio).toBeGreaterThanOrEqual(40);
    expect(resumen.perros.disponibles).toBe(18);
  });

  test("una señal ya abortada no lee el fixture", async () => {
    const signal = AbortSignal.abort();
    await expect(crear("ok").obtenerResumen({ signal })).rejects.toMatchObject({
      name: "AbortError",
    });
  });
}
