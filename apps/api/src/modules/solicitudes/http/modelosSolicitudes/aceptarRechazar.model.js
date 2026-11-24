import { supabaseAdmin } from "../../../../config/supabaseClient.js";

//Function to get the data
export async function getData(id) {
  const { data, error } = await supabaseAdmin
    .from("solicitud_adopcion")
    .select(`
      id_solicitud,
      fecha_solicitud,
      motivo,
      tipo_vivienda,
      otras_mascotas,
      integrantes_hogar,
      comentario_admin,
      fecha_resolucion,
      usuario:usuario!solicitud_adopcion_id_usuario_fkey (
        nombre,
        apellidos,
        cliente (
          datos_confidenciales ( correo )
        )
      ),
      perro:perro!solicitud_adopcion_id_perro_fkey (
        id_perro,
        nombre,
        estado
      ),
      estado:estado_solicitud!solicitud_adopcion_id_estado_fkey (
        descripcion
      )
    `)
    .eq("id_solicitud", id)
    .single();

  if (error) throw error;

  const estadoRaw = String(data.estado?.descripcion || "").toLowerCase();
  let estado = "por_revisar";
  if (estadoRaw.includes("aprob")) estado = "aprobada";
  else if (estadoRaw.includes("rechaz")) estado = "rechazada";
  else if (estadoRaw.includes("prueba")) estado = "periodo_prueba";
  else if (estadoRaw.includes("revis")) estado = "en_revision";

  return {
    id: data.id_solicitud,
    folio: `QA-${data.id_solicitud.slice(0, 8).toUpperCase()}`,
    estado,
    motivo: data.motivo || "",
    tipo_vivienda: data.tipo_vivienda || "",
    comentario_admin: data.comentario_admin || "",
    adoptante: {
      nombre: data.usuario?.nombre || "",
      apellidos: data.usuario?.apellidos || "",
      email: data.usuario?.cliente?.datos_confidenciales?.correo || "",
    },
    perro: {
      id: data.perro?.id_perro,
      nombre: data.perro?.nombre || "",
      estado: data.perro?.estado || "",
    },
  };
}

//Function to update the databse with the new values
export async function resolverSolicitud(solicitudId, state, comment, puedeEditar = false) {
  const { data: estadosCatalogo, error: errorEstados } = await supabaseAdmin
    .from("estado_solicitud")
    .select("id_estado, descripcion");

  if (errorEstados) throw errorEstados;

  //Normalizes the comments and the options chosen
  const buscarPorTerminos = (terminos) =>
    estadosCatalogo.find((item) => {
      const desc = String(item.descripcion || "")
        .normalize("NFD")
        .replace(/\p{Diacritic}/gu, "") //takes of accents
        .toLowerCase();
      return terminos.some((termino) => desc.includes(termino));
    });

  let estadoDestino;
  if (state === "aprobada") estadoDestino = buscarPorTerminos(["aprob"]);
  else if (state === "rechazada") estadoDestino = buscarPorTerminos(["rechaz"]);
  else if (state === "periodo_prueba") estadoDestino = buscarPorTerminos(["prueba"]);
  else if (state === "en_revision") estadoDestino = buscarPorTerminos(["revision", "revisar"]);

  if (!estadoDestino) {
    throw new Error(`No se encontró un estado válido en estado_solicitud para "${state}"`);
  }
  //Updates the request
  const camposActualizar = {
    id_estado: estadoDestino.id_estado,
    comentario_admin: state === "rechazada" ? comment : null,
    fecha_resolucion: ["en_revision", "periodo_prueba"].includes(state)
      ? null
      : new Date().toISOString(),
    puede_editar: puedeEditar === true,
  };

  const { data, error } = await supabaseAdmin
    .from("solicitud_adopcion")
    .update(camposActualizar)
    .eq("id_solicitud", solicitudId)
    .select("id_solicitud, id_estado, id_perro, comentario_admin, fecha_resolucion, puede_editar")
    .single();

  if (error) throw error;

  if (data?.id_perro && state === "aprobada") {
    await supabaseAdmin.from("perro").update({ estado: "Adoptado" }).eq("id_perro", data.id_perro);
  }

  return data;
}
