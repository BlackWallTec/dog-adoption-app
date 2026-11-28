package com.app.android.presentation.viewmodel.viewModelSolicitudes

// Modelos del paso 6 (Confirmar) y de Solicitud enviada.
// Antes vivian en view/atomicDesign/page/ConfirmarScreen.kt

enum class SeccionSolicitud { DATOS_GENERALES, TU_HOGAR, COMPROMISO, DOCUMENTOS }

data class CampoResumen(
    val etiqueta: String,
    val valor: String, // "Pendiente" cuando no hay dato
)

data class SeccionResumen(
    val tipo: SeccionSolicitud,
    val titulo: String,
    val campos: List<CampoResumen>,
)

data class MascotaResumen(
    val nombre: String,
    val detalle: String, // "2 años · Macho · Mediano"
    val raza: String, // "Mestizo · Querétaro"
    val fotoUrl: String? = null,
)

data class ConfirmarSolicitudUiState(
    val mascota: MascotaResumen,
    val secciones: List<SeccionResumen>,
    val documentos: List<String>,
    val enviando: Boolean = false,
)

data class SolicitudEnviadaSnapshot(
    val solicitudId: String,
    val mascota: MascotaResumen,
)
