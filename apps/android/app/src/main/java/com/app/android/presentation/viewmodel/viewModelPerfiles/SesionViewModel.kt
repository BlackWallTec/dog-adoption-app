package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.SesionVencidaException
import com.app.android.domain.repoInterface.repoIntPerfiles.PerfilesRepositoryInt
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SesionUiState(
    val sesion: Sesion? = null,
    val perfil: Perfil? = null,
    val comprobando: Boolean = true,
    val cargandoPerfil: Boolean = false,
    val cerrando: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class SesionViewModel @Inject constructor(
    private val repository: PerfilesRepositoryInt,
    private val borrador: BorradorRepositoryInt,
) : ViewModel() {
    private val _state = MutableStateFlow(SesionUiState())
    val state = _state.asStateFlow()
    private var restoreJob: Job? = null
    private var profileJob: Job? = null
    private var logoutJob: Job? = null

    // Este número me ayuda a reconocer respuestas de una sesión anterior.
    private var versionSesion = 0

    init { restaurar() }

    fun restaurar() {
        if (_state.value.cerrando) return
        val version = ++versionSesion
        restoreJob?.cancel()
        profileJob?.cancel()
        _state.value = SesionUiState()

        // Recupero la sesión sin guardar tokens ni hacer peticiones desde la vista.
        restoreJob = viewModelScope.launch {
            try {
                val sesion = repository.restaurarSesion()
                if (version != versionSesion) return@launch
                limpiarBorradorAjeno(sesion?.usuario?.id)
                _state.value = SesionUiState(sesion = sesion, comprobando = false)
                if (sesion != null) consultarPerfil()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (version == versionSesion) {
                    _state.value = SesionUiState(comprobando = false, error = e.mensajePerfiles())
                }
            }
        }
    }

    fun autenticado(sesion: Sesion) {
        // Si termina una restauración vieja después del login, ya no debe reemplazarlo.
        versionSesion++
        restoreJob?.cancel()
        profileJob?.cancel()
        logoutJob?.cancel()
        limpiarBorradorAjeno(sesion.usuario.id)
        _state.value = SesionUiState(sesion = sesion, comprobando = false)
        consultarPerfil()
    }

    fun consultarPerfil() {
        if (_state.value.sesion == null || _state.value.cerrando || profileJob?.isActive == true) return
        val version = versionSesion

        profileJob = viewModelScope.launch {
            _state.update { it.copy(cargandoPerfil = true, error = null) }
            try {
                val perfil = repository.consultarPerfil()
                // Ignoro el perfil si mientras llegaba se cambió o se cerró la sesión.
                if (version == versionSesion) _state.update { it.copy(perfil = perfil) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SesionVencidaException) {
                if (version == versionSesion) {
                    versionSesion++
                    borrador.limpiar()
                    _state.value = SesionUiState(comprobando = false, error = e.mensajePerfiles())
                }
            } catch (e: Exception) {
                if (version == versionSesion) _state.update { it.copy(error = e.mensajePerfiles()) }
            } finally {
                if (version == versionSesion) _state.update { it.copy(cargandoPerfil = false) }
            }
        }
    }

    fun salir(notificarServidor: Boolean = true) {
        if (_state.value.cerrando) return
        val version = ++versionSesion
        restoreJob?.cancel()
        profileJob?.cancel()
        borrador.limpiar()
        _state.update { it.copy(cerrando = true, cargandoPerfil = false, error = null) }

        // El ViewModel cambia el estado; la navegación se encarga de volver al login.
        logoutJob = viewModelScope.launch {
            try {
                repository.cerrarSesion(notificarServidor)
                if (version == versionSesion) _state.value = SesionUiState(comprobando = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (version == versionSesion) {
                    _state.update { it.copy(cerrando = false, error = e.mensajePerfiles()) }
                }
            }
        }
    }

    private fun limpiarBorradorAjeno(idUsuario: String?) {
        // Un usuario nuevo no debe continuar el borrador de otra cuenta.
        if (borrador.borrador.value?.idUsuario != idUsuario) borrador.limpiar()
    }
}
