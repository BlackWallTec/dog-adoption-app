package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionForm
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

            // Probar diversos errores (depende de Exception el cual no he definido y
            // no se si vamos a definir uno para cada caso):
            // Quitar comentarios para probar
            // throw XException("Error de prueba")
            // throw XException.Rejected("Datos inválidos")
        }

        // Cómo todavia tengo muchas dudas de que si perfiles va a recibir estos datos, mejor
        // lo comento cuando haga push a este commando
        override suspend fun getApplicantProfile() =
            AdoptionForm(
                name = "John Doe",
                age = 25,
                email = "john.c.breckinridge@altostrat.com",
                phone = "1234567890",
                address = "123 Main St",
                occupation = "Software Engineer",
            )
    }
