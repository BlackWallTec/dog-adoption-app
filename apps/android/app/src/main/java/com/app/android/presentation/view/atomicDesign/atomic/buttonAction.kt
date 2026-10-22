package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// 1. TU COMPONENTE REAL: Aquí creas tu interfaz de usuario

val ColorPrimario = Color(0xFFFDD254)
val PrimaryContainer = Color(0xFFFFEDAD)

@Composable
fun MiTarjetaBienvenida(
    nombre: String,
    alHacerClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp).background(PrimaryContainer),
    ) {
        Text(text = "¡Hola, $nombre!")
        Text(text = "Bienvenido a Jetpack Compose.")
        Button(
            onClick = alHacerClick,
            modifier = Modifier.padding(top = 8.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = ColorPrimario,
                    contentColor = Color.Black,
                ),
        )
        {
            Text("Continuar")
        }
    }
}

// 2. LA PREVISUALIZACIÓN: Esta función solo sirve para ver el diseño en Android Studio
@Preview(showBackground = true, name = "Vista de Bienvenida")
@Composable
fun MiTarjetaBienvenidaPreview() {
    MiTarjetaBienvenida(nombre = "Carlos", alHacerClick = {})
}
