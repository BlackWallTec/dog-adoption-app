package com.app.android.di

import com.app.android.data.repoImp.repoImpCatalogo.PerroRepositoryImpl
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindPerroRepository(
        impl: PerroRepositoryImpl
    ): PerroRepository
}