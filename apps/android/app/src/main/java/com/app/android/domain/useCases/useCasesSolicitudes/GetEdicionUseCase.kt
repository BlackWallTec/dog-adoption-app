package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.EdicionSolicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

class GetEdicionUseCase
@Inject
constructor(
    private val borrador: BorradorRepositoryInt,
){
    operator fun invoke(): EdicionSolicitud? = borrador.borrador.value?.edicion
}