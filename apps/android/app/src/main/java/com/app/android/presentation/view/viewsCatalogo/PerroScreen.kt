package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.SearchBar
import com.app.android.presentation.viewmodel.viewModelCatalogo.PerrosUiState
import com.app.android.presentation.viewmodel.viewModelCatalogo.PerrosViewModel
import androidx.compose.runtime.LaunchedEffect
@Composable
fun PerroScreen(
    modifier: Modifier = Modifier,
    vm: PerrosViewModel = hiltViewModel(), //vm
    //nuevo
    onDogClick: (String) -> Unit = {}
) {
    var selected by remember { mutableIntStateOf(0) } //navbar
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.onLoadPerros()
    }
    val estado by vm.uiState.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .statusBarsPadding()
    ) {
        Column( //esto es para que se pueda mover de arriba hacia abajo y viceversa
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            when (selected) {
                0 -> {
                    Row( //esto es para las 3 rayitas y la campana
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú")
                        }
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                    Text(
                        "Catálogo",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Encuentra a tu nuevo mejor amigo.", fontSize = 16.sp)
                    Spacer(Modifier.height(20.dp))

                    SearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        onFilterClick = {}
                    )

                    PerroScreen(
                        estado=estado,
                        //nuevo
                        onDogClick = onDogClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()

                    )
                }
                /*
                                1 -> Text("Ayudar")
                                2 -> Text("Perfil")
                                3 -> Text("Solicitudes")
                                4 -> Text("Ajustes")
                 */
            }
        }
        NavBar(selected = selected, onSelect = { selected = it })
    }
}

@Composable
private fun PerroScreen(
    estado: PerrosUiState,        //vm
    modifier: Modifier = Modifier,
    //nuevo
    onDogClick: (String) -> Unit = {}
) {
    when (val resultado = estado) {
        PerrosUiState.Loading -> Text("Cargando perros...", modifier = modifier)
        PerrosUiState.Empty -> Text("No hay perros disponibles", modifier = modifier)
        is PerrosUiState.Error -> Text(resultado.mensaje, modifier = modifier)
        is PerrosUiState.Success -> {
            LazyVerticalGrid(
                modifier = modifier,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resultado.perros, key= { it.id }) { perro -> //nuevo  
                    Card (onClick = { onDogClick(perro.id) }) { //nuevo
                        Column {
                            AsyncImage(
                                model = perro.fotoUrl,
                                contentDescription = perro.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            Column(Modifier.padding(12.dp)) {
                                Text(perro.nombre, fontWeight = FontWeight.Bold)
                                Text("${perro.edad} años")
                                Text(perro.sexo)
                            }
                        }
                    }
                }
            }
        }
    }
}