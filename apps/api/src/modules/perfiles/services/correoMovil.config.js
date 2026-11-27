// Módulo sin dependencias para que los modelos lo consulten sin importar el servicio completo.
// "local" guarda el estado en un archivo (sólo desarrollo y pruebas); "supabase" lo comparte
// entre instancias de Vercel. Cualquier otro valor conserva el login sin confirmación de correo.
const MODOS = new Set(["local", "supabase"]);

export const modoCorreoMovil = () => process.env.CORREO_MOVIL_MODO;
export const correoMovilHabilitado = () => MODOS.has(modoCorreoMovil());
