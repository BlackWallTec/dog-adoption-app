package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.data.dtos.dtosPerfiles.ApiResponseUsuarioDTO

import com.app.android.domain.entities.entitiesPerfiles.Sesion

interface PerfilesRepositoryInt {
    suspend fun login(email: String, passwd: String): Sesion
}