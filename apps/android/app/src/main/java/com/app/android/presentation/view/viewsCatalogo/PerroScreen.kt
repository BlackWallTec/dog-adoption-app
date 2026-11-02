package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.android.presentation.viewmodel.viewModelCatalogo.PerrosViewModel
import com.app.android.presentation.viewmodel.viewModelCatalogo.PerrosUiState
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage

@Composable
fun PerrosScreen(
    modifier : Modifier = Modifier,
    vm: PerrosViewModel =hiltViewModel()
) {
    val estado by vm.uiState.collectAsState()
    when (val resultado = estado) {
        PerrosUiState.Loading -> Text("Cargando perros...")
        PerrosUiState.Empty -> Text("No hay perros disponibles")
        is PerrosUiState.Error -> Text(resultado.mensaje)
        is PerrosUiState.Success -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resultado.perros) { perro ->
                    Card {
                        Column {
                            Text("URL: ${perro.fotoUrl}")
                            AsyncImage(
                                model = perro.fotoUrl,
                                contentDescription = perro.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(150.dp)
                            )
                            Column(Modifier.padding(12.dp)) {
                                Text(perro.nombre, fontWeight = FontWeight.Bold)
                                Text("${perro.edad} años")
                            }
                        }
                    }
                }
            }
        }
    }
}