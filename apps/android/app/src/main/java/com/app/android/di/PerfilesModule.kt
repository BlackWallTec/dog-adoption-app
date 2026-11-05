package com.app.android.di

import android.content.Context
import com.app.android.data.session.AlmacenSesionSeguro
import com.app.android.domain.repoInterface.repoIntPerfiles.AlmacenSesion
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PerfilesModule {
    @Provides @Singleton
    fun provideAlmacenSesion(@ApplicationContext context: Context): AlmacenSesion = AlmacenSesionSeguro(context)
}
