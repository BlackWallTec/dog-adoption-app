package com.app.android.presentation.viewmodel.viewModelCatalogo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.useCases.useCasesCatalogo.GetDogByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Maquina de estados de ViewModel para DogDetailScreen
sealed interface DogDetailUiState {
    // Singletons
    // Usar data junto con object para tener acceso funcionalidades (como toString())
    data object Loading : DogDetailUiState // Singleton para el estado de carga

    data object Empty : DogDetailUiState // Singleton para el estado de vacío

    // Data Classes para cada estado con información
    data class Success(
        val datos: Dog,
    ) : DogDetailUiState

    data class Error(
        val mensaje: String,
    ) : DogDetailUiState
}

@HiltViewModel
class DogDetailViewModel
    @Inject
    constructor(
        private val getDogByIdUseCase: GetDogByIdUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<DogDetailUiState>(DogDetailUiState.Loading)
        val uiState: StateFlow<DogDetailUiState> = _uiState

        fun onLoadDogByID(id: String) {
            viewModelScope.launch {
                _uiState.value = DogDetailUiState.Loading
                try {
                    val resultado: Dog? = getDogByIdUseCase(id)

                    // Debug
                    // 🟢 IMPRIMIMOS LA ENTITY RECIBIDA EN CONSOLA
                    Log.d("TEST_CLEAN", "✅ 2. ¡Éxito! Entity recibida: $resultado")
                    Log.d("TEST_CLEAN", "🐶 Nombre: ${resultado?.nombre}, Raza: ${resultado?.razaNombre}")

                    if (resultado == null) {
                        // Caso 1: BD vacía
                        _uiState.value = DogDetailUiState.Empty
                    } else {
                        // Caso 2: Datos encontrados
                        _uiState.value = DogDetailUiState.Success(resultado)
                    }
                } catch (e: Exception) {
                    _uiState.value = DogDetailUiState.Error("Ocurrió un error al cargar datos")
                    Log.e("TEST_CLEAN", "❌ 3. Error al conectar con el backend: ${e.message}", e)
                }
            }
        }
    }
