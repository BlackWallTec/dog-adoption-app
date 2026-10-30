package com.app.android.domain.entities.entitiesCatalogo

data class Dog(
    val id: String,
    val nombre: String,
    val sexo: String,
    val edad: Int,
    val tamanoNum: Float,
    val descripcion: String,
    val esterilizado: Boolean,
    val vacunado: Boolean,
    val estado: String,
    val fechaIngreso: String,
    // Tabla Raza
    val razaId: String,
    val razaNombre: String,
    val razaDescripcion: String,
    // Tabla Tamaño
    val tamanoId: String,
    val tamanoNombre: String,
    val tamanoDescripcion: String,
    // Tabla Perro-Carácter
    val perroCaracterId: String,
    val perroCaracterNombre: String,
    val perroCaracterDescripcion: String,
    // Tabla Perro-Cuidados
    val perroCuidadosId: String,
    val perroCuidadosDescripcion: String,
    // Tabla Multimedia
    val url: String,
    val tipo: String,
    val fechaSubida: String,
)
