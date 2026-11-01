package com.app.android.domain.repoInterface.repoIntPerfiles

import com.app.android.domain.entities.entitiesPerfiles.Usuario

interface PerfilesRepositoryInt {
    // Con "?" devuelve null si no hay Usuario para tal email
    suspend fun fetchByEmail(email: String): Usuario?
}

