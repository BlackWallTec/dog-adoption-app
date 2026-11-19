import postgres from "postgres";
import "dotenv/config";

/*
Conexión directa a Postgres con el rol signup_service (sólo hace
INSERT en las tablas del registro).

ÚNICAMENTE se usa en el registro público de Clientes.
*/

let sql = null;

export function getSqlSignup() {
  if (sql) return sql;

  const url = process.env.SIGNUP_DATABASE_URL;
  if (!url) throw new Error("Missing SIGNUP_DATABASE_URL");

  sql = postgres(url, {
    // Vercel es serverless:
    // cada instancia abre pocas conexiones y el pooler de Supabase
    // hace el resto.
    max: 1,
    // Obligatorio con el pooler en modo transacción (puerto 6543)
    prepare: false,
    ssl: "require",
    idle_timeout: 20,
    connect_timeout: 10,
  });

  return sql;
}
