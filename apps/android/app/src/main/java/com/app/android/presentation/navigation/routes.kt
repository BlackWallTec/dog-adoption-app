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

    // ===== Solicitudes de adopción =====

    // llega desde el detalle del perro (Catálogo) con el id del perro
    object Requisitos : Screen("solicitud_requisitos/{idPerro}") {
        fun crearRuta(idPerro: String) = "solicitud_requisitos/$idPerro"
    }

    // Paso 2 · Datos generales (todo sale del borrador, no lleva parámetros)
    object DatosGenerales : Screen("solicitud_datos_generales")

    // Paso 3 · Tu hogar
    object TuHogar : Screen("solicitud_tu_hogar")

    // Paso 4 · Compromiso (todo sale del borrador, no lleva parámetros)
    object Compromiso : Screen("solicitud_compromiso")

    // Paso 5 · Documentos
    object Documentos : Screen("solicitud_documentos")

    // Paso 6 · Confirmar (aquí está el único botón que escribe en la BD: Enviar solicitud)
    object Confirmar : Screen("solicitud_confirmar")

    // Después de enviar con éxito
    object SolicitudEnviada : Screen("solicitud_enviada/{idSolicitud}") {
        fun crearRuta(idSolicitud: String) = "solicitud_enviada/$idSolicitud"
    }

    // Patrón para nueva pantalla
    // Object nombrePantalla: Screen("ruta_pantalla"){
    // fun crearRuta(dogId: String, categoria: String) = "editar_perro_screen/$dogId/$categoria"
    // }
}