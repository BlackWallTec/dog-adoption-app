package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
import com.app.android.domain.entities.entitiesSolicitudes.AdoptionRequest

// Que datos pedimos y guardamos para el proceso de adopción (no como lo hacemos)
// interface defines a function without body
// suspend: freezes the screen while the file is being uploaded
interface AdoptionRepository {
    suspend fun submitRequest(request: AdoptionRequest)

    suspend fun getApplicantProfile(): AdoptionForm
}
