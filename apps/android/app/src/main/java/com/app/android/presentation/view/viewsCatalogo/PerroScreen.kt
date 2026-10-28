package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

@Composable
fun PerrosScreen(vm: perrosViewModel=viewmodel()){
    LazyVerticalGrid(
        columns= GridCells.Fixed(2),
        contentPadding=PaddingValues(16.dp),
        horizontalArrangement= Arrangement.spaceBy(12.dp),
        verticalArrangement=Arrangement.spaceBy(12.dp)
    ){
        items(vm.perros){perro->
            Card{
                Column{
                    AsyncImage(
                        modifier=Modifier.fillMaxWidth().height(150.dp)
                        model=perro.fotoUrl,
                        contentDescription=perro.nombre,
                        contentScale= ContentScale.Crop,
                    )
                    Column(Modifier.padding(12.dp)){
                        Text(perro.nombre, fontWeight=FontWeight.Bold)
                        Text("${perro.edad} años")
                    }
                }
            }
        }
    }
}