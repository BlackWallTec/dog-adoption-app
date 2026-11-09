package com.app.android.data.dataSource.dataSourcesCatalogo
/* Ya no se usa
=======
>>>>>>> develop

import  retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import jakarta.inject.Inject

class PerroDataSource @Inject constructor() {
    private val api=Retrofit.Builder()
<<<<<<< HEAD
        .baseUrl("http://192.168.1.66:3000/") ///
=======
        .baseUrl("http://10.25.90.151:3000/") ///
>>>>>>> develop
        .addConverterFactory(GsonConverterFactory.create())     //
        .build()
        .create(PerroApi::class.java)       //
    suspend fun getPerros(): List<PerroDto> = api.getPerros().data
<<<<<<< HEAD
}
*/

