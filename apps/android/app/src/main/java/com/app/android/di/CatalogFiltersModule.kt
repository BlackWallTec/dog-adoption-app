package com.app.android.di

import com.app.android.data.dataSource.dataSourcesCatalogo.CatalogFiltersApi
import com.app.android.data.local.preferences.DogPreferences
import com.app.android.data.repoImp.repoImpCatalogo.FilteredDogsRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCatalogo.FilteredDogsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CatalogFiltersModule {
    @Provides
    @Singleton
    fun provideCatalogFiltersApi(retrofit: Retrofit): CatalogFiltersApi = retrofit.create(CatalogFiltersApi::class.java)

    @Provides
    @Singleton
    fun provideFilteredDogsRepository(
        api: CatalogFiltersApi,
        preferences: DogPreferences,
    ): FilteredDogsRepository = FilteredDogsRepositoryImpl(api, preferences)
}
