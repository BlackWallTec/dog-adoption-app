package com.app.android.data.repoImp.repoImpDonativos

import com.app.android.data.dataSource.dataSourcesDonativos.DonativosApiService
import com.app.android.data.dtos.dtosDonativos.toDomain
import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.entities.entitiesDonativos.Donativo
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
            monto: String,
            referencia: String?,
            comprobante: ComprobanteArchivo,
        ) {
            val cuerpo = comprobante.bytes.toRequestBody(comprobante.tipo.toMediaType())
            val respuesta = api.createDonativoRemote(monto, referencia, cuerpo)
            if (respuesta.success != true) {
                throw Exception(respuesta.message ?: "No se pudo registrar el donativo")
            }
        }

        // HU49: los donativos del usuario con sesión
        override suspend fun getMisDonativos(): List<Donativo> =
            api.fetchMisDonativosRemote().data.orEmpty().map { it.toDomain() }

        // HU49: pedir el CFDI de un donativo validado
        override suspend fun solicitarCfdi(idDonativo: String) {
            val respuesta = api.createSolicitudCfdiRemote(idDonativo)
            if (respuesta.success != true) {
                throw Exception(respuesta.message ?: "No se pudo solicitar el CFDI")
            }
        }
    }
