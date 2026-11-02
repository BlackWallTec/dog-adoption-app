package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.AdoptionRequest
import com.app.android.domain.repoInterface.repoIntSolicitudes.AdoptionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

// Definir después en common (asi se llamaba en el lab, no se tenemos uno similar aqui) el result.kt
class SubmitAdoptionRequestUseCase
    @Inject
    constructor(
        private val repository: AdoptionRepository,
    ) {
        operator fun invoke(request: AdoptionRequest): Flow<Result<Unit>> =
            flow {
                emit(Result.Loading) //  Emite estado de carga
                try {
                    repository.submitRequest(request)
                    emit(Result.Success(Unit)) // éxito
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }
    }
