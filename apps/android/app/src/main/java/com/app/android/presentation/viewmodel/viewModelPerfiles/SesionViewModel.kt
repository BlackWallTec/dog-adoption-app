package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.CodigoError
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.useCases.useCasesPerfiles.CerrarSesionUseCase
import com.app.android.domain.useCases.useCasesPerfiles.ObtenerPerfilUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RestaurarSesionUseCase
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
    private val restaurarSesion: RestaurarSesionUseCase,
    private val obtenerPerfil: ObtenerPerfilUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SesionUiState())
    val state = _state.asStateFlow()

    private var restoreJob: Job? = null
    private var profileJob: Job? = null
    private var logoutJob: Job? = null

    // Ruta protegida a la que volver después de iniciar sesión.
    private var destinoTrasLogin: String? = null

    init { restaurar() }

    fun recordarDestino(ruta: String?) { destinoTrasLogin = ruta }

    fun consumirDestino(): String? = destinoTrasLogin.also { destinoTrasLogin = null }

    fun restaurar() {
        if (_state.value.cerrando) return

        // Cancelamos cualquier trabajo anterior antes de empezar uno nuevo.
        restoreJob?.cancel()
        profileJob?.cancel()
        _state.value = SesionUiState()

        restoreJob = viewModelScope.launch {
            try {
                val sesion = restaurarSesion()
                _state.value = SesionUiState(sesion = sesion, comprobando = false)
                if (sesion != null) consultarPerfil()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = SesionUiState(comprobando = false, error = e.mensajePerfiles())
            }
        }
    }

    fun autenticado(sesion: Sesion) {
        // Cancelamos restauraciones o logout que pudieran seguir corriendo.
        restoreJob?.cancel()
        profileJob?.cancel()
        logoutJob?.cancel()
        _state.value = SesionUiState(sesion = sesion, comprobando = false)
        consultarPerfil()
    }

    fun consultarPerfil() {
        if (_state.value.sesion == null) return
        if (_state.value.cerrando) return
        if (profileJob?.isActive == true) return

        profileJob = viewModelScope.launch {
            _state.update { it.copy(cargandoPerfil = true, error = null) }
            try {
                val perfil = obtenerPerfil()
                _state.update { it.copy(perfil = perfil) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si la sesión venció, volvemos a la pantalla de login.
                if (e is ErrorApi && e.codigo == CodigoError.UNAUTHENTICATED) {
                    _state.value = SesionUiState(comprobando = false, error = e.mensajePerfiles())
                } else {
                    _state.update { it.copy(error = e.mensajePerfiles()) }
                }
            } finally {
                // Solo actualizamos cargandoPerfil si este job no fue cancelado.
                if (profileJob?.isCancelled == false) {
                    _state.update { it.copy(cargandoPerfil = false) }
                }
            }
        }
    }

    fun salir() {
        if (_state.value.cerrando) return
        destinoTrasLogin = null
        restoreJob?.cancel()
        profileJob?.cancel()
        _state.update { it.copy(cerrando = true, cargandoPerfil = false, error = null) }

        logoutJob = viewModelScope.launch {
            try {
                cerrarSesion()
                _state.value = SesionUiState(comprobando = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(cerrando = false, error = e.mensajePerfiles()) }
            }
        }
    }
