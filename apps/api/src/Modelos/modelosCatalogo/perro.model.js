import { supabase } from "../../config/supabaseClient.js";

export const getPerrosAdminModel = async () => {
  const { data, error } = await supabase
    .from("perro")
    .select(`
        id_perro,
        nombre,
        sexo,
        edad_aprox,
        tamano_medida,
        descripcion,
        esterilizado,
        vacunado,
        estado,
        fecha_ingreso,
        raza (
            id_raza,
            nombre
        ),
        tamano (
            id_tamano,
            nombre
        )
    `)
    .order("fecha_ingreso", { ascending: false });

  if (error) {
    throw new Error(`Error en la consulta de perros: ${error.message}`);
  }

  return data;
};
