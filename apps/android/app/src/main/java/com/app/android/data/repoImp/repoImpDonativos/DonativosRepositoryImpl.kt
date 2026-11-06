package com.app.android.data.repoImp.repoImpDonativos

import com.app.android.data.dataSource.dataSourcesDonativos.DonativosApiService
import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.repoInterface.repoIntDonativos.DonativosRepositoryInt
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class DonativosRepositoryImpl
    @Inject
    constructor(
        private val api: DonativosApiService,
    ) : DonativosRepositoryInt {
        override suspend fun registrarDonativo(
            idUsuario: String,
            monto: String,
            referencia: String?,
            comprobante: ComprobanteArchivo,
        ) {
            val cuerpo = comprobante.bytes.toRequestBody(comprobante.tipo.toMediaType())
            val respuesta = api.createDonativoRemote(idUsuario, monto, referencia, cuerpo)
            if (respuesta.success != true) {
                throw Exception(respuesta.message ?: "No se pudo registrar el donativo")
            }
        }
    }
