# 🎨 Presentation / View

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Presentation (Interfaz de Usuario)**. Esta subcarpeta contiene las **Pantallas y Composables (Jetpack Compose)**.

### 🎯 Objetivo y Tarea

Dibujar los elementos visuales en pantalla reaccionando (observando) al `UiState` del ViewModel.

- **Regla estricta:** No toma decisiones lógicas de negocio. Solo dispara eventos al ViewModel y renderiza componentes visuales.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.presentation.view

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.tuempresa.app.presentation.viewmodel.NombreUiState
import com.tuempresa.app.presentation.viewmodel.NombreViewModel

@Composable
fun NombreScreen(
    idParametro: String,
    viewModel: NombreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idParametro) {
        viewModel.onCargarDatos(idParametro)
    }

    when (val state = uiState) {
        is NombreUiState.Loading -> { /* Componente Loader */ }
        is NombreUiState.Success -> { /* Componente de éxito usando state.datos */ }
        is NombreUiState.Error -> { /* Componente de Error usando state.mensaje */ }
    }
}
```
