package com.app.android.di

import android.content.Context
import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
import com.app.android.data.dataSource.dataSourcesSolicitudes.DocumentApi
import com.app.android.data.repoImp.repoImpCatalogo.DogRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.DocumentRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCatalogo.DogRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.DocumentRepository
import com.app.android.data.dataSource.dataSourcesSolicitudes.SolicitudApiService
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryImpl
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
            .addInterceptor(loggingInterceptor)
            .build()
    // ====== Fin Codigo agregado con IA =====

    // Excepción 1: Fabrica el motor de Retrofit configurado con la URL de Supabase
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit
            .Builder()
            .baseUrl("http://192.168.0.143:3000/") // backend local en Express DA MUCHOS MERGE CONF (buscar con .env mejor)
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
    @Provides
    @Singleton
    fun provideDocumentApi(retrofit: Retrofit): DocumentApi {
        return retrofit.create(DocumentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDocumentRepository(
        //we get the api
        apiService: DocumentApi,
        //we also get the context of the hardware to open: photos
        @ApplicationContext context: Context
    ): DocumentRepository {
        //we call the repository implementation with the api service and context
        return DocumentRepositoryImpl(apiService, context)
    }

    @Provides
    @Singleton
    fun provideSolicitudApiService(retrofit: Retrofit): SolicitudApiService = retrofit.create(SolicitudApiService::class.java)

    // 4. Repositorio de Solicitudes.
    // Para volver al fake (sin backend): return SolicitudRepositoryFake()
    @Provides
    @Singleton
    fun provideSolicitudRepository(apiService: SolicitudApiService): SolicitudRepositoryInt = SolicitudRepositoryImpl(apiService)
}
