import { createClient } from "@supabase/supabase-js";
import "dotenv/config";

let cliente;
export function getSupabaseAdmin() {
  if (!cliente) {
    const url = process.env.SUPABASE_URL;
    const key = process.env.SUPABASE_SERVICE_ROLE_KEY;
    if (!url || !key) {
      throw new Error("Falta SUPABASE_SERVICE_ROLE_KEY en el .env");
    }
    cliente = createClient(url, key, {
      auth: { persistSession: false, autoRefreshToken: false },
    });
  }
  return cliente;
}
