package com.app.android.di

import com.app.android.data.dataSource.dataSourcesDonativos.DonativosApiService
import com.app.android.data.repoImp.repoImpDonativos.DatosFiscalesRepositoryImpl
import com.app.android.data.repoImp.repoImpDonativos.DonativosRepositoryImpl
import com.app.android.domain.repoInterface.repoIntDonativos.DatosFiscalesRepositoryInt
import com.app.android.domain.repoInterface.repoIntDonativos.DonativosRepositoryInt
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// Dependencias de Donativos aparte del NetworkModule para no chocar con los demás módulos.
// Usa el mismo Retrofit que ya fabrica el NetworkModule.
@Module
@InstallIn(SingletonComponent::class)
object DonativosModule {
    @Provides
    @Singleton
    fun provideDonativosApiService(retrofit: Retrofit): DonativosApiService = retrofit.create(DonativosApiService::class.java)

    @Provides
    @Singleton
    fun provideDatosFiscalesRepository(apiService: DonativosApiService): DatosFiscalesRepositoryInt =
        DatosFiscalesRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideDonativosRepository(apiService: DonativosApiService): DonativosRepositoryInt = DonativosRepositoryImpl(apiService)
}
