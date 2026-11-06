package com.app.android.data.repoImp.repoImpDonativos

import com.app.android.data.dataSource.dataSourcesDonativos.DonativosApiService
import com.app.android.data.dtos.dtosDonativos.toDomain
import com.app.android.data.dtos.dtosDonativos.toRequest
import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.repoInterface.repoIntDonativos.DatosFiscalesRepositoryInt
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class DatosFiscalesRepositoryImpl
    @Inject
    constructor(
        private val api: DonativosApiService,
    ) : DatosFiscalesRepositoryInt {
        override suspend fun getDatosFiscales(idUsuario: String): DatosFiscales {
            val respuesta = api.fetchDatosFiscalesRemote(idUsuario)
            return respuesta.data?.toDomain()
                ?: throw Exception(respuesta.message ?: "No se encontraron los datos fiscales")
        }

        override suspend fun guardarDatosFiscales(
            idUsuario: String,
            datos: DatosFiscales,
        ) {
            val respuesta = api.updateDatosFiscalesRemote(idUsuario, datos.toRequest())
            if (respuesta.success != true) {
                throw Exception(respuesta.message ?: "No se pudieron guardar los datos fiscales")
            }
        }

        override suspend fun subirConstancia(
            idUsuario: String,
            archivo: ConstanciaArchivo,
        ): String {
            val cuerpo = archivo.bytes.toRequestBody(archivo.tipo.toMediaType())
            val respuesta = api.uploadConstanciaRemote(idUsuario, cuerpo)
            if (respuesta.success != true) {
                throw Exception(respuesta.message ?: "No se pudo subir la constancia")
            }
            return respuesta.data?.get("constancia_url") ?: ""
        }
    }
