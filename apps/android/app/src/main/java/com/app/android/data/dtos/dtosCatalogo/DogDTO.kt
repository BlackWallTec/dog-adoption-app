package com.app.android.data.dtos.dtosCatalogo

import com.app.android.domain.entities.entitiesCatalogo.Dog
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
    // Extraemos el primer elemento del array "dog"
    val mainDog = this.data?.dogList?.firstOrNull()

    // Extraemos la lista de nombres de caracteres/comportamiento
    val primerCaracter =
        this.data
            ?.behavior
            ?.caracteresList
            ?.firstOrNull()
            ?.caracter

    // Extraemos el primer cuidado especial
    val primerCuidado =
        this.data
            ?.specialCondition
            ?.cuidadosList
            ?.firstOrNull()
            ?.cuidadoEspecial

    // Extraemos la primera foto o URL de multimedia
    val primeraFoto = this.data?.multimedia?.firstOrNull()

    return Dog(
        id = mainDog?.idPerro ?: "",
        nombre = mainDog?.nombre?.trim() ?: "Sin nombre",
        sexo = mainDog?.sexo?.trim() ?: "No especificado",
        edad = mainDog?.edadAprox ?: 0,
        tamanoNum = mainDog?.tamanoMedida ?: 0f,
        descripcion = mainDog?.descripcion ?: "",
        esterilizado = mainDog?.esterilizado ?: false,
        vacunado = mainDog?.vacunado ?: false,
        estado = mainDog?.estado ?: "",
        fechaIngreso = mainDog?.fechaIngreso ?: "",
        // Datos de Raza y Tamaño
        razaId = mainDog?.raza?.idRaza ?: "",
        razaNombre = mainDog?.raza?.nombre ?: "Desconocida",
        razaDescripcion = mainDog?.raza?.descripcion ?: "",
        tamanoId = mainDog?.tamano?.idTamano ?: "",
        tamanoNombre = mainDog?.tamano?.nombre ?: "",
        tamanoDescripcion = mainDog?.tamano?.descripcion ?: "",
        // Datos de Comportamiento y Cuidados
        perroCaracterId = primerCaracter?.idCaracter ?: "",
        perroCaracterNombre = primerCaracter?.nombre ?: "",
        perroCaracterDescripcion = primerCaracter?.descripcion ?: "",
        perroCuidadosId = primerCuidado?.idCuidado ?: "",
        perroCuidadosDescripcion = primerCuidado?.descripcion ?: "",
        // Multimedia
        url = primeraFoto?.url ?: "",
        tipo = primeraFoto?.tipo?.trim() ?: "",
        fechaSubida = primeraFoto?.fechaSubida ?: "",
    )
}
