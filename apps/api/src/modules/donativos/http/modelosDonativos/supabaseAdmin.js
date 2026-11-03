import { createClient } from "@supabase/supabase-js";
import "dotenv/config";

// datos_confidenciales tiene RLS, con la llave anon no se ve nada.
// Este cliente usa la llave de servidor y SOLO se usa dentro del backend.
const supabaseUrl = process.env.SUPABASE_URL;
const serviceKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !serviceKey) {
  throw new Error("Falta SUPABASE_SERVICE_ROLE_KEY en el .env");
}

export const supabaseAdmin = createClient(supabaseUrl, serviceKey, {
  auth: { persistSession: false },
});
