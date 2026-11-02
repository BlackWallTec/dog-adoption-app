package com.app.android.di

import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.repoImp.repoImpCatalogo.DogRepositoryImpl
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCatalogo.DogRepositoryInt
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Le dice a Hilt: "Estas dependencias viven tanto como la App"
object NetworkModule {
    // ====== Codigo agregado con IA =====
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            // El nivel BODY imprime: URL, cabeceras, códigos de error y el JSON CRUDO completo
            level = HttpLoggingInterceptor.Level.BODY
        }

    // Dificultad para conectar backend con Android en iOS
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 30 segundos de margen para conectar
            .readTimeout(30, TimeUnit.SECONDS) // 30 segundos para leer datos
            .writeTimeout(30, TimeUnit.SECONDS) // 30 segundos para enviar
            // ====== Inicio de código de Luis Gtz. =====
            // Este es el interceptor de registro fue añadido por Luis Gtz.
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("x-client", "mobile").build())
            }
            // ====== Fin de código de Luis Gtz. =====
            .addInterceptor(loggingInterceptor)
            .build()
    // ====== Fin Codigo agregado con IA =====

    // Excepción 1: Fabrica el motor de Retrofit configurado con la URL de Supabase
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://192.168.0.161:3000/") // backend local en Express
            .client(okHttpClient)
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


    // Providers de Perfiles
    @Provides @Singleton
    fun provideUsuarioApiService(retrofit: Retrofit): UsuarioAPIservice =
        retrofit.create(UsuarioAPIservice::class.java)

    @Provides @Singleton
    fun providePerfilesRepository(api: UsuarioAPIservice): PerfilesRepositoryInt =
        UsuarioRepositoryImpl(api)
}



