package com.app.android.domain.entities.entitiesPerfiles

// El registro conserva su usuario; las banderas adicionales sólo controlan el siguiente paso de UI.
data class ResultadoRegistro(
    val usuario: Usuario,
    val requiereConfirmacion: Boolean = false,
    val correoEnviado: Boolean = true,
)
