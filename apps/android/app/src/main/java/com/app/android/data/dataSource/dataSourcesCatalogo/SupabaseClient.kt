package com.app.android.data.dataSource.dataSourcesCatalogo

import android.net.http.HttpResponseCache.install

val supabase=createSupabaseClient(
    supabaseUrl= "https://nwqfhcvlwntkqkraodoq.supabase.co/rest/v1/"
    supabaseKey="sb_publishable_Z4fK8huMLh4i1i_qLGg4zg_HrrPoy5O"
){
    install(Postgrest)
}