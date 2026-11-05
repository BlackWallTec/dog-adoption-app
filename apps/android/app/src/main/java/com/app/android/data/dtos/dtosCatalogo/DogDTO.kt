package com.app.android.data.dtos.dtosCatalogo

import android.util.Log
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.entities.entitiesCatalogo.MultimediaDog
import com.google.gson.annotations.SerializedName

// 1. Envoltorio Global de la API
data class ApiResponseDetailDogDto(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("data") val data: DogDetailDataDto?,
)

// 2. Objeto "data" contenedor
data class DogDetailDataDto(
    @SerializedName("dog") val dogList: List<DogInfoDto>?,
    @SerializedName("behavior") val behavior: BehaviorDto?,
    @SerializedName("specialCondition") val specialCondition: SpecialConditionDto?,
    @SerializedName("multimedia") val multimedia: List<MultimediaDto>?,
)

// 3. Info principal del perro (lo que viene dentro del array "dog")
data class DogInfoDto(
    @SerializedName("id_perro") val idPerro: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("sexo") val sexo: String?,
    @SerializedName("edad_aprox") val edadAprox: Int?,
    @SerializedName("tamano_medida") val tamanoMedida: Float?,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("esterilizado") val esterilizado: Boolean?,
    @SerializedName("vacunado") val vacunado: Boolean?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("fecha_ingreso") val fechaIngreso: String?,
    @SerializedName("raza") val raza: RazaDto?,
    @SerializedName("tamano") val tamano: TamanoDto?,
)

// 4. Objetos Secundarios (Comportamiento, Cuidados, Fotos)
data class RazaDto(
    @SerializedName("id_raza") val idRaza: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
)

data class TamanoDto(
    @SerializedName("id_tamano") val idTamano: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
)

data class BehaviorDto(
    @SerializedName("perro_tiene_caracter") val caracteresList: List<CaracterContainerDto>?,
)

data class CaracterContainerDto(
    @SerializedName("caracter") val caracter: CaracterInfoDto?,
)

data class CaracterInfoDto(
    @SerializedName("id_caracter") val idCaracter: String?,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,
)

data class SpecialConditionDto(
    @SerializedName("perro_tiene_cuidado_especial") val cuidadosList: List<CuidadoContainerDto>?,
)

data class CuidadoContainerDto(
    @SerializedName("cuidado_especial") val cuidadoEspecial: CuidadoInfoDto?,
)

data class CuidadoInfoDto(
    @SerializedName("id_cuidado") val idCuidado: String?,
    @SerializedName("descripcion") val descripcion: String?,
)

data class MultimediaDto(
    @SerializedName("id_multimedia") val idMultimedia: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("tipo") val tipo: String?,
    @SerializedName("fecha_subida") val fechaSubida: String?,
)

fun ApiResponseDetailDogDto.toDomain(): Dog {
    // 1. FASE DTO: Ver qué llegó directamente de la API / Gson
    val mainDog = this.data?.dogList?.firstOrNull()

    Log.d("DOG_DEBUG", "=== FASE 1: DATOS CRUDOS DEL DTO ===")
    Log.d("DOG_DEBUG", "Nombre del perro: ${mainDog?.nombre}")
    Log.d("DOG_DEBUG", "DTO esterilizado (crudo): ${mainDog?.esterilizado}")
    Log.d("DOG_DEBUG", "DTO vacunado (crudo): ${mainDog?.vacunado}")

    // Extraemos el comportamiento y cuidados
    val primerCaracter =
        this.data
            ?.behavior
            ?.caracteresList
            ?.firstOrNull()
            ?.caracter
    val primerCuidado =
        this.data
            ?.specialCondition
            ?.cuidadosList
            ?.firstOrNull()
            ?.cuidadoEspecial

    // Mapeamos la lista completa de multimedia
    val listaMultimedia =
        this.data?.multimedia?.map { dto ->
            MultimediaDog(
                url = dto.url ?: "",
                tipo = dto.tipo?.trim() ?: "",
                fechaSubida = dto.fechaSubida ?: "",
            )
        } ?: emptyList()

    // Creamos el objeto de dominio final
    val dogDomain =
        Dog(
            id = mainDog?.idPerro ?: "",
            nombre = mainDog?.nombre?.trim() ?: "Sin nombre",
            sexo = mainDog?.sexo?.trim() ?: "No especificado",
            edad = mainDog?.edadAprox ?: 0,
            tamanoNum = mainDog?.tamanoMedida ?: 0f,
            descripcion = mainDog?.descripcion ?: "",
            esterilizado = mainDog?.esterilizado ?: false, // 🔍 Si arriba es null, aquí caerá en false
            vacunado = mainDog?.vacunado ?: false, // 🔍 Si arriba es null, aquí caerá en false
            estado = mainDog?.estado ?: "",
            fechaIngreso = mainDog?.fechaIngreso ?: "",
            razaId = mainDog?.raza?.idRaza ?: "",
            razaNombre = mainDog?.raza?.nombre ?: "Desconocida",
            razaDescripcion = mainDog?.raza?.descripcion ?: "",
            tamanoId = mainDog?.tamano?.idTamano ?: "",
            tamanoNombre = mainDog?.tamano?.nombre ?: "",
            tamanoDescripcion = mainDog?.tamano?.descripcion ?: "",
            perroCaracterId = primerCaracter?.idCaracter ?: "",
            perroCaracterNombre = primerCaracter?.nombre ?: "",
            perroCaracterDescripcion = primerCaracter?.descripcion ?: "",
            perroCuidadosId = primerCuidado?.idCuidado ?: "",
            perroCuidadosDescripcion = primerCuidado?.descripcion ?: "",
            multimedia = listaMultimedia,
        )

    // 2. FASE DOMINIO: Ver cómo quedó construido tu objeto final
    Log.d("DOG_DEBUG", "=== FASE 2: OBJETO DOMINIO GENERADO ===")
    Log.d("DOG_DEBUG", "Dominio esterilizado: ${dogDomain.esterilizado}")
    Log.d("DOG_DEBUG", "Dominio vacunado: ${dogDomain.vacunado}")
    Log.d("DOG_DEBUG", "======================================")

    return dogDomain
}
