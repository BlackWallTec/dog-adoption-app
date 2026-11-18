package com.app.android.di

import com.app.android.data.dataSource.dataSourcesCatalogo.CatalogFiltersApi
import com.app.android.data.repoImp.repoImpCatalogo.FilteredDogsRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

// Módulo de Hilt solo para los filtros del catálogo (HU24). No toca NetworkModule:
// reutiliza el Retrofit que ya provee.
//

@Module
@InstallIn(SingletonComponent::class)
object CatalogFiltersModule {
    @Provides
    @Singleton
    fun provideCatalogFiltersApi(retrofit: Retrofit): CatalogFiltersApi = retrofit.create(CatalogFiltersApi::class.java)

    @Provides
    @Singleton
    fun provideFilteredDogsRepository(api: CatalogFiltersApi): FilteredDogsRepository = FilteredDogsRepositoryImpl(api)
}