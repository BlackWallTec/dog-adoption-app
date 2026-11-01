import { createClient } from "@supabase/supabase-js";
import "dotenv/config";

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_ANON_KEY;

if (!supabaseUrl || !supabaseKey) {
  throw new Error("Missing Supabase environment variables");
}

export const supabase = createClient(supabaseUrl, supabaseKey);

/* ESTO SÓLO LO AÑÁDÍ PARA RECORDAR EN CASO DE QUE
NO HAYA UN USUARIO CREADO PARA ESTO Y QUE SÍ SE NECESITE
POR EJEMPLO SI HAY GRANTS/POLICIES QUE LO AMERITEN

const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!serviceKey) throw new Error("Missing SUPABASE_SERVICE_ROLE_KEY");

export const supabaseAdmin = createClient(supabaseUrl, serviceKey, {
  auth: { persistSession: false },
});
*/
