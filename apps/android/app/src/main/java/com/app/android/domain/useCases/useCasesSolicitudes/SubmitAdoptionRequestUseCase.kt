package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionRequest
import com.app.android.domain.repoInterface.repoIntSolicitudes.AdoptionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// Definir después los estados del flow en el transcurso del proceso de adopción
class SubmitAdoptionRequestUseCase
    @Inject
    constructor(
        private val repository: AdoptionRepository,
    ) {
        suspend operator fun invoke(request: AdoptionRequest): Flow<Result<Unit>> =
            flow {
                try {
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                }
            }
    }
