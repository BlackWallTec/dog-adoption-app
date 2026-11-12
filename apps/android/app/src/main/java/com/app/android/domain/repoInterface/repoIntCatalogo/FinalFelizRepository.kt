package com.app.android.domain.repoInterface.repoIntCatalogo

import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz

interface FinalFelizRepository {
    suspend fun getFinalesFelices(): List<FinalFeliz>
}