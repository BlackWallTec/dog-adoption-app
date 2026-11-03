package com.app.android.data.dtos.dtosSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
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
    @SerializedName("had_pet") val hadPet: Boolean?,
    @SerializedName("other_pets") val otherPets: Boolean?,
    @SerializedName("why_adopt") val whyAdopt: String?,
    @SerializedName("where_live") val whereLive: String?,
    @SerializedName("people_in_house") val peopleInHouse: Int?,
    @SerializedName("all_agree") val allAgree: Boolean?,
    @SerializedName("house_type") val houseType: String?,
    @SerializedName("has_garden") val hasGarden: String?,
    @SerializedName("pet_acept") val petAcept: Boolean?,
    @SerializedName("about_pets") val aboutPets: String?,
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
    @SerializedName("pet_needs") val petNeeds: String?,
    @SerializedName("pet_sterilization") val petSterilization: String?,
    @SerializedName("pet_following") val petFollowing: Boolean?,
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
    @SerializedName("had_pet") val hadPet: Boolean,
    @SerializedName("other_pets") val otherPets: Boolean,
    @SerializedName("why_adopt") val whyAdopt: String,
    @SerializedName("where_live") val whereLive: String,
    @SerializedName("people_in_house") val peopleInHouse: Int,
    @SerializedName("all_agree") val allAgree: Boolean,
    @SerializedName("house_type") val houseType: String,
    @SerializedName("has_garden") val hasGarden: String,
    @SerializedName("pet_acept") val petAcept: Boolean,
    @SerializedName("about_pets") val aboutPets: String,
)

// Body del PUT /api/solicitudes/cliente/:id/compromiso
data class GuardarCompromisoRequestDto(
    @SerializedName("pet_needs") val petNeeds: String,
    @SerializedName("pet_sterilization") val petSterilization: String,
    @SerializedName("pet_following") val petFollowing: Boolean,
)

// ===== MAPPERS: DTO → Entity =====

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
        hadPet = hadPet ?: false,
        otherPets = otherPets ?: false,
    )

// DTO de compromiso
fun CompromisoDto.toDomain(): Compromiso =
    Compromiso(
        petNeeds = petNeeds ?: "",
        petSterilization = petSterilization ?: "",
        petFollowing = petFollowing ?: false,
    )
