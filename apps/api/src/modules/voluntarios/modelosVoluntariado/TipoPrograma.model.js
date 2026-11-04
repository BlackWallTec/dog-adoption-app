import { supabaseAdmin } from "../../../config/supabaseAdmin.js";

const TABLA = "tipo_programa";

// Consulta a DB para obtener el listado de tipos de programa
export async function obtenerTipos() {
  const { data, error } = await supabaseAdmin
    .from("tipo_programa")
    .select("nombre,descripcion")
    .order("nombre");

  if (error) throw error;

  const tipos = data || [];
  return data || [];
}

// Verificar si el tipo ya existe (Coincidencia de nombre), en caso de no coincidir se crea uno nuevo
export async function registrarTipo(datos) {
  const nombre = datos.nombre.trim();
  const descripcion = (datos.descripcion ?? "").trim();
  const tipos = await obtenerTipos();
  const existe = tipos.some((tipo) => {
    const nombreRegistrado = String(tipo.nombre || " ").trim();
    return nombreRegistrado.toLowerCase() === nombre.toLowerCase();
  });

  if (existe) {
    const error = new Error("Ya existe un tipo con ese nombre.");
    error.status = 409;
    throw error;
  }

  const { data, error } = await supabaseAdmin
    .from(TABLA)
    .insert({ nombre: nombre, descripcion: descripcion })
    .select("nombre,descripcion")
    .single();

  if (error) throw error;
  return { nombre: data.nombre, descripcion: data.descripcion ?? "" };
}
