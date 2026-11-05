package com.app.android.domain.repoInterface.repoIntSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import kotlinx.coroutines.flow.StateFlow

// CONTRATO: el borrador en memoria, compartido por las 6 pantallas de la solicitud.
// No toca la red ni la BD. (hasta el boton final se hace eso)
interface BorradorRepositoryInt {
    // El borrador actual. null = no hay ninguna solicitud en curso
    val borrador: StateFlow<BorradorSolicitud?>

    // Empieza un borrador para ese usuario y perro.
    // Si ya hay uno del mismo usuario y perro, lo conserva (por ejemplo, regresó con back)
    fun iniciar(
        idUsuario: String,
        idPerro: String,
    )

    // Cambia el borrador actual. Ejemplo: actualizar { it.copy(tuHogar = nuevo) }
    fun actualizar(cambio: (BorradorSolicitud) -> BorradorSolicitud)

    // Borra el borrador (después de enviar con exito)
    fun limpiar()
}