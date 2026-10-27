# 🧠 Presentation / ViewModel

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Presentation (Gestión de Estado)**. Esta subcarpeta contiene los **ViewModels y Estados de UI**.

### 🎯 Objetivo y Tarea

Sostener el estado de la pantalla (`UiState`) a través del ciclo de vida y procesar eventos de la vista ejecutando `UseCases`.

- **Regla estricta:** Lleva `@HiltViewModel`. Jamás hace referencias directas a archivos de `Data` ni componentes visuales de Jetpack Compose.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tuempresa.app.domain.entities.NombreEntidad
import com.tuempresa.app.domain.useCases.ObtenNombreCasoDeUso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NombreUiState {
    object Loading : NombreUiState
    data class Success(val datos: NombreEntidad) : NombreUiState
    data class Error(val mensaje: String) : NombreUiState
}

@HiltViewModel
class NombreViewModel @Inject constructor(
    private val obtenNombreCasoDeUso: ObtenNombreCasoDeUso
) : ViewModel() {

    private val _uiState = MutableStateFlow<NombreUiState>(NombreUiState.Loading)
    val uiState: StateFlow<NombreUiState> = _uiState

    fun onCargarDatos(id: String) {
        viewModelScope.launch {
            _uiState.value = NombreUiState.Loading
            try {
                val resultado = obtenNombreCasoDeUso(id)
                _uiState.value = NombreUiState.Success(resultado)
            } catch (e: Exception) {
                _uiState.value = NombreUiState.Error("Ocurrió un error al cargar datos")
            }
        }
    }
}
```
