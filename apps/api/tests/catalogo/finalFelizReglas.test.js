import { describe, expect, test } from "vite-plus/test";
import { prepararFinalesFelices } from "../../src/modules/catalogo/http/finalFelizReglas.js";

function fila(cambios = {}) {
  return {
    id_perro: "perro-1",
    foto_hogar_url: "https://foto",
    historia: "Ya tiene hogar.",
    fecha_adopcion: "2026-03-01",
    publicado: true,
    perro: { nombre: "Luna", estado: "Adoptado" },
    ...cambios,
  };
}

describe("prepararFinalesFelices", () => {
  test("solo deja perros adoptados, publicados y completos", () => {
    const data = prepararFinalesFelices([
      fila(),
      fila({ id_perro: "proceso", perro: { nombre: "Sol", estado: "En proceso de adopción" } }),
      fila({ id_perro: "sin-foto", foto_hogar_url: "  " }),
      fila({ id_perro: "sin-historia", historia: "" }),
      fila({ id_perro: "oculto", publicado: false }),
      fila({ id_perro: "disponible", perro: { nombre: "Nube", estado: "Disponible" } }),
    ]);

    expect(data.map((item) => item.id_perro)).toEqual(["perro-1"]);
    expect(data[0]).toMatchObject({
      nombre: "Luna",
      estado: "Adoptado",
      foto_hogar_url: "https://foto",
      historia: "Ya tiene hogar.",
    });
  });

  test("conserva la primera historia, ordena por fecha y manda al final las que no tienen", () => {
    const data = prepararFinalesFelices([
      fila({
        id_perro: "sin-fecha",
        fecha_adopcion: null,
        perro: { nombre: "Zoo", estado: "adoptada" },
      }),
      fila({ id_perro: "viejo", fecha_adopcion: "2020-01-01" }),
      fila({ id_perro: " mismo ", historia: "Primera.", fecha_adopcion: null }),
      fila({ id_perro: "mismo", historia: "Segunda.", fecha_adopcion: "2026-08-01" }),
      fila({ id_perro: "nuevo", fecha_adopcion: "2026-09-01" }),
    ]);

    expect(data.map((item) => item.id_perro)).toEqual(["nuevo", "viejo", "sin-fecha", "mismo"]);
    expect(data.find((item) => item.id_perro === "mismo").historia).toBe("Primera.");
  });

  test("corta en 20 y una entrada que no es lista regresa vacio", () => {
    const filas = Array.from({ length: 21 }, (_, indice) =>
      fila({
        id_perro: `perro-${indice}`,
        fecha_adopcion: `2024-01-${String(indice + 1).padStart(2, "0")}`,
      }),
    );

    const data = prepararFinalesFelices(filas);

    expect(data).toHaveLength(20);
    expect(data[0].id_perro).toBe("perro-20");
    expect(data.some((item) => item.id_perro === "perro-0")).toBe(false);
    expect(prepararFinalesFelices(null)).toEqual([]);
  });
});
