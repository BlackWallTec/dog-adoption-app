@file:Suppress("ktlint:standard:import-ordering", "ktlint:standard:no-wildcard-imports")

package com.app.android.presentation.view.viewsCatalogo
// 1. Estados y ciclos de vida de JetPack Compose
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

// 2.Inyeccion de ViewModel con Hilt
import androidx.hilt.navigation.compose.hiltViewModel

// 3. Componentes de UI (Material 3)
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.entities.entitiesCatalogo.toUiState
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.toMediaItems
import com.app.android.presentation.view.atomicDesign.organism.NavBar

// 4. Clases de VMStates y el VM
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogDetailUiState
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogDetailViewModel

import com.app.android.presentation.view.atomicDesign.template.DogDetailTemplate

@Composable
fun DogDetailScreenView(
    // 1. Hilt inyecta el ViewModel
    viewModel: DogDetailViewModel = hiltViewModel(),
    // 2. Parámetros
    dogId: String = "0edfe707-cc43-4bcf-bd43-a7f61e312e17",
) {
    // 2. Nos suscribimos al UiState (conectamos a la señal como en Godot)
    val uiState by viewModel.uiState.collectAsState()
    // Combinación de _ready() & _load()
    LaunchedEffect(dogId) {
        viewModel.onLoadDogByID(dogId)
    }

    // 3. Lógica de pantalla
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = uiState) {
            // 3.1 Loading
            is DogDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            // 3.2 Succes case
            is DogDetailUiState.Success -> {
                // Muestra los datos mapeados desde tu Entity
                val dog: Dog = state.datos

                // Instanciamos nuestro Template, mapeamos Entity y enviamos parámetros
                val dogState = dog.toUiState()
                val mediaItems = listOf(dog).toMediaItems()
                // Variable para el contexto de la aplicación (necesario para el Toast)
                val context = LocalContext.current

                Scaffold(
                    // NavBar
                    bottomBar = {
                        // Asignamos NavBar al slot inferior del Scaffold
                        NavBar(
                            selected = 0,
                            onSelect = {},
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                ) { paddingValues ->
                    DogDetailTemplate(
                        mediaList = mediaItems,
                        dogState = dogState,
                        // onAdoptClick = { onNavigateToAdoptionForm(dog.id) },
                        onAdoptClick = { Toast.makeText(context, "Adoptando a: ${dog.id}", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.padding(paddingValues),
                    )
                }
            }

            // 3.3 Empty
            is DogDetailUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No hay datos disponibles",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }

            // 3.4 Error case
            is DogDetailUiState.Error -> {
                // Muestra un mensaje de error y opción de reintento
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = state.mensaje)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.onLoadDogByID(dogId) }) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}
