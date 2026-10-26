// Referencias a elementos del DOM
const tablaBody = document.getElementById("tabla-perros-body");
const counterSubtitle = document.getElementById("perros-counter-subtitle");
const paginationInfo = document.getElementById("pagination-info");

// Estado ocal de la vista
let catalogoPerros = [];

// Función para obtener la clase de badge según el estado
function obtenerClaseBadge(estado) {
  const normalizado = (estado || "").toLowerCase().trim();
  if (normalizado === "disponible") return "badge-disponible";
  if (normalizado === "adoptado" || normalizado === "adoptada") return "badge-adoptado";
  return "badge-tratamiento";
}

function textoRelacion(valor, fallback) {
  if (!valor) return fallback;
  if (typeof valor === "object") return valor.nombre || fallback;
  return valor;
}

function textoEdad(perro) {
  const edad = perro.edad ?? perro.edad_aprox;
  if (edad === null || edad === undefined || edad === "") return "No especificada";
  if (typeof edad === "number" || /^\d+(\.\d+)?$/.test(String(edad).trim())) {
    return `${edad} años`;
  }
  return String(edad);
}

// Función para renderizar la tabla de perros
function renderizarTabla(perros) {
  if (!tablaBody) return;

  if (perros.length === 0) {
    tablaBody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align: center; padding: 32px; color: var(--md-sys-color-on-surface-variant);">
                No se encontraron perros registrados.
                </td>
            </tr>
        `;
    return;
  }

  tablaBody.innerHTML = perros
    .map((perro) => {
      // Foto fallback en caso de no tener URL cargada
      const fotoUrl = perro.foto_url || perro.foto || "/favicon.svg";
      const raza = textoRelacion(perro.raza, "Mestizo");
      const edadTexto = textoEdad(perro);
      const tamano = textoRelacion(perro.tamano || perro.tamanio, "Mediano");
      const estado = perro.estado || "Disponible";
      const badgeClase = obtenerClaseBadge(estado);
      const id = perro.id ?? perro.id_perro ?? "";

      return `
      <tr data-id="${id}">
        <td>
          <img src="${fotoUrl}" alt="${perro.nombre}" class="dog-photo" onerror="this.src='/favicon.svg'">
        </td>
        <td>
          <div class="dog-name-cell">
            <span class="dog-name">${perro.nombre}</span>
            <span class="dog-meta">${raza}</span>
          </div>
        </td>
        <td>${edadTexto}</td>
        <td>${tamano}</td>
        <td>
          <span class="badge-estado ${badgeClase}">${estado}</span>
        </td>
        <td>
          <div class="actions-cell">
            <button type="button" class="action-btn action-edit" title="Editar">
              <span class="material-symbols-rounded">edit</span>
              Editar
            </button>
            <button type="button" class="action-btn action-delete" title="Eliminar">
              <span class="material-symbols-rounded">delete</span>
              Eliminar
            </button>
          </div>
        </td>
      </tr>
    `;
    })
    .join("");
}

async function cargarPerros() {
  try {
    const respuesta = await fetch("/api/catalogo/admin/perros");

    const contentType = respuesta.headers.get("content-type") || "";
    if (!respuesta.ok || !contentType.includes("application/json")) {
      throw new Error(`La ruta /api/catalogo/admin/perros no devolvió JSON (${respuesta.status}).`);
    }

    const data = await respuesta.json();
    catalogoPerros = Array.isArray(data) ? data : data.data || data.perros || [];

    // Actualizar contador en el encabezado y paginación
    const total = catalogoPerros.length;
    if (counterSubtitle) {
      counterSubtitle.textContent = `Mostrando ${total} perro${total === 1 ? "" : "s"} bajo cuidado`;
    }
    if (paginationInfo) {
      paginationInfo.textContent =
        total === 0 ? "Mostrando 0 de 0 perros" : `Mostrando 1-${total} de ${total} perros`;
    }

    renderizarTabla(catalogoPerros);
  } catch (error) {
    console.error("Error al cargar los perros:", error);
    if (tablaBody) {
      tablaBody.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align: center; padding: 28px; color: var(--md-sys-color-error);">
                        Error al conectar con la base de datos. Asegúrate de que el servidor backend esté corriendo.
                    </td>
                </tr>
            `;
    }
  }
}

// Carga inicial al montar la página
document.addEventListener("DOMContentLoaded", cargarPerros);
