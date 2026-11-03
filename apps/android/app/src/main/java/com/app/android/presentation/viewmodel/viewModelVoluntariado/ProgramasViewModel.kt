package com.app.android.presentation.viewmodel.viewModelVoluntariado

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerProgramasVigentesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramasViewModel @Inject constructor(
    private val obtenerProgramasVigentes: ObtenerProgramasVigentesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProgramasUiState>(ProgramasUiState.Loading)
    val uiState: StateFlow<ProgramasUiState> = _uiState.asStateFlow()

    init {
        cargarProgramas()
    }

    fun cargarProgramas() {
        viewModelScope.launch {
            _uiState.value = ProgramasUiState.Loading
            _uiState.value = try {
                val programas = obtenerProgramasVigentes()
                if (programas.isEmpty()) ProgramasUiState.Empty
                else ProgramasUiState.Success(programas)
            } catch (e: Exception) {
                ProgramasUiState.Error("No se pudieron cargar los programas")
            }
        }
    }
}