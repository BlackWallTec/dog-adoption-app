package com.app.android.di

import com.app.android.BuildConfig
import com.app.android.data.session.PerfilesAuthInterceptor
import com.app.android.domain.repoInterface.repoIntPerfiles.AlmacenSesion

import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService

import com.app.android.data.dataSource.dataSourcesSolicitudes.SolicitudApiService
import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaApiService
import com.app.android.data.repoImp.repoImpCatalogo.DogRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryImpl
import com.app.android.data.repoImp.repoImpVoluntariado.ProgramaRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCatalogo.DogRepositoryInt
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
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
import com.google.gson.Gson
import java.time.Clock


@Module
@InstallIn(SingletonComponent::class) // Le dice a Hilt: "Estas dependencias viven tanto como la App"
object NetworkModule {
    // ====== Codigo agregado con IA =====
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC else HttpLoggingInterceptor.Level.NONE
        }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()


    // Dificultad para conectar backend con Android en iOS
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor, authInterceptor: PerfilesAuthInterceptor): OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // 30 segundos de margen para conectar
            .readTimeout(30, TimeUnit.SECONDS) // 30 segundos para leer datos
            .writeTimeout(30, TimeUnit.SECONDS) // 30 segundos para enviar
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    // ====== Fin Codigo agregado con IA =====

    // Excepción 1: Fabrica el motor de Retrofit configurado con la URL de Supabase
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
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
    fun providePerfilesRepository(api: UsuarioAPIservice, almacen: AlmacenSesion): PerfilesRepositoryInt =
        UsuarioRepositoryImpl(api, almacen)

    @Provides
    @Singleton
    fun provideSolicitudApiService(retrofit: Retrofit): SolicitudApiService = retrofit.create(SolicitudApiService::class.java)

    // 4. Repositorio de Solicitudes.
    // Para volver al fake (sin backend): return SolicitudRepositoryFake()
    @Provides
    @Singleton
    fun provideSolicitudRepository(apiService: SolicitudApiService): SolicitudRepositoryInt =
        SolicitudRepositoryImpl(apiService)

    // Borrador de la solicitud (en memoria, compartido por las 6 pantallas).
    // @Singleton es obligatory: sin el, cada pantalla tendria su propio borrador vacio.
    @Provides
    @Singleton
    fun provideBorradorRepository(): BorradorRepositoryInt = BorradorRepositoryImpl()

    @Provides
    @Singleton
    fun provideProgramaApiService(retrofit: Retrofit): ProgramaApiService =
        retrofit.create(ProgramaApiService::class.java)

    @Provides
    @Singleton
    fun provideProgramaRepository(apiService: ProgramaApiService): ProgramaRepository =
        ProgramaRepositoryImpl(apiService)

}
