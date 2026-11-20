package com.app.android.domain.entities.entitiesDonativos

// ENTITY: un donativo del usuario (HU49)
data class Donativo(
    val id: String,
    val monto: Double,
    val fecha: String, // yyyy-MM-dd
    val estado: String,
    val cfdiSolicitado: Boolean,
) {
    // CA-02: solo se pide CFDI si ya esta validado, y una sola vez
    val puedePedirCfdi: Boolean
        get() = estado == "Validado" && !cfdiSolicitado
}
