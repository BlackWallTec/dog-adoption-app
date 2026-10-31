import db from "../../../../config/supabaseClient.js";

// Se parametrizan las consultas, para que se traten como dato y no
// como instrucción.

export default class Usuario {
  static async fetchByEmail(email) {
    const query = `
    SELECT
        u.id_usuario,
        u.contrasena,
        u.rol,
    FROM usuarios u
    INNER JOIN datos_confidenciales dc ON dc.id_usuario = u.id_usuario
    WHERE u.correo_electronico = $1
    `;

    return db.query(query, [email]);
  }
}
