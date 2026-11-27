const MODOS_HABILITADOS = new Set(["local", "supabase"]);

export const modoCorreoMovil = () => process.env.CORREO_MOVIL_MODO;
export const correoMovilHabilitado = () => MODOS_HABILITADOS.has(modoCorreoMovil());
