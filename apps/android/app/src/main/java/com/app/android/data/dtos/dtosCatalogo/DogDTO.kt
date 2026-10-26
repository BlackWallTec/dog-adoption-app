package com.app.android.data.dtos.dtosCatalogo
import com.app.android.domain.entities.entitiesCatalogo.Dog

// 1. DTOs Pequeños (Sub-DTOs)
data class RazaDTO(
    val id_raza: String?,
    val nombre: String?,
    val descripcion: String?,
)

data class TamanoDTO(
    val id_tamano: String?,
    val nombre: String?,
    val descripcion: String?,
)

data class CaracterDTO(
    val id_caracter: String?,
    val nombre: String?,
    val descripcion: String?,
)

data class CuidadoDTO(
    val id_cuidado: String?,
    val descripcion: String?,
)

data class MultimediaDTO(
    val id_multimedia: String?,
    val url: String?,
    val tipo: String?,
    val fecha_subida: String?,
)

// 2. El DTO Principal
data class DogDTO(
    // Campos de tabla perro
    val id_perro: String?,
    val nombre: String?,
    val sexo: String?,
    val edad_aprox: Int?,
    val tamano_medida: Float?,
    val descripcion: String?,
    val esterilizado: Boolean?,
    val vacunado: Boolean?,
    val estado: String?,
    val fecha_ingreso: String?,
    // Sub-DTOs de tablas foráneas (FK)
    val raza: RazaDTO?,
    val tamano: TamanoDTO?,
    val caracter: CaracterDTO?,
    val cuidado: CuidadoDTO?,
    val multimedia: MultimediaDTO?,
)

// función de mapeo a Dog
fun DogDTO.toDomain(): Dog =
    Dog(
        id = this.id_perro ?: "",
        nombre = this.nombre ?: "Sin nombre",
        sexo = this.sexo ?: "No especificado",
        edad = this.edad_aprox ?: 0,
        tamanoNum = this.tamano_medida ?: 0f,
        descripcion = this.descripcion ?: "",
        esterilizado = this.esterilizado ?: false,
        vacunado = this.vacunado ?: false,
        estado = this.estado ?: "",
        fechaIngreso = this.fecha_ingreso ?: "",
        // los sub-DTOs se mapean a campos de Dog de forma segura usando el operador ?.
        razaId = this.raza?.id_raza ?: "",
        razaNombre = this.raza?.nombre ?: "Desconocida",
        razaDescripcion = this.raza?.descripcion ?: "",
        tamanoId = this.tamano?.id_tamano ?: "",
        tamanoNombre = this.tamano?.nombre ?: "",
        tamanoDescripcion = this.tamano?.descripcion ?: "",
        perroCaracterId = this.caracter?.id_caracter ?: "",
        perroCaracterNombre = this.caracter?.nombre ?: "",
        perroCaracterDescripcion = this.caracter?.descripcion ?: "",
        perroCuidadosId = this.cuidado?.id_cuidado ?: "",
        perroCuidadosDescripcion = this.cuidado?.descripcion ?: "",
        url = this.multimedia?.url ?: "",
        tipo = this.multimedia?.tipo ?: "",
        fechaSubida = this.multimedia?.fecha_subida ?: "",
    )
