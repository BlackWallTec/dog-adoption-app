package com.app.android.presentation.viewmodel.viewModelCatalogo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.domain.useCases.useCasesCatalogo.GetFinalesFelicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
sealed interface FinalesFelicesUiState {
    data object Loading : FinalesFelicesUiState
    data object Empty : FinalesFelicesUiState
    data class Success(
        val finales: List<FinalFeliz>,
    ) : FinalesFelicesUiState
    data class Error(
        val mensaje: String,
    ) : FinalesFelicesUiState
}
@HiltViewModel
class FinalesFelicesViewModel

@Inject
constructor(
    private val getFinalesFelices: GetFinalesFelicesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FinalesFelicesUiState>(FinalesFelicesUiState.Loading)
    val uiState: StateFlow<FinalesFelicesUiState> = _uiState.asStateFlow()
    private var carga: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        carga?.cancel()
        carga =
            viewModelScope.launch {
                _uiState.value = FinalesFelicesUiState.Loading
                try {
                    val finales = getFinalesFelices()
                    _uiState.value =
                        if (finales.isEmpty()) {
                            FinalesFelicesUiState.Empty
                        } else {
                            FinalesFelicesUiState.Success(finales)
                        }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Log.e("HU22", "Error al cargar finales felices", e)
                    _uiState.value =
                        FinalesFelicesUiState.Error("No se pudieron cargar los finales felices")
                }
            }
    }
}