package com.app.android.presentation.viewmodel.viewModelPerfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesPerfiles.Perfil
import com.app.android.domain.entities.entitiesPerfiles.Sesion
import com.app.android.domain.entities.entitiesPerfiles.SesionVencidaException
import com.app.android.domain.entities.entitiesPerfiles.mensajePerfiles
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
    val sesion: Sesion? = null, val perfil: Perfil? = null,
    val comprobando: Boolean = true, val cargandoPerfil: Boolean = false,
    val cerrando: Boolean = false, val error: String? = null,
)

@HiltViewModel
class SesionViewModel @Inject constructor(
    private val repository: PerfilesRepositoryInt,
    private val borrador: BorradorRepositoryInt,
) : ViewModel() {
    private val _state = MutableStateFlow(SesionUiState())
    val state = _state.asStateFlow()
    private var profileJob: Job? = null
    init { restaurar() }

    fun restaurar() {
        _state.value = SesionUiState()
        viewModelScope.launch {
            try {
                val sesion = repository.restaurarSesion()
                limpiarBorradorAjeno(sesion?.usuario?.id)
                _state.value = SesionUiState(sesion = sesion, comprobando = false)
                if (sesion != null) consultarPerfil()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.value = SesionUiState(comprobando = false, error = e.mensajePerfiles()) }
        }
    }
    fun autenticado(sesion: Sesion) {
        limpiarBorradorAjeno(sesion.usuario.id)
        _state.value = SesionUiState(sesion = sesion, comprobando = false)
        consultarPerfil()
    }
    fun consultarPerfil() {
        if (_state.value.sesion == null || profileJob?.isActive == true) return
        profileJob = viewModelScope.launch {
            _state.update { it.copy(cargandoPerfil = true, error = null) }
            try { val perfil = repository.consultarPerfil(); _state.update { it.copy(perfil = perfil) } }
            catch (e: CancellationException) { throw e }
            catch (e: SesionVencidaException) {
                borrador.limpiar()
                _state.value = SesionUiState(comprobando = false, error = e.mensajePerfiles())
            }
            catch (e: Exception) { _state.update { it.copy(error = e.mensajePerfiles()) } }
            finally { _state.update { it.copy(cargandoPerfil = false) } }
        }
    }
    fun salir(onFinished: () -> Unit, notificarServidor: Boolean = true) {
        if (_state.value.cerrando) return
        profileJob?.cancel()
        borrador.limpiar()
        _state.update { it.copy(cerrando = true) }
        viewModelScope.launch {
            try {
                repository.cerrarSesion(notificarServidor)
                _state.value = SesionUiState(comprobando = false)
                onFinished()
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { _state.update { it.copy(cerrando = false, error = e.mensajePerfiles()) } }
        }
    }

    private fun limpiarBorradorAjeno(idUsuario: String?) {
        if (borrador.borrador.value?.idUsuario != idUsuario) borrador.limpiar()
    }
}
