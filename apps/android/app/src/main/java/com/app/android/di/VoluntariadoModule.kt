package com.app.android.di

import com.app.android.data.dataSource.dataSourcesVoluntariado.InscripcionApi
import com.app.android.data.repoImp.repoImpVoluntariado.InscripcionRepositoryImpl
import com.app.android.domain.repoInterface.repoIntVoluntariado.InscripcionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VoluntariadoApiModule {
    @Provides
    @Singleton
    fun provideInscripcionApi(retrofit: Retrofit): InscripcionApi =
        retrofit.create(InscripcionApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class VoluntariadoRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindInscripcionRepository(impl: InscripcionRepositoryImpl): InscripcionRepository
}