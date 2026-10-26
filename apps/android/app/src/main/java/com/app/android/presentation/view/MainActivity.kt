package com.app.android.presentation.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.core.ui.components.AppButton
import com.app.android.core.ui.components.ButtonVariant
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.SearchBar
import com.app.android.presentation.view.ui.theme.AndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTheme {
                App()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun App() {
    var filtro by remember { mutableIntStateOf(0) } //boton esta seleccionado
    var selected by remember { mutableIntStateOf(0) } //navbar
    var query by remember { mutableStateOf("") }

    Column(
        modifier=Modifier.fillMaxSize().background(Color(0xFFFFFBF2)).statusBarsPadding()
    ){
        Column( //esto es para que se pueda mover de arriba hacia abajo y viceversa
            modifier=Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal=24.dp)
        ) {
            Row( //esto es para las 3 rayitas y la campana
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                }
            }
            Text("Catálogo", fontSize = 32.sp, fontWeight = FontWeight.Bold)//hacer letras negritas
            Spacer(Modifier.height(12.dp))
            Text("Encuentra a tu nuevo mejor amigo.", fontSize = 16.sp)
            Spacer(Modifier.height(20.dp))

            SearchBar(
                query=query,
                onQueryChange= { query = it},
                onFilterClick = {}
            )
            Spacer(Modifier.height(20.dp))


            Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                AppButton(
                    text = "Todos",
                    onClick = { filtro = 0 },
                    variant = if (filtro == 0) ButtonVariant.Secondary else ButtonVariant.Outlined
                )
                AppButton(
                    text = "Tamaño",
                    onClick = { filtro = 1 },
                    variant = if (filtro == 1) ButtonVariant.Secondary else ButtonVariant.Outlined
                )
                AppButton(
                    text = "Sexo",
                    onClick = { filtro = 2 },
                    variant = if (filtro == 2) ButtonVariant.Secondary else ButtonVariant.Outlined
                )
                //AppButton(text="Raza", onClick={filtro=3}, variant=if(filtro==3)ButtonVariant.Secondary else ButtonVariant.Outlined)
                AppButton(
                    text = "Edad",
                    onClick = { filtro = 4 },
                    variant = if (filtro == 4) ButtonVariant.Secondary else ButtonVariant.Outlined
                )

            }
            //poner spacer
        }
        NavBar(selected=selected, onSelect={selected=it})


    }
}
