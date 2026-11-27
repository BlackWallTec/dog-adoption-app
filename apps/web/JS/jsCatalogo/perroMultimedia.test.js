import { describe, expect, test } from "vite-plus/test";
import { clasificarArchivo, esVideo, nombreSeguro, resumenGaleria } from "./perroMultimedia.js";

const archivo = (name, type = "") => ({ name, type });

describe("HU19C: clasificarArchivo", () => {
  test("acepta JPG, MP4 y MOV sin importar mayúsculas", () => {
    expect(clasificarArchivo(archivo("luna.JPG", "image/jpeg"))).toEqual({
      extension: "jpg",
      mime: "image/jpeg",
      tipo: "imagen",
    });
    expect(clasificarArchivo(archivo("luna.jpeg"))?.tipo).toBe("imagen");
    expect(clasificarArchivo(archivo("juego.mp4", "video/mp4"))?.tipo).toBe("video");
    expect(clasificarArchivo(archivo("juego.MOV"))?.mime).toBe("video/quicktime");
  });

  test("tolera el tipo genérico o ausente que reportan algunos navegadores", () => {
    expect(clasificarArchivo(archivo("a.mov", "application/octet-stream"))).not.toBeNull();
    expect(clasificarArchivo(archivo("a.mov", "video/x-quicktime"))).not.toBeNull();
    expect(clasificarArchivo(archivo("a.jpg", ""))).not.toBeNull();
  });

  test("rechaza otros formatos y tipos que contradicen la extensión", () => {
    expect(clasificarArchivo(archivo("a.png", "image/png"))).toBeNull();
    expect(clasificarArchivo(archivo("a.gif"))).toBeNull();
    expect(clasificarArchivo(archivo("a.exe"))).toBeNull();
    expect(clasificarArchivo(archivo("sinextension"))).toBeNull();
    expect(clasificarArchivo(archivo("a.jpg", "text/html"))).toBeNull();
    expect(clasificarArchivo(archivo("a.mp4", "image/jpeg"))).toBeNull();
    expect(clasificarArchivo(null)).toBeNull();
  });
});

describe("HU19C: nombreSeguro", () => {
  test("quita acentos, espacios y símbolos pero conserva la extensión", () => {
    expect(nombreSeguro("Perro León (1).jpg")).toBe("Perro_Leon_1_.jpg");
    expect(nombreSeguro("foto con espacios.MOV")).toBe("foto_con_espacios.MOV");
    expect(nombreSeguro("ya-esta_bien.v2.mp4")).toBe("ya-esta_bien.v2.mp4");
  });

  test("nunca devuelve un nombre vacío", () => {
    expect(nombreSeguro("")).toBe("archivo");
    expect(nombreSeguro(null)).toBe("archivo");
    expect(nombreSeguro("🐶")).toBe("_");
  });
});

describe("HU19C: esVideo", () => {
  test("usa el tipo de la BD y, si falta, la extensión de la url", () => {
    expect(esVideo({ tipo: "video", url: "https://x/a.jpg" })).toBe(true);
    expect(esVideo({ tipo: "imagen ", url: "https://x/a.mp4" })).toBe(false);
    expect(esVideo({ url: "https://x/a.MOV?token=1" })).toBe(true);
    expect(esVideo({ url: "https://x/a.jpg" })).toBe(false);
    expect(esVideo(null)).toBe(false);
  });
});

describe("HU19C: resumenGaleria", () => {
  test("cuenta lo que quedará tras guardar", () => {
    expect(resumenGaleria({ existentes: 3 })).toBe("3 archivos");
    expect(resumenGaleria({ existentes: 1 })).toBe("1 archivo");
    expect(resumenGaleria({ existentes: 3, nuevos: 1 })).toBe("4 archivos · 1 nuevo");
    expect(resumenGaleria({ existentes: 3, quitadas: 2, nuevos: 2 })).toBe(
      "3 archivos · 2 nuevos · 2 por quitar",
    );
  });

  test("sin archivos dice que no hay fotos ni videos", () => {
    expect(resumenGaleria()).toBe("Sin fotos ni videos");
    expect(resumenGaleria({ existentes: 2, quitadas: 2 })).toBe(
      "Sin fotos ni videos · 2 por quitar",
    );
  });

  test("nunca da un total negativo", () => {
    expect(resumenGaleria({ existentes: 1, quitadas: 5 })).toBe(
      "Sin fotos ni videos · 5 por quitar",
    );
  });
});
