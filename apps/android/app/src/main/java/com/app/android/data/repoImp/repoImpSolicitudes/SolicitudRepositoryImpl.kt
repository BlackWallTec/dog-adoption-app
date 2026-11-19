package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.data.dataSource.dataSourcesSolicitudes.SolicitudApiService
import com.app.android.data.dtos.dtosSolicitudes.ApiErrorResponseDto
import com.app.android.data.dtos.dtosSolicitudes.CrearSolicitudRequestDto
import com.app.android.data.dtos.dtosSolicitudes.toDomain
import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class SolicitudRepositoryImpl
@Inject
constructor(
    private val api: SolicitudApiService,
) : SolicitudRepositoryInt {
    override suspend fun getSolicitudActiva(
        idPerro: String,
    ): SolicitudResumen? {
        try {
            val respuesta = api.fetchActiveRemote(idPerro)
            if (respuesta.success != true) {
                throw SolicitudHttpException(400, respuesta.message ?: "Error al consultar solicitud activa")
            }
            return respuesta.data?.toDomain()
        } catch (e: HttpException) {
            throw e.toFriendlyException()
        } catch (e: SolicitudHttpException) {
            throw e
        } catch (_: IOException) {
            throw Exception("No se pudo conectar con el servidor.")
        }
    }

    override suspend fun getPerfil(): DatosGenerales {
        try {
            val respuesta = api.fetchProfileRemote()
            if (respuesta.success != true) {
                throw SolicitudHttpException(400, respuesta.message ?: "Error al obtener perfil")
            }
            return respuesta.data?.toDomain()
                ?: throw SolicitudHttpException(400, respuesta.message ?: "No se encontró el perfil")
        } catch (e: HttpException) {
            throw e.toFriendlyException()
        } catch (e: SolicitudHttpException) {
            throw e
        } catch (_: IOException) {
            throw Exception("No se pudo conectar con el servidor.")
        }
    }

    override suspend fun getMisSolicitudes(): List<SolicitudCliente> {
        try {
            val respuesta = api.fetchMisSolicitudesRemote()
            if (respuesta.success != true) {
                throw SolicitudHttpException(400, respuesta.message ?: "Error al obtener las solicitudes")
            }
            return respuesta.data?.map { it.toDomain() }
                ?: throw SolicitudHttpException(400, respuesta.message ?: "No se encontraron solicitudes")
        } catch (e: HttpException) {
            throw e.toFriendlyException()
        } catch (e: SolicitudHttpException) {
            throw e
        } catch (_: IOException) {
            throw Exception("No se pudo conectar con el servidor.")
        }
    }

    override suspend fun crearOReutilizarBorrador(borrador: BorradorSolicitud): String {
        val dg = borrador.datosGenerales
        val hogar = borrador.tuHogar
        val comp = borrador.compromiso

        val request =
            CrearSolicitudRequestDto(
                idPerro = borrador.idPerro,
                ocupacion = dg?.ocupacion?.trim() ?: "",
                tuvoMascotas = hogar?.hadPet,
                otrasMascotas = hogar?.otherPets,
                motivoAdopcion = hogar?.whyAdopt?.trim() ?: "",
                dondeVivira = hogar?.whereLive?.trim() ?: "",
                personasEnCasa = hogar?.peopleInHouse ?: 1,
                todosDeAcuerdo = hogar?.allAgree,
                tipoVivienda =
                    when (hogar?.houseType) {
                        HouseType.CASA -> "Casa"
                        HouseType.DEPARTAMENTO -> "Departamento"
                        HouseType.OTRO -> "Otro"
                        null -> "Casa"
                    },
                jardinPatio = hogar?.hasGarden?.trim() ?: "",
                caseroAcepta = hogar?.petAccept,
                sobreMascotas = hogar?.aboutPets?.trim() ?: "",
                cuidadosBasicos = comp?.petNeeds?.trim() ?: "",
                opinionEsterilizacion = comp?.petSterilization?.trim() ?: "",
                tieneVeterinario = comp?.hasVet,
                detalleVeterinario = comp?.vetDetail?.trim() ?: "",
                tiempoSolo = comp?.timeAlone?.trim() ?: "",
                rutinaDiaria = comp?.dailyRoutine?.trim() ?: "",
                manejoComportamiento = comp?.dogManagement?.trim() ?: "",
            )

        try {
            val response = api.crearOReutilizarBorrador(request)
            if (response.success != true) {
                throw SolicitudHttpException(400, response.message ?: "No se pudo crear o reutilizar el borrador.")
            }
            val data =
                response.data
                    ?: throw SolicitudHttpException(400, response.message ?: "El servidor no devolvió datos de la solicitud.")
            return data.idSolicitud
                ?: throw SolicitudHttpException(400, response.message ?: "No se pudo obtener el id de la solicitud.")
        } catch (e: HttpException) {
            throw e.toFriendlyException()
        } catch (e: SolicitudHttpException) {
            throw e
        } catch (_: IOException) {
            throw Exception("No se pudo conectar con el servidor.")
        }
    }

    override suspend fun subirDocumento(
        solicitudId: String,
        type: DocumentType,
        file: PickedFile,
        bytes: ByteArray,
    ): UploadedDocument {
        val tipoBody = type.name.toRequestBody("text/plain".toMediaType())
        val archivoPart =
            MultipartBody.Part.createFormData(
                "archivo",
                file.name,
                bytes.toRequestBody(file.mimeType.toMediaType()),
            )

        try {
            val response = api.uploadDocument(solicitudId, tipoBody, archivoPart)
            if (response.success != true) {
                throw SolicitudHttpException(400, response.message ?: "Error al subir documento")
            }
            val data =
                response.data
                    ?: throw SolicitudHttpException(400, response.message ?: "El servidor no devolvió datos del documento subido.")
            val nombre =
                data.nombreArchivo
                    ?: throw SolicitudHttpException(400, "El servidor no devolvió el nombre del archivo subido.")
            val tamano =
                data.tamanoBytes
                    ?: throw SolicitudHttpException(400, "El servidor no devolvió el tamaño del archivo subido.")
            return UploadedDocument(
                type = type,
                fileName = nombre,
                sizeBytes = tamano,
            )
        } catch (e: HttpException) {
            throw e.toFriendlyException()
        } catch (e: SolicitudHttpException) {
            throw e
        } catch (_: IOException) {
            throw Exception("No se pudo conectar con el servidor.")
        }
    }

    override suspend fun finalizarSolicitud(solicitudId: String) {
        try {
            val response = api.finalizarSolicitud(solicitudId)
            if (response.success != true) {
                throw SolicitudHttpException(400, response.message ?: "Error al finalizar la solicitud")
            }
        } catch (e: HttpException) {
            throw e.toFriendlyException()
        } catch (e: SolicitudHttpException) {
            throw e
        } catch (_: IOException) {
            throw Exception("No se pudo conectar con el servidor.")
        }
    }
}

fun HttpException.toFriendlyException(): SolicitudHttpException {
    val code = code()
    val errorBody = response()?.errorBody()?.string()
    val apiError =
        runCatching {
            Gson().fromJson(errorBody, ApiErrorResponseDto::class.java)
        }.getOrNull()

    val serverMsg = apiError?.message ?: apiError?.error

    val message =
        when (code) {
            400 -> serverMsg ?: "Datos inválidos en la solicitud."
            401 -> "Sesión expirada o no autorizada. Por favor inicia sesión nuevamente."
            403 -> "Este borrador pertenece a otra cuenta de usuario."
            409 -> serverMsg ?: "Ya existe una solicitud activa o borrador para este perro."
            413 -> "La imagen es demasiado grande. Por favor reduce o vuelve a capturar la imagen."
            else -> serverMsg ?: "Error del servidor ($code)."
        }
    return SolicitudHttpException(code, message)
}

class SolicitudHttpException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
