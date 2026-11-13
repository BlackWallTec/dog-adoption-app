package com.app.android.presentation.viewmodel.viewModelSolicitudes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.useCases.useCasesSolicitudes.GetMisSolicitudesUseCase
import com.app.android.presentation.util.utilSolicitudes.toItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MisSolicitudesViewModel
    @Inject
    constructor(
        private val getMisSolicitudesUseCase: GetMisSolicitudesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<MisSolicitudesUiState>(MisSolicitudesUiState.Loading)
        val uiState: StateFlow<MisSolicitudesUiState> = _uiState

        // Se llama al antrar a la pantalla y con el botón de Reintentar
        fun onLoadSolicitudes() {
            _uiState.value = MisSolicitudesUiState.Loading
            viewModelScope.launch {
                try {
                    val solicitudes = getMisSolicitudesUseCase()
                    // Lista vacia =  SUccess(emptyList()): la pantalla invita a ir al catálogo
                    _uiState.value = MisSolicitudesUiState.Success(solicitudes.map { it.toItem() })
                } catch (e: CancellationException) {
                    throw e // Si se cancela la corrutina no es un error de pantalla
                } catch (e: Exception) {
                    Log.e("SOLICITUDES", "Error al cargar mis solicitudes: ${e.message} ", e)
                    _uiState.value =
                        MisSolicitudesUiState.Error(
                            "No se pudieron cargar tus solicitudes. Revisa tu conexión e inténtalo de nuevo.",
                        )
                }
            }
        }
    }
