package com.app.android.data.dtos.dtosSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudParaEditar
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.google.gson.annotations.SerializedName

// ===== GET /api/solicitudes/cliente/activa =====

data class ApiResponseSolicitudActivaDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: SolicitudActivaDto?,
    @SerializedName("message") val message: String?,
)

data class SolicitudActivaDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("estado") val estado: String?,
)

// ===== GET /api/solicitudes/cliente/mis-solicitudes =====

// { success, data: [ ...solicitudes ] }  — data = [] si no tiene ninguna
data class ApiResponseMisSolicitudesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: List<SolicitudClienteDto>?,
    @SerializedName("message") val message: String?,
)

data class SolicitudClienteDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("folio") val folio: String?,
    @SerializedName("fecha_solicitud") val fechaSolicitud: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("perro") val perro: PerroSolicitudDto?,
    @SerializedName("puede_editar") val puedeEditar: Boolean?,
    @SerializedName("comentario_admin") val comentarioAdmin: String?,
)

data class PerroSolicitudDto(
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("foto_url") val fotoUrl: String?,
)

// ===== GET /api/solicitudes/cliente/perfil/:idUsuario =====

data class ApiResponseDatosGeneralesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DatosGeneralesDto?,
    @SerializedName("message") val message: String?,
)

data class DatosGeneralesDto(
    @SerializedName("id_usuario") val idUsuario: String?,
    @SerializedName("nombre_completo") val nombreCompleto: String?,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String?,
    @SerializedName("ocupacion") val ocupacion: String?,
    @SerializedName("domicilio") val domicilio: String?,
    @SerializedName("municipio") val municipio: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("correo") val correo: String?,
)

// ===== Respuestas Simples / DTOs de Solicitud =====

// Respuesta sin datos { success, message }
data class ApiResponseSimpleDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
)

// DTOs para creación de borrador / envío
data class CrearSolicitudRequestDto(
    @SerializedName("id_perro") val idPerro: String,
    @SerializedName("ocupacion") val ocupacion: String,
    @SerializedName("tuvo_mascotas") val tuvoMascotas: Boolean?,
    @SerializedName("otras_mascotas") val otrasMascotas: Boolean?,
    @SerializedName("motivo_adopcion") val motivoAdopcion: String,
    @SerializedName("donde_vivira") val dondeVivira: String,
    @SerializedName("personas_en_casa") val personasEnCasa: Int,
    @SerializedName("todos_de_acuerdo") val todosDeAcuerdo: Boolean?,
    @SerializedName("tipo_vivienda") val tipoVivienda: String,
    @SerializedName("jardin_patio") val jardinPatio: String,
    @SerializedName("casero_acepta") val caseroAcepta: Boolean?,
    @SerializedName("sobre_mascotas") val sobreMascotas: String,
    @SerializedName("cuidados_basicos") val cuidadosBasicos: String,
    @SerializedName("opinion_esterilizacion") val opinionEsterilizacion: String,
    @SerializedName("tiene_veterinario") val tieneVeterinario: Boolean?,
    @SerializedName("detalle_veterinario") val detalleVeterinario: String,
    @SerializedName("tiempo_solo") val tiempoSolo: String,
    @SerializedName("rutina_diaria") val rutinaDiaria: String,
    @SerializedName("manejo_comportamiento") val manejoComportamiento: String,
)

data class CrearSolicitudResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: SolicitudCreadaDataDto?,
    @SerializedName("message") val message: String?,
)

data class SolicitudCreadaDataDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("estado") val estado: String?,
)

data class FinalizarSolicitudRequestDto(
    @SerializedName("id_solicitud") val idSolicitud: String,
)

data class DocumentoUploadResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DocumentoDataDto?,
    @SerializedName("message") val message: String?,
)

data class DocumentoDataDto(
    @SerializedName("nombre_archivo") val nombreArchivo: String?,
    @SerializedName("tamano_bytes") val tamanoBytes: Long?,
)

data class ApiErrorResponseDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("code") val code: Any?,
    @SerializedName("fields") val fields: Map<String, String>?,
)

// ===== GET /api/solicitudes/cliente/:id/edicion =====

data class ApiResponseSolicitudEdicionDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: SolicitudEdicionDto?,
    @SerializedName("message") val message: String?,
)

data class SolicitudEdicionDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("id_perro") val idPerro: String?,
    @SerializedName("ocupacion") val ocupacion: String?,
    @SerializedName("tuvo_mascotas") val tuvoMascotas: Boolean?,
    @SerializedName("otras_mascotas") val otrasMascotas: Boolean?,
    @SerializedName("motivo_adopcion") val motivoAdopcion: String?,
    @SerializedName("donde_vivira") val dondeVivira: String?,
    @SerializedName("personas_en_casa") val personasEnCasa: Int?,
    @SerializedName("todos_de_acuerdo") val todosDeAcuerdo: Boolean?,
    @SerializedName("tipo_vivienda") val tipoVivienda: String?,
    @SerializedName("jardin_patio") val jardinPatio: String?,
    @SerializedName("casero_acepta") val caseroAcepta: Boolean?,
    @SerializedName("sobre_mascotas") val sobreMascotas: String?,
    @SerializedName("cuidados_basicos") val cuidadosBasicos: String?,
    @SerializedName("opinion_esterilizacion") val opinionEsterilizacion: String?,
    @SerializedName("tiene_veterinario") val tieneVeterinario: Boolean?,
    @SerializedName("detalle_veterinario") val detalleVeterinario: String?,
    @SerializedName("tiempo_solo") val tiempoSolo: String?,
    @SerializedName("rutina_diaria") val rutinaDiaria: String?,
    @SerializedName("manejo_comportamiento") val manejoComportamiento: String?,
    @SerializedName("tiene_ine") val tieneIne: Boolean?,
    @SerializedName("tiene_domicilio") val tieneDomicilio: Boolean?,
)

// ===== MAPPERS: DTO -> Entity =====

fun SolicitudActivaDto.toDomain(): SolicitudResumen =
    SolicitudResumen(
        idSolicitud = idSolicitud ?: "",
        estado = estado.toEstadoSolicitud(),
    )

fun String?.toEstadoSolicitud(): EstadoSolicitud =
    when (this) {
        "Pendiente de Revisión" -> EstadoSolicitud.PENDIENTE_REVISION
        "En Periodo de Prueba" -> EstadoSolicitud.EN_PERIODO_PRUEBA
        "Aprobada Definitiva" -> EstadoSolicitud.APROBADA
        "Rechazada" -> EstadoSolicitud.RECHAZADA
        else -> EstadoSolicitud.DESCONOCIDO
    }

fun DatosGeneralesDto.toDomain(): DatosGenerales =
    DatosGenerales(
        nombreCompleto = nombreCompleto ?: "",
        fechaNacimiento = fechaNacimiento,
        ocupacion = ocupacion ?: "",
        domicilio = domicilio ?: "",
        municipio = municipio ?: "",
        telefono = telefono ?: "",
        correo = correo ?: "",
    )

fun SolicitudClienteDto.toDomain(): SolicitudCliente =
    SolicitudCliente(
        idSolicitud = idSolicitud ?: "",
        folio = folio ?: "",
        fechaSolicitud = fechaSolicitud ?: "",
        estado = estado.toEstadoSolicitud(),
        nombrePerro = perro?.nombre?.takeIf { it.isNotBlank() },
        fotoUrl = perro?.fotoUrl?.takeIf { it.isNotBlank() },
        edicionHabilitada = puedeEditar ?: false,
        comentarioAdmin = comentarioAdmin?.takeIf { it.isNotBlank() },
    )

fun SolicitudEdicionDto.toDomain(): SolicitudParaEditar =
    SolicitudParaEditar(
        idSolicitud = idSolicitud ?: "",
        idPerro = idPerro ?: "",
        ocupacion = ocupacion ?: "",
        tuHogar =
            TuHogar(
                hadPet = tuvoMascotas,
                otherPets = otrasMascotas,
                whyAdopt = motivoAdopcion ?: "",
                whereLive = dondeVivira ?: "",
                peopleInHouse = personasEnCasa ?: 1,
                allAgree = todosDeAcuerdo,
                // La BD guarda "Depto"; aceptamos las dos formas
                houseType =
                    when (tipoVivienda?.trim()?.lowercase()) {
                        "depto", "departamento" -> HouseType.DEPARTAMENTO
                        "otro" -> HouseType.OTRO
                        else -> HouseType.CASA
                    },
                hasGarden = jardinPatio ?: "",
                petAccept = caseroAcepta,
                aboutPets = sobreMascotas ?: "",
            ),
        compromiso =
            Compromiso(
                petNeeds = cuidadosBasicos ?: "",
                petSterilization = opinionEsterilizacion ?: "",
                hasVet = tieneVeterinario,
                vetDetail = detalleVeterinario ?: "",
                timeAlone = tiempoSolo ?: "",
                dailyRoutine = rutinaDiaria ?: "",
                dogManagement = manejoComportamiento ?: "",
            ),
        tieneIne = tieneIne ?: false,
        tieneDomicilio = tieneDomicilio ?: false,
    )
