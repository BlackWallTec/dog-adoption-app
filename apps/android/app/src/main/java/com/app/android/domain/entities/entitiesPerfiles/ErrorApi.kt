package com.app.android.domain.entities.entitiesPerfiles

// Error independiente de HTTP: conserva el texto y los campos enviados por el servidor.
class ErrorApi(
    mensaje: String,
    val campos: Map<String, String> = emptyMap(),
    val sesionExpirada: Boolean = false,
    val codigo: String? = null,
) : Exception(mensaje)
