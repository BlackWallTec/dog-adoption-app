package com.app.android.domain.entities.entitiesDonativos

// El comprobante que escogió el usuario, ya leído (HU46)
class ComprobanteArchivo(
    val nombre: String,
    val tipo: String, // "application/pdf", "image/jpeg" o "image/png"
    val bytes: ByteArray,
)
