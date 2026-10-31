package com.app.android.data.dataSource.dataSourcesCatalogo

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import jakarta.inject.Inject

class PerroDataSource @Inject constructor() {
    private val api=Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5555/") ///
        .addConverterFactory(GsonConverterFactory.create())     //
        .build()
        .create(PerroApi::class.java)       //
    suspend fun getPerros(): List<PerroDto> = api.getPerros().data
}