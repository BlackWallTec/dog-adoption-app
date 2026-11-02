import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const supabase = createClient(
  "https://nwqfhcvlwntkqkraodoq.supabase.co",
  "sb_publishable_Z4fK8huMLh4i1i_qLGg4zg_HrrPoy5O",
);

export async function iniciarSesionDonativos(email, password) {
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password,
  });

  if (error) {
    throw new Error(error.message);
  }

  return data.session;
}

export async function obtenerTokenDonativos() {
  const { data, error } = await supabase.auth.getSession();

  if (error) {
    throw new Error(error.message);
  }

  return data.session?.access_token ?? null;
}
