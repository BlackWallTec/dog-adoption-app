package com.app.android.presentation.navigation

// Equivalente a un singletone de preloads en Godot.
// En otras palabras: Script estatico con las rutas de las pantallas.
// Para no equivocarte al escribirlas.
sealed class Screen(
    val ruta: String,
) {
    object Catalogo : Screen("catalogo_screen")

    object DetallePerro : Screen("detalle_perro_screen/{dogId}") {
        // Esta función ayuda a construir la ruta cuando quieres pasarle un ID
        fun crearRuta(dogId: String) = "detalle_perro_screen/$dogId"
    }

    // HU39A: catálogo de programas de voluntariado
    object Programas : Screen("programas_screen")

    // Patrón para nueva pantalla
    // Object nombrePantalla: Screen("ruta_pantalla"){
    // fun crearRuta(dogId: String, categoria: String) = "editar_perro_screen/$dogId/$categoria"
    // }
}