package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.ErrorApi
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.useCases.useCasesPerfiles.CerrarSesionUseCase
import com.app.android.domain.useCases.useCasesPerfiles.ObtenerPerfilUseCase
import com.app.android.domain.useCases.useCasesPerfiles.RestaurarSesionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
    // Iniciando MutableStateFlow con un valor inicial de SesionUiState(),
    // que será observado y actualizado conforme a cambios
    // No quieres que tu _state quede expuesto más allá del viewmodel
    private val _state = MutableStateFlow(SesionUiState())
    // Por eso se representa como un StateFlow READONLY
    val state = _state.asStateFlow()

    // Rutinas a cargo de la sesión y el perfil
    private var sesionJob: Job? = null
    private var perfilJob: Job? = null

    // Esto se ejecuta cada vez que se crea una nueva instancia del objeto
    init {
        restaurar()
    }

    //
    fun restaurar() {

        if (_state.value.cerrando) return

        sesionJob?.cancel()
        perfilJob?.cancel()

        _state.value = SesionUiState()

        sesionJob = viewModelScope.launch {
            restaurarSesion().fold(
                onSuccess = { sesion ->
                    _state.value = SesionUiState(sesion = sesion, comprobando = false)
                    if (sesion != null) consultarPerfil()
                },
                onFailure = {
                    _state.value = SesionUiState(comprobando = false, error = it.message)
                },
            )
        }
    }

    fun autenticado(sesion: Sesion) {
        sesionJob?.cancel()
        perfilJob?.cancel()
        _state.value = SesionUiState(sesion = sesion, comprobando = false)
        consultarPerfil()
    }

    fun consultarPerfil() {
        if (_state.value.sesion == null || _state.value.cerrando || perfilJob?.isActive == true)
            return

        _state.update { it.copy(cargandoPerfil = true, error = null) }

        perfilJob = viewModelScope.launch {
            obtenerPerfil().fold(
                onSuccess = { perfil -> _state.update {
                        it.copy(perfil = perfil, cargandoPerfil = false)
                    }
                },

                onFailure = { error ->
                    if ((error as? ErrorApi)?.sesionExpirada == true) {
                        _state.value = SesionUiState(comprobando = false, error = error.message)
                    } else {
                        _state.update { it.copy(cargandoPerfil = false, error = error.message) }
                    }
                },
            )
        }
    }

    fun salir() {
        if (_state.value.cerrando) return
        sesionJob?.cancel()
        perfilJob?.cancel()
        _state.update { it.copy(cerrando = true, cargandoPerfil = false, error = null) }
        sesionJob = viewModelScope.launch {
            cerrarSesion().fold(
                onSuccess = { _state.value = SesionUiState(comprobando = false) },
                onFailure = { error -> _state.update {
                        it.copy(cerrando = false, error = error.message)
                    }
                },
            )
        }
    }
}
