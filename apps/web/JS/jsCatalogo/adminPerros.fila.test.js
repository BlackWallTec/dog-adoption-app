import { expect, test } from "vite-plus/test";
import {
  FOTO_RESPALDO,
  NO_ESPECIFICADO,
  listadoDeRespuesta,
  marcadoErrorListado,
  textoContador,
  textoDato,
  textoEdad,
  textoPaginacion,
  urlFoto,
} from "./adminPerros.fila.js";

test("usa la primera url de multimedia y, si no hay, la imagen de respaldo", () => {
  expect(
    urlFoto({
      multimedia: [{ url: "" }, { url: " https://fotos/luna.jpg " }],
    }),
  ).toBe("https://fotos/luna.jpg");

  expect(urlFoto({ multimedia: [] })).toBe(FOTO_RESPALDO);
  expect(urlFoto({})).toBe(FOTO_RESPALDO);
  expect(urlFoto({ foto_url: "https://fotos/sol.jpg" })).toBe("https://fotos/sol.jpg");
});

test("un dato ausente se muestra como no especificado", () => {
  expect(textoDato(null)).toBe(NO_ESPECIFICADO);
  expect(textoDato("")).toBe(NO_ESPECIFICADO);
  expect(textoDato({ nombre: "   " })).toBe(NO_ESPECIFICADO);
  expect(textoDato({ nombre: "Labrador" })).toBe("Labrador");
  expect(textoEdad({})).toBe(NO_ESPECIFICADO);
  expect(textoEdad({ edad_aprox: 3 })).toBe("3 años");
});

test("el contador describe la ultima consulta y el error ofrece reintentar", () => {
  expect(textoContador(0)).toBe("No hay perros registrados.");
  expect(textoContador(1)).toBe("Mostrando 1 perro bajo cuidado");
  expect(textoPaginacion(2)).toBe("Mostrando 1-2 de 2 perros");
  expect(textoPaginacion(0)).toBe("Mostrando 0 de 0 perros");

  const error = marcadoErrorListado();
  expect(error).toContain('class="estado-error"');
  expect(error).toContain('class="btn-primary"');
  expect(error).toContain("Reintentar");
  expect(error).not.toContain("foto_url");
});

test("solo acepta un listado; un error o un cuerpo invalido no se pintan como filas", () => {
  expect(listadoDeRespuesta({ success: true, data: [{ id_perro: 1 }] })).toEqual([{ id_perro: 1 }]);
  expect(listadoDeRespuesta({ success: true, data: [] })).toEqual([]);
  expect(listadoDeRespuesta({ success: false, message: "falló" })).toBeNull();
  expect(listadoDeRespuesta({ success: true })).toBeNull();
  expect(listadoDeRespuesta(null)).toBeNull();
});
