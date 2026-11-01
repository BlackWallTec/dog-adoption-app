package com.app.android.domain.entities.entitiesCatalogo

import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.DogDetailState

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

// Función de extensión para mapear Dog a DogDetailState para la vista
fun Dog.toUiState(): DogDetailState {
    // 1. Mapeo dinámico de Salud y Cuidados (Booleanos + Colecciones)
    val saludList =
        mutableListOf<String>().apply {
            if (vacunado) add("Vacunado")
            if (esterilizado) add("Esterilizado")
            if (perroCuidadosDescripcion.isNotBlank()) add(perroCuidadosDescripcion)
        }

    // 2. Mapeo de Características Especiales
    val caracteristicasList =
        mutableListOf<String>().apply {
            if (perroCaracterNombre.isNotBlank()) add(perroCaracterNombre)
        }

    // 3. Mapeo de Métricas Principales (Edad, Sexo, Tamaño, Raza)
    val metricasMap =
        mapOf(
            "Edad" to "$edad años",
            "Sexo" to sexo,
            "Tamaño" to tamanoNombre,
            "Raza" to razaNombre,
        )

    return DogDetailState(
        nombre = nombre,
        estado = estado,
        ubicacion = "Refugio Principal", // O un campo si se agrega después
        metricas = metricasMap,
        salud = saludList,
        caracteristicas = caracteristicasList,
        sobreMi = descripcion,
        // Agregamos los nuevos campos descriptivos de las tablas relacionales
        descripcionRaza = razaDescripcion,
        descripcionTamano = tamanoDescripcion,
    )
}
