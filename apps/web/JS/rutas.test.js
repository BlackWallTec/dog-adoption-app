import { expect, test } from "vite-plus/test";
import {
  archivoDeRuta,
  enlaceA,
  idMenuActivo,
  pantallaPorUbicacion,
  rutaDe,
  tituloDe,
} from "./rutas.js";

test("la pantalla de perros se abre en /admin/perros", () => {
  expect(rutaDe("perros")).toBe("/admin/perros");
  expect(archivoDeRuta("/admin/perros")).toBe("/HTML/htmlCatalogo/adminPerros.html");
  expect(archivoDeRuta("/admin/perros/?id=1")).toBe("/HTML/htmlCatalogo/adminPerros.html?id=1");
});

test("las solicitudes CFDI usan una ruta del módulo de donativos", () => {
  expect(rutaDe("adminSolicitudesCfdi")).toBe("/admin/donativos/solicitudes-cfdi");
  expect(archivoDeRuta("/admin/donativos/solicitudes-cfdi")).toBe(
    "/HTML/htmlDonativos/adminSolicitudesCfdi.html",
  );
  expect(idMenuActivo("/admin/donativos/solicitudes-cfdi")).toBe("donativos");
});

test("reconoce la ruta publica y el archivo al marcar el menu", () => {
  expect(pantallaPorUbicacion("/admin/perros")?.id).toBe("perros");
  expect(pantallaPorUbicacion("/HTML/htmlCatalogo/adminPerros.html")?.id).toBe("perros");
  expect(idMenuActivo("/admin/voluntariado/nueva")).toBe("voluntariado");
  expect(tituloDe("/admin/perros")).toBe("Perros");
  expect(tituloDe("/admin/voluntariado/inscritos")).toBe("Voluntariado");
});

test("una ruta ajena al panel no se reescribe", () => {
  expect(archivoDeRuta("/login")).toBeNull();
  expect(pantallaPorUbicacion("/login")).toBeNull();
  expect(tituloDe("/login")).toBe("Panel");
});

test("enlaceA arma la ruta publica con sus parametros", () => {
  expect(enlaceA("solicitudes", { estado: "pendientes" })).toBe(
    "/admin/solicitudes?estado=pendientes",
  );
  expect(enlaceA("perros", { abrir: "abc" })).toBe("/admin/perros?abrir=abc");
  expect(enlaceA("nuevaJornada")).toBe("/admin/voluntariado/nueva");
  expect(enlaceA("donativos", { abrir: "" })).toBe("/admin/donativos");
});

test("el calendario de voluntariado se abre en /admin/voluntariado/calendario", () => {
  expect(rutaDe("calendario")).toBe("/admin/voluntariado/calendario");
  expect(archivoDeRuta("/admin/voluntariado/calendario")).toBe(
    "/HTML/htmlVoluntariado/adminCalendario.html",
  );
  expect(idMenuActivo("/admin/voluntariado/calendario")).toBe("voluntariado");
  expect(tituloDe("/admin/voluntariado/calendario")).toBe("Voluntariado");
});
