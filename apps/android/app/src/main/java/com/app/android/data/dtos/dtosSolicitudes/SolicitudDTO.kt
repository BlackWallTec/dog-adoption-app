package com.app.android.data.dtos.dtosSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.Solicitud
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.google.gson.annotations.SerializedName

// RESPUESTAS (lo que el backend manda)

// Envoltorio de una solicitud { success, data: { ... } }
data class ApiResponseSolicitudDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: SolicitudDto?, // null si no hay borrador
    @SerializedName("message") val message: String?,
)

// La solicitud tal como viene de la tabla solicitud_adopcion
data class SolicitudDto(
    @SerializedName("id_solicitud") val idSolicitud: String?,
    @SerializedName("id_usuario") val idUsuario: String?,
    @SerializedName("id_perro") val idPerro: String?,
    @SerializedName("id_estado") val idEstado: String?,
    @SerializedName("fecha_solicitud") val fechaSolicitud: String?,
)

// Paso 2 de la solicitud de adopcion

// Envoltorio de datos generales { success, data: { ... } }
data class ApiResponseDatosGeneralesDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DatosGeneralesDto?,
    @SerializedName("message") val message: String?,
)

// Los datos generales ya hechos buenos bonitos hermosos por formatProfile del backend
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

// Paso 2 de la solicitud de adopcion

//  Envoltorio de tu hogar { success, data: { ... } }
data class ApiResponseTuHogarDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: TuHogarDto?,
    @SerializedName("message") val message: String?,
)

// DTO de tu hogar
data class TuHogarDto(
    @SerializedName("ha_tenido_mascotas") val hadPet: Boolean?,
    @SerializedName("otras_mascotas") val otherPets: Boolean?,
    @SerializedName("motivo") val whyAdopt: String?,
    @SerializedName("donde_vivira") val whereLive: String?,
    @SerializedName("integrantes_hogar") val peopleInHouse: Int?,
    @SerializedName("todos_de_acuerdo") val allAgree: Boolean?,
    @SerializedName("tipo_vivienda") val houseType: String?,
    @SerializedName("jardin_o_patio") val hasGarden: String?,
    @SerializedName("casero_acepta") val petAccept: Boolean?,
    @SerializedName("sobre_mascotas") val aboutPets: String?,
)

// Paso 3 de la solicitud de adopcion

// Envoltorio de compromiso { success, data: { ... } }
data class ApiResponseCompromisoDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: CompromisoDto?,
    @SerializedName("message") val message: String?,
)

// DTO de compromiso
data class CompromisoDto(
    @SerializedName("cuidados_basicos") val petNeeds: String?,
    @SerializedName("opinion_esterilizacion") val petSterilization: String?,
    @SerializedName("permite_seguimiento") val petFollowing: Boolean?,
)

// Respuesta sin datos { success, message }
data class ApiResponseSimpleDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
)

// PETICIONES (lo que se le manda  en el body)

// Body del POST /api/solicitudes/cliente
data class CrearSolicitudRequestDto(
    @SerializedName("id_usuario") val idUsuario: String,
    @SerializedName("id_perro") val idPerro: String,
)

// Body del PUT /api/solicitudes/cliente/:id/datos-generales
data class GuardarDatosGeneralesRequestDto(
    @SerializedName("ocupacion") val ocupacion: String,
)

// Body del PUT /api/solicitudes/cliente/:id/tu-hogar
data class GuardarTuHogarRequestDto(
    @SerializedName("ha_tenido_mascotas") val hadPet: Boolean,
    @SerializedName("otras_mascotas") val otherPets: Boolean,
    @SerializedName("motivo") val whyAdopt: String,
    @SerializedName("donde_vivira") val whereLive: String,
    @SerializedName("integrantes_hogar") val peopleInHouse: Int,
    @SerializedName("todos_de_acuerdo") val allAgree: Boolean,
    @SerializedName("tipo_vivienda") val houseType: String,
    @SerializedName("jardin_o_patio") val hasGarden: String,
    @SerializedName("casero_acepta") val petAccept: Boolean,
    @SerializedName("sobre_mascotas") val aboutPets: String,
)

// Body del PUT /api/solicitudes/cliente/:id/compromiso
data class GuardarCompromisoRequestDto(
    @SerializedName("cuidados_basicos") val petNeeds: String,
    @SerializedName("opinion_esterilizacion") val petSterilization: String,
    @SerializedName("permite_seguimiento") val petFollowing: Boolean,
)

// ===== MAPPERS: DTO → Entity (Recibir información =====

fun SolicitudDto.toDomain(): Solicitud =
    Solicitud(
        id = idSolicitud ?: "",
        idUsuario = idUsuario ?: "",
        idPerro = idPerro ?: "",
        // Por ahora estos endpoints solo manejan borradores.
        // Cuando haya más, el backend debería mandar la descripción del estado para traducirla
        estado = EstadoSolicitud.BORRADOR,
        aceptoRequisitos = true,
    )

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

// DTO de tu hogar
fun TuHogarDto.toDomain(): TuHogar =
    TuHogar(
        hadPet = hadPet,
        otherPets = otherPets,
        whyAdopt = whyAdopt ?: "",
        whereLive = whereLive ?: "",
        peopleInHouse = peopleInHouse ?: 1,
        allAgree = allAgree ?: false,
        houseType =
            HouseType.entries.find { it.name.equals(houseType, ignoreCase = true) }
                ?: HouseType.CASA,
        hasGarden = hasGarden ?: "",
        petAccept = petAccept,
        aboutPets = aboutPets ?: "",
    )

// DTO de compromiso
fun CompromisoDto.toDomain(): Compromiso =
    Compromiso(
        petNeeds = petNeeds ?: "",
        petSterilization = petSterilization ?: "",
        petFollowing = petFollowing,
    )

// Mappers: Entity -> DTO

// Guardar de tuHogar
fun TuHogar.toRequestDto(): GuardarTuHogarRequestDto =
    GuardarTuHogarRequestDto(
        hadPet = hadPet == true,
        otherPets = otherPets == true,
        whyAdopt = whyAdopt,
        whereLive = whereLive,
        peopleInHouse = peopleInHouse,
        allAgree = allAgree == true,
        houseType = houseType.name.lowercase(),
        hasGarden = hasGarden,
        petAccept = petAccept == true,
        aboutPets = aboutPets,
    )

// Guardar de compromiso
fun Compromiso.toRequestDto(): GuardarCompromisoRequestDto =
    GuardarCompromisoRequestDto(
        petNeeds = petNeeds,
        petSterilization = petSterilization,
        petFollowing = petFollowing == true,
    )
