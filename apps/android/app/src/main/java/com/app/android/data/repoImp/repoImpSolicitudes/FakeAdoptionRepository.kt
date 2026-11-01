package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionRequest
import com.app.android.domain.repoInterface.repoIntSolicitudes.AdoptionRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

// RepoInt es para definir los datos que pedimos y guardamos
// Repositorio falso / Se remplaza al conectar con la BD
// Simula el subir formulario de adopción
class FakeAdoptionRepository
    @Inject
    constructor() : AdoptionRepository {
        override suspend fun submitRequest(request: AdoptionRequest) {
            delay(1500) // simulamos la espera de red

            // Probar diversos errores:
            // Quitar comentarios para probar
            // throw Exception("Error de prueba")
            // throw AppException.Rejected("Datos inválidos")
        }
    }
