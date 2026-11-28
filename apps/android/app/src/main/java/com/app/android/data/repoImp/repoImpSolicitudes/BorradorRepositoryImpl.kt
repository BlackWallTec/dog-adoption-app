package com.app.android.data.repoImp.repoImpSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.presentation.view.atomicDesign.page.SolicitudEnviadaSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// El borrador vive en memoria. Hilt crea UNO solo para toda la app (@Singleton en NetworkModule),
// así las 6 pantallas ven el mismo. Se pierde al cerrar la app.
class BorradorRepositoryImpl : BorradorRepositoryInt {
    private val _borrador = MutableStateFlow<BorradorSolicitud?>(null)
    override val borrador: StateFlow<BorradorSolicitud?> = _borrador

    override fun iniciar(
        idUsuario: String,
        idPerro: String,
    ) {
        val actual = _borrador.value
        // Mismo usuario y mismo perro → se conserva lo que ya lleno
        if (actual != null && actual.idUsuario == idUsuario && actual.idPerro == idPerro) return
        // Otro perro (o no habia) → empieza uno nuevo
        _borrador.value =
            BorradorSolicitud(
                idUsuario = idUsuario,
                idPerro = idPerro,
                aceptoRequisitos = true,
            )
    }

    override fun reemplazar(nuevo: BorradorSolicitud) {
        _borrador.value = nuevo
    }

    override fun actualizar(cambio: (BorradorSolicitud) -> BorradorSolicitud) {
        // Si no hay borrador, no hace nada
        _borrador.update { actual -> actual?.let(cambio) }
    }

    override fun limpiar() {
        _borrador.value = null
    }

    private val _confirmacionEnviada =
        MutableStateFlow<SolicitudEnviadaSnapshot?>(null)

    override val confirmacionEnviada: StateFlow<SolicitudEnviadaSnapshot?> =
        _confirmacionEnviada.asStateFlow()

    override fun guardarConfirmacionEnviada(snapshot: SolicitudEnviadaSnapshot) {
        _confirmacionEnviada.value = snapshot
    }
}
