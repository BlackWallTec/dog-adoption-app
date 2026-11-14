import { supabaseAdmin } from "../../../../config/supabaseClient.js";

const TABLA = "programa";

export function combinarFechaHora(fecha, hora) {
  return `${fecha} ${hora}:00`;
}

function separarFechaHora(fechaHora) {
  const texto = String(fechaHora || "");
  return {
    fecha: texto.slice(0, 10),
    hora: texto.slice(11, 16),
  };
}

// Se normalizan los nombres en los cuales se utilizan en el FrontEnd
export function normalizarPrograma(r) {
  const inicio = separarFechaHora(r.fecha_inicio);
  const fin = separarFechaHora(r.fecha_fin);
  return {
    idPrograma: r.id_programa,
    nombre: r.nombre,
    tipo: r.tipo,
    descripcion: r.descripcion,
    fechaInicio: inicio.fecha,
    horaInicio: inicio.hora,
    fechaFin: fin.fecha,
    horaFin: fin.hora,
    ubicacion: r.ubicacion,
    cupoMaximo: r.cupo_maximo,
    estado: String(r.estado_del_programa || "").trim(),
  };
}

// Se hace una consulta de las jornadas, se devuelve unicamente en caso en que dos jornadas coincidan en fecha y hora
export async function obtenerEmpalmes(datos) {
  const inicioNuevo = combinarFechaHora(datos.fechaInicio, datos.horaInicio);
  const finNuevo = combinarFechaHora(datos.fechaFin, datos.horaFin);

  const { data, error } = await supabaseAdmin
    .from(TABLA)
    .select("*")
    .lt("fecha_inicio", finNuevo)
    .gt("fecha_fin", inicioNuevo);

  if (error) throw error;

  const programas = data || [];
  const empalmes = [];
  for (const programa of programas) {
    const estado = String(programa.estado_del_programa || "")
      .trim()
      .toLowerCase();

    if (estado === "cancelado" || estado === "cancelada") {
      continue;
    }

    const jornada = normalizarPrograma(programa);

    empalmes.push(jornada);
  }
  return empalmes;
}

// Se arma el registro, se inserta en supabase y devuelve la jornada creada
export async function insertarPrograma(datos, idAdmin, idPrograma) {
  const registro = {
    id_programa: idPrograma,
    tipo: datos.tipo,
    id_admin: idAdmin,
    nombre: datos.nombre,
    estado_del_programa: "Disponible",
    descripcion: datos.descripcion,
    fecha_inicio: combinarFechaHora(datos.fechaInicio, datos.horaInicio),
    fecha_fin: combinarFechaHora(datos.fechaFin, datos.horaFin),
    ubicacion: datos.ubicacion,
    cupo_maximo: datos.cupoMaximo,
  };

  const { data, error } = await supabaseAdmin.from(TABLA).insert(registro).select("*").single();

  if (error) throw error;
  return { programa: normalizarPrograma(data) };
}
