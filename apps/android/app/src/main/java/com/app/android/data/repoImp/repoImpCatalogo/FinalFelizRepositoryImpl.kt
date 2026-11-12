package com.app.android.data.repoImp.repoImpCatalogo

import com.app.android.data.dataSource.dataSourcesCatalogo.FinalFelizApi
import com.app.android.data.dtos.dtosCatalogo.toDomain
import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.domain.repoInterface.repoIntCatalogo.FinalFelizRepository
import javax.inject.Inject

class FinalFelizRepositoryImpl
    @Inject
    constructor(
        private val api: FinalFelizApi,
    ) : FinalFelizRepository {
        override suspend fun getFinalesFelices(): List<FinalFeliz> {
            val respuesta = api.getFinalesFelices()
            if (respuesta.success == false) {
                throw IllegalStateException("El servicio de finales felices respondió sin éxito")
            }
            return respuesta.data.orEmpty().map { it.toDomain() }
        }
    }
