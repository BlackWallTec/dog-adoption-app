package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionRequest

// Que datos pedimos y guardamos para el proceso de adopción (no como lo hacemos)
interface AdoptionRepository {
    suspend fun submitRequest(request: AdoptionRequest)

    suspend fun getApplicantProfile(): AdoptionForm
}
