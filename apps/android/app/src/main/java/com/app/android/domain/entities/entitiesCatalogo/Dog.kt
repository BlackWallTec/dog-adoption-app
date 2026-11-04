package com.app.android.domain.entities.entitiesCatalogo
import android.util.Log
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
    val multimedia: List<MultimediaDog>,
)

// Una clase simple para el dominio
data class MultimediaDog(
    val url: String,
    val tipo: String,
    val fechaSubida: String,
)

// Función de extensión para mapear Dog a DogDetailState para la vista
fun Dog.toUiState(): DogDetailState {
    // 1. FASE ENTIDAD: Ver qué valores tiene el objeto Dog de Dominio antes de mapear a UI
    Log.d("DOG_DEBUG", "=== FASE 3: MAPEO A UI (ENTIDAD DOG) ===")
    Log.d("DOG_DEBUG", "Entidad - Nombre: $nombre")
    Log.d("DOG_DEBUG", "Entidad - esterilizado: $esterilizado")
    Log.d("DOG_DEBUG", "Entidad - vacunado: $vacunado")

    // Mapeo dinámico de Salud y Cuidados (Booleanos + Colecciones)
    val saludList =
        mutableListOf<String>().apply {
            if (vacunado) add("Vacunado")
            if (esterilizado) add("Esterilizado")
            if (perroCuidadosDescripcion.isNotBlank()) add(perroCuidadosDescripcion)
        }

    // 2. FASE RESULTADO UI: Ver si los textos se añadieron correctamente a la lista
    Log.d("DOG_DEBUG", "=== FASE 4: RESULTADO EN LISTA DE SALUD ===")
    Log.d("DOG_DEBUG", "Lista saludList generada: $saludList")
    Log.d("DOG_DEBUG", "==========================================")

    // Mapeo de Características Especiales
    val caracteristicasList =
        mutableListOf<String>().apply {
            if (perroCaracterNombre.isNotBlank()) add(perroCaracterNombre)
        }

    // Mapeo de Métricas Principales (Edad, Sexo, Tamaño, Raza)
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
        ubicacion = "Refugio Principal",
        metricas = metricasMap,
        salud = saludList,
        caracteristicas = caracteristicasList,
        sobreMi = descripcion,
        descripcionRaza = razaDescripcion,
        descripcionTamano = tamanoDescripcion,
    )
}
