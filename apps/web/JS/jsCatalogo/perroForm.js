// Formulario de ficha de perro, pensado para reutilizarse en HU19A (alta) y HU19C (edición).
// No hace peticiones: solo pinta, lee y valida. Quien lo use decide a qué endpoint mandar.

export function escaparHtml(texto) {
  return String(texto ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

const SEXOS = ["Macho", "Hembra"];
export const ESTADOS = ["Disponible", "En Tratamiento", "Adoptado"];

function opciones(lista, idKey, seleccionado) {
  return lista
    .map(
      (o) =>
        `<option value="${escaparHtml(o[idKey])}" ${String(o[idKey]) === String(seleccionado) ? "selected" : ""}>${escaparHtml(o.nombre)}</option>`,
    )
    .join("");
}

// Ids que el perro ya tiene en una relación (ej. perro_tiene_caracter -> id_caracter).
export function idsDeRelacion(perro, relacion, columna) {
  const filas = Array.isArray(perro?.[relacion]) ? perro[relacion] : [];
  return filas.map((fila) => String(fila?.[columna] ?? "")).filter(Boolean);
}

// Dos listas de ids son iguales si tienen los mismos elementos, sin importar el orden.
export function mismoConjunto(a, b) {
  const x = new Set(a.map(String));
  const y = new Set(b.map(String));
  return x.size === y.size && [...x].every((id) => y.has(id));
}

// Insignias seleccionables (el mismo componente .badge-item del alta). Son botones para que
// funcionen con teclado; `.active` marca las elegidas.
function insignias(lista, idKey, textoKey, activos) {
  if (lista.length === 0) return `<span class="field-hint">No hay opciones registradas.</span>`;
  const elegidos = new Set(activos.map(String));
  return lista
    .map((o) => {
      const activo = elegidos.has(String(o[idKey]));
      return `<button type="button" class="badge-item ${activo ? "active" : ""}" data-id="${escaparHtml(o[idKey])}" aria-pressed="${activo}">${escaparHtml(o[textoKey])}</button>`;
    })
    .join("");
}

function conectarInsignias(contenedor) {
  contenedor.querySelectorAll(".badge-item").forEach((insignia) => {
    insignia.addEventListener("click", () => {
      const activo = insignia.classList.toggle("active");
      insignia.setAttribute("aria-pressed", String(activo));
    });
  });
}

// Pinta el formulario dentro de `contenedor` con los datos de `perro` (o vacío para un alta).
// `caracteristicas` ({ id_caracter, nombre }) y `cuidados` ({ id_cuidado, descripcion }) son los
// catálogos completos; las que el perro ya tiene salen marcadas. La galería de fotos se monta
// aparte, en el elemento [data-galeria] que deja este formulario.
export function pintarFormulario(
  contenedor,
  { perro = {}, razas, tamanos, caracteristicas = [], cuidados = [] },
) {
  // La BD guarda sexo como CHAR con relleno ("Macho     "): se limpia para no duplicar opciones.
  const sexoActual = (perro.sexo ?? "").trim();
  const sexos = !sexoActual || SEXOS.includes(sexoActual) ? SEXOS : [...SEXOS, sexoActual];
  const estadoActual = (perro.estado ?? "").trim();
  const estados =
    !estadoActual || ESTADOS.includes(estadoActual) ? ESTADOS : [...ESTADOS, estadoActual];
  contenedor.innerHTML = `
    <div class="form-grid">
      <label class="field">
        <span class="field-label">Nombre *</span>
        <input name="nombre" type="text" maxlength="60" value="${escaparHtml(perro.nombre)}" />
        <span class="field-error" data-error-for="nombre"></span>
      </label>
      <label class="field">
        <span class="field-label">Sexo *</span>
        <select name="sexo">
          <option value="">Selecciona…</option>
          ${sexos.map((s) => `<option value="${escaparHtml(s)}" ${s === sexoActual ? "selected" : ""}>${escaparHtml(s)}</option>`).join("")}
        </select>
        <span class="field-error" data-error-for="sexo"></span>
      </label>
      <label class="field">
        <span class="field-label">Edad aproximada (años) *</span>
        <input name="edad_aprox" type="number" min="0" max="30" step="1" value="${escaparHtml(perro.edad_aprox)}" />
        <span class="field-error" data-error-for="edad_aprox"></span>
      </label>
      <label class="field">
        <span class="field-label">Raza *</span>
        <select name="id_raza">
          <option value="">Selecciona…</option>
          ${opciones(razas, "id_raza", perro.raza?.id_raza)}
        </select>
        <span class="field-error" data-error-for="id_raza"></span>
      </label>
      <label class="field">
        <span class="field-label">Tamaño *</span>
        <select name="id_tamano">
          <option value="">Selecciona…</option>
          ${opciones(tamanos, "id_tamano", perro.tamano?.id_tamano)}
        </select>
        <span class="field-hint" data-hint-for="id_tamano"></span>
        <span class="field-error" data-error-for="id_tamano"></span>
      </label>
      <label class="field">
        <span class="field-label">Altura (cm)</span>
        <input name="tamano_medida" type="number" min="0" max="200" step="0.1" value="${escaparHtml(perro.tamano_medida)}" />
        <span class="field-error" data-error-for="tamano_medida"></span>
      </label>
      <label class="field">
        <span class="field-label">Estado *</span>
        <select name="estado">
          <option value="">Selecciona…</option>
          ${estados.map((s) => `<option value="${escaparHtml(s)}" ${s === estadoActual ? "selected" : ""}>${escaparHtml(s)}</option>`).join("")}
        </select>
        <span class="field-error" data-error-for="estado"></span>
      </label>
      <label class="field field-wide">
        <span class="field-label">Descripción</span>
        <textarea name="descripcion" rows="4" maxlength="1000">${escaparHtml(perro.descripcion)}</textarea>
        <span class="field-error" data-error-for="descripcion"></span>
      </label>
      <div class="field-checks field-wide">
        <label class="check"><input name="esterilizado" type="checkbox" ${perro.esterilizado ? "checked" : ""} /> Esterilizado</label>
        <label class="check"><input name="vacunado" type="checkbox" ${perro.vacunado ? "checked" : ""} /> Vacunado</label>
      </div>
      <div class="field field-wide">
        <span class="field-label">Características</span>
        <div class="badge-selector" data-grupo="caracteres">
          ${insignias(caracteristicas, "id_caracter", "nombre", idsDeRelacion(perro, "perro_tiene_caracter", "id_caracter"))}
        </div>
        <span class="field-error" data-error-for="caracteres"></span>
      </div>
      <div class="field field-wide">
        <span class="field-label">Cuidados especiales</span>
        <div class="badge-selector" data-grupo="cuidados">
          ${insignias(cuidados, "id_cuidado", "descripcion", idsDeRelacion(perro, "perro_tiene_cuidado_especial", "id_cuidado"))}
        </div>
        <span class="field-error" data-error-for="cuidados"></span>
      </div>
      <div class="field field-wide">
        <span class="field-label">Fotos y videos</span>
        <div data-galeria></div>
        <span class="field-error" data-error-for="multimedia_agregar"></span>
      </div>
    </div>
  `;
  conectarInsignias(contenedor);
}

// Lee el formulario como objeto con los nombres de columna del API.
export function leerFormulario(contenedor) {
  const v = (n) => contenedor.querySelector(`[name="${n}"]`);
  const elegidos = (grupo) =>
    [...contenedor.querySelectorAll(`[data-grupo="${grupo}"] .badge-item.active`)].map(
      (insignia) => insignia.dataset.id,
    );
  return {
    nombre: v("nombre").value,
    sexo: v("sexo").value,
    edad_aprox: v("edad_aprox").value,
    id_raza: v("id_raza").value,
    id_tamano: v("id_tamano").value,
    tamano_medida: v("tamano_medida").value,
    estado: v("estado").value,
    descripcion: v("descripcion").value,
    esterilizado: v("esterilizado").checked,
    vacunado: v("vacunado").checked,
    caracteres: elegidos("caracteres"),
    cuidados: elegidos("cuidados"),
  };
}

// Mismas reglas que el servidor, para avisar antes de enviar. El servidor vuelve a validar.
export function validarFormulario(d) {
  const e = {};
  if (!d.nombre.trim()) e.nombre = "El nombre es obligatorio.";
  if (!d.sexo) e.sexo = "Selecciona el sexo.";
  if (!d.estado) e.estado = "Selecciona el estado.";
  const edad = d.edad_aprox === "" ? NaN : Number(d.edad_aprox);
  if (!Number.isInteger(edad) || edad < 0 || edad > 30) {
    e.edad_aprox = "Captura un entero entre 0 y 30.";
  }
  if (!d.id_raza) e.id_raza = "Selecciona la raza.";
  if (!d.id_tamano) e.id_tamano = "Selecciona el tamaño.";
  if (d.tamano_medida !== "" && !(Number(d.tamano_medida) >= 0 && Number(d.tamano_medida) <= 200)) {
    e.tamano_medida = "La altura debe ser un número entre 0 y 200 cm.";
  }
  return e;
}

// Devuelve solo los campos que cambiaron respecto a la ficha original.
export function cambiosRespectoA(perro, d) {
  const original = {
    nombre: perro.nombre ?? "",
    sexo: perro.sexo ?? "",
    edad_aprox: String(perro.edad_aprox ?? ""),
    id_raza: String(perro.raza?.id_raza ?? ""),
    id_tamano: String(perro.tamano?.id_tamano ?? ""),
    tamano_medida: String(perro.tamano_medida ?? ""),
    estado: perro.estado ?? "",
    descripcion: perro.descripcion ?? "",
    esterilizado: Boolean(perro.esterilizado),
    vacunado: Boolean(perro.vacunado),
  };
  const cambios = {};
  for (const clave of Object.keys(original)) {
    const nuevo = typeof d[clave] === "string" ? d[clave].trim() : d[clave];
    const viejo = typeof original[clave] === "string" ? original[clave].trim() : original[clave];
    if (nuevo !== viejo) cambios[clave] = d[clave];
  }
  return cambios;
}

// Características y cuidados que cambiaron. Si cambió el conjunto, se manda la lista COMPLETA
// de ids elegidos (el servidor deja al perro exactamente con esa lista).
export function cambiosEnRelaciones(perro, d) {
  const cambios = {};
  if (
    Array.isArray(d.caracteres) &&
    !mismoConjunto(idsDeRelacion(perro, "perro_tiene_caracter", "id_caracter"), d.caracteres)
  ) {
    cambios.caracteres = d.caracteres;
  }
  if (
    Array.isArray(d.cuidados) &&
    !mismoConjunto(idsDeRelacion(perro, "perro_tiene_cuidado_especial", "id_cuidado"), d.cuidados)
  ) {
    cambios.cuidados = d.cuidados;
  }
  return cambios;
}

export function mostrarErrores(contenedor, errores = {}) {
  contenedor.querySelectorAll("[data-error-for]").forEach((el) => {
    const msg = errores[el.dataset.errorFor] || "";
    el.textContent = msg;
    el.closest(".field")?.classList.toggle("has-error", Boolean(msg));
  });
  // Lleva el foco al primer campo con error
  const primero = contenedor.querySelector(
    ".has-error input, .has-error select, .has-error textarea",
  );
  primero?.focus();
}

// ---- Tamaño según la altura (reglas de la tabla `tamano`) ----
// Chico: menor a 45 cm · Mediano: de 45 a 75 cm · Grande: mayor a 75 cm
export function nombreTamanoPorAltura(altura) {
  const vacio = altura === null || altura === undefined || String(altura).trim() === "";
  const h = vacio ? NaN : Number(altura);
  if (!Number.isFinite(h) || h < 0) return null;
  if (h < 45) return "Chico";
  if (h <= 75) return "Mediano";
  return "Grande";
}

// Devuelve el id del tamaño que corresponde a la altura, o null si no se puede decidir.
export function tamanoPorAltura(altura, tamanos) {
  const nombre = nombreTamanoPorAltura(altura);
  if (!nombre) return null;
  const t = tamanos.find((x) => String(x.nombre).trim().toLowerCase() === nombre.toLowerCase());
  return t ? t.id_tamano : null;
}

// Conecta altura y tamaño: al escribir la altura se elige el tamaño solo (se puede cambiar a mano)
// y, si el tamaño elegido no cuadra con la altura, se muestra un aviso (no bloquea el guardado).
export function conectarAlturaYTamano(contenedor, tamanos) {
  const altura = contenedor.querySelector('[name="tamano_medida"]');
  const tamano = contenedor.querySelector('[name="id_tamano"]');
  const aviso = contenedor.querySelector('[data-hint-for="id_tamano"]');

  const actualizarAviso = (autoElegido) => {
    const esperado = tamanoPorAltura(altura.value, tamanos);
    const nombreEsperado = nombreTamanoPorAltura(altura.value);
    if (!esperado) aviso.textContent = "";
    else if (autoElegido)
      aviso.textContent = `Tamaño elegido según la altura (${nombreEsperado}). Puedes cambiarlo.`;
    else if (tamano.value !== String(esperado)) {
      aviso.textContent = `Ojo: por la altura le corresponde ${nombreEsperado}.`;
    } else aviso.textContent = "";
  };

  altura.addEventListener("input", () => {
    const esperado = tamanoPorAltura(altura.value, tamanos);
    if (esperado) tamano.value = String(esperado);
    actualizarAviso(Boolean(esperado));
  });
  tamano.addEventListener("change", () => actualizarAviso(false));
  actualizarAviso(false);
}
