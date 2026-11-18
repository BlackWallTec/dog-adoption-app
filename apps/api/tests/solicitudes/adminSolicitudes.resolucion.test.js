import { describe, it, expect } from "vite-plus/test";

// Lógica de validación extraída de adminSolicitudes.view.js
function validarResolucionPure(estado, comentarioRaw) {
  const comentario = String(comentarioRaw || "").trim();
  const estadosValidos = ["en_revision", "periodo_prueba", "aprobada", "rechazada"];

  if (!estadosValidos.includes(estado)) {
    return { ok: false, mensaje: "Selecciona un estado válido." };
  }

  if (estado === "rechazada" && !comentario) {
    return {
      ok: false,
      mensaje: "Debes escribir un comentario interno cuando la solicitud es rechazada.",
    };
  }

  if (comentario.length > 1000) {
    return { ok: false, mensaje: "El comentario no puede superar los 1,000 caracteres." };
  }

  return {
    ok: true,
    datos: {
      state: estado,
      comment: estado === "rechazada" ? comentario : null,
    },
  };
}

// Lógica de mapeo de estado inicial al abrir el detalle en adminSolicitudes.detalle.js
function mapearEstadoInicialSelect(estadoDetalle) {
  const estadoRaw = String(estadoDetalle || "").toLowerCase();
  if (estadoRaw.includes("aprob")) return "aprobada";
  if (estadoRaw.includes("rechaz")) return "rechazada";
  if (estadoRaw.includes("prueba")) return "periodo_prueba";
  return "en_revision";
}

// Lógica de sincronización visual del formulario (punto de color y visibilidad del comentario)
function calcularEstadoVisualFormulario(estado, comentarioActual) {
  const esRechazada = estado === "rechazada";

  let claseDot = "dot-revision";
  if (estado === "aprobada") claseDot = "dot-aprobada";
  else if (estado === "rechazada") claseDot = "dot-rechazada";
  else if (estado === "periodo_prueba") claseDot = "dot-prueba";

  const comentarioFinal = esRechazada ? comentarioActual : "";
  const textoContador = `${comentarioFinal.length.toLocaleString("es-MX")} / 1,000`;

  return {
    claseDot,
    ocultarComentario: !esRechazada,
    comentarioFinal,
    textoContador,
  };
}

describe("validarResolucion - Validaciones del modal de gestión", () => {
  it("rechaza la validación si el estado no está dentro de los permitidos", () => {
    const res = validarResolucionPure("cancelada", "");
    expect(res.ok).toBe(false);
    expect(res.mensaje).toBe("Selecciona un estado válido.");
  });

  it("exige comentario obligatorio cuando el estado es 'rechazada'", () => {
    const resVacio = validarResolucionPure("rechazada", "   ");
    expect(resVacio.ok).toBe(false);
    expect(resVacio.mensaje).toBe(
      "Debes escribir un comentario interno cuando la solicitud es rechazada.",
    );
  });

  it("permite rechazar cuando se incluye un comentario válido y hace trim de espacios", () => {
    const res = validarResolucionPure("rechazada", "  Vivienda sin malla protectora  ");
    expect(res).toEqual({
      ok: true,
      datos: {
        state: "rechazada",
        comment: "Vivienda sin malla protectora",
      },
    });
  });

  it("bloquea comentarios que superen los 1,000 caracteres", () => {
    const textoLargo = "a".repeat(1001);
    const res = validarResolucionPure("rechazada", textoLargo);
    expect(res.ok).toBe(false);
    expect(res.mensaje).toBe("El comentario no puede superar los 1,000 caracteres.");
  });

  it("permite exactamente 1,000 caracteres en un rechazo", () => {
    const textoExacto = "a".repeat(1000);
    const res = validarResolucionPure("rechazada", textoExacto);
    expect(res.ok).toBe(true);
    expect(res.datos.comment).toHaveLength(1000);
  });

  it("envía comment en null cuando se aprueba o pasa a periodo de prueba, aunque hubiera texto", () => {
    const resAprobada = validarResolucionPure("aprobada", "Algún texto residual");
    expect(resAprobada).toEqual({
      ok: true,
      datos: { state: "aprobada", comment: null },
    });

    const resPrueba = validarResolucionPure("periodo_prueba", "");
    expect(resPrueba).toEqual({
      ok: true,
      datos: { state: "periodo_prueba", comment: null },
    });
  });
});

describe("mapearEstadoInicialSelect - Sincronización al abrir el detalle", () => {
  it("mapea correctamente los textos de estado provenientes del backend", () => {
    expect(mapearEstadoInicialSelect("Aprobada")).toBe("aprobada");
    expect(mapearEstadoInicialSelect("Rechazada")).toBe("rechazada");
    expect(mapearEstadoInicialSelect("Periodo de prueba")).toBe("periodo_prueba");
    expect(mapearEstadoInicialSelect("En revisión")).toBe("en_revision");
    expect(mapearEstadoInicialSelect("Por revisar")).toBe("en_revision");
    expect(mapearEstadoInicialSelect(null)).toBe("en_revision");
  });
});

describe("calcularEstadoVisualFormulario - Comportamiento visual del formulario", () => {
  it("muestra el campo de comentario y punto rojo cuando se selecciona rechazada", () => {
    const ui = calcularEstadoVisualFormulario("rechazada", "Falta comprobante");
    expect(ui).toEqual({
      claseDot: "dot-rechazada",
      ocultarComentario: false,
      comentarioFinal: "Falta comprobante",
      textoContador: "17 / 1,000",
    });
  });

  it("oculta y limpia el comentario y pone punto verde cuando se selecciona aprobada", () => {
    const ui = calcularEstadoVisualFormulario("aprobada", "Texto previo");
    expect(ui).toEqual({
      claseDot: "dot-aprobada",
      ocultarComentario: true,
      comentarioFinal: "",
      textoContador: "0 / 1,000",
    });
  });
});
