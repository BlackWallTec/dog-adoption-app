import { perfilesApi } from "./perfiles.api.js";

// Código postal en dos pasos:
//   1. La persona escribe 5 dígitos en #register-cp (este input NO se envía).
//   2. Se consulta GET /api/perfiles/codigos-postales?cp=XXXXX y se llena el
//      <select id="register-id_codigo_postal"> con las colonias de ese CP.
//      El value de cada <option> es el id_codigo_postal, que es lo que sí se
//      manda al registrar.
//
// Uso en registro.js:
//   import { initCodigoPostal } from "./registro.cp.js";
//   initCodigoPostal();

const byId = (id) => document.getElementById(id);

export function initCodigoPostal() {
  const cpInput = byId("register-cp");
  const select = byId("register-id_codigo_postal");
  const resumen = byId("register-cp-resumen");
  let consulta = 0; // descarta respuestas viejas si se sigue escribiendo

  function vaciar(mensaje) {
    // new Option usa texto plano: no hay riesgo de inyectar HTML desde la BD
    select.replaceChildren(new Option(mensaje, ""));
    select.disabled = true;
    resumen.textContent = "";
  }

  async function buscar() {
    const cp = cpInput.value.trim();
    const actual = ++consulta;

    if (!/^\d{5}$/.test(cp)) {
      vaciar("Primero escribe tu código postal");
      return;
    }

    vaciar("Buscando colonias…");
    try {
      const { data } = await perfilesApi.getCodigosPostales(cp);
      if (actual !== consulta) return;

      if (data.length === 0) {
        vaciar("No encontramos ese código postal");
        return;
      }

      select.replaceChildren(
        new Option("Elige tu colonia", ""),
        ...data.map((fila) => new Option(fila.colonia ?? "(Sin colonia)", fila.id_codigo_postal)),
      );
      select.disabled = false;
      resumen.textContent = [data[0].municipio, data[0].estado].filter(Boolean).join(", ");
    } catch {
      if (actual !== consulta) return;
      vaciar("No pudimos consultar. Intenta de nuevo");
    }
  }

  cpInput.addEventListener("input", buscar);
  vaciar("Primero escribe tu código postal");
}
