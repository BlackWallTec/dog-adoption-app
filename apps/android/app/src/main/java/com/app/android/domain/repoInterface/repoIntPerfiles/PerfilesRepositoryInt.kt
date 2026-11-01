package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.data.dtos.dtosPerfiles.ApiResponseUsuarioDTO

interface PerfilesRepositoryInt {
    // Con "?" devuelve null si no hay Usuario para tal email
    suspend fun fetchByEmail(email: String): ApiResponseUsuarioDTO?
}

