package com.app.android.di

import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
import com.app.android.data.repoImp.repoImpCatalogo.DogRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCatalogo.DogRepositoryInt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Le dice a Hilt: "Estas dependencias viven tanto como la App"
object NetworkModule {
    // Excepción 1: Fabrica el motor de Retrofit configurado con la URL de Supabase
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://10.0.2.2:3000/") // backend local en Express
            .addConverterFactory(GsonConverterFactory.create()) // El transformador JSON de Google
            .build()

    // Excepción 2: Fabricamos el DataSource automático usando el motor de Retrofit
    @Provides
    @Singleton
    fun provideDogApiService(retrofit: Retrofit): DogApiService = retrofit.create(DogApiService::class.java)

    // 3. Enlazamos la Interfaz con la Implementación Real del Repositorio
    @Provides
    @Singleton
    fun provideDogRepository(apiService: DogApiService): DogRepositoryInt {
        // Hilt inyectará el ApiService aquí dentro de tu implementación automáticamente
        return DogRepositoryImpl(apiService)
    }
}
