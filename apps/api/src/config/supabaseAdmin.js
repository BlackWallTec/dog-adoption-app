import { createClient } from "@supabase/supabase-js";
import "dotenv/config";

const supabaseUrl = process.env.SUPABASE_URL;
const supabaseSecretKey = process.env.SUPABASE_SECRET_KEY;

if (!supabaseUrl || !supabaseSecretKey) {
  throw new Error("Missing Supabase admin environment variables");
}

// Cliente con la clave SECRETA: ignora RLS. Solo se usa dentro del servidor.
export const supabaseAdmin = createClient(supabaseUrl, supabaseSecretKey, {
  auth: { persistSession: false },
});
