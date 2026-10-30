package com.app.android.presentation.view.di

import com.app.android.data.repoImp.repoImpSolicitudes.FakeDocumentRepository
import com.app.android.domain.repoInterface.repoIntSolicitudes.DocumentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//When someone asks for a document I'm going to give them this fake document
//This is a temporary solution, while I connect backend
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        impl: FakeDocumentRepository
    ): DocumentRepository
}