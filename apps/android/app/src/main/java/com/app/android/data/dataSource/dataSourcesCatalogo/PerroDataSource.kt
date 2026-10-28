package com.app.android.data.dataSource.dataSourcesCatalogo

class PerroDataSource{
    suspend fun getPerros():  List<PerroDto>=
    supabase.from("perro").select().decodeList<PerroDto>()
}