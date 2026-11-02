package com.app.android.di

import com.app.android.data.repoImp.repoImpSolicitudes.FakeAdoptionRepository
import com.app.android.domain.repoInterface.repoIntSolicitudes.AdoptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SolicitudesModule {
    @Binds
    @Singleton
    abstract fun bindAdoptionRepository(impl: FakeAdoptionRepository): AdoptionRepository
}
