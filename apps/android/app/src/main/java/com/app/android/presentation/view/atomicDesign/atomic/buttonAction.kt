package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// 1. TU COMPONENTE REAL: Aquí creas tu interfaz de usuario

val Primary = Color(0xFFFDD254)
val PrimaryContainer = Color(0xFFFFEDAD)
val Secondary = Color(0xFF2F6FB5)
val SecondaryContainer = Color(0xFFD8E8FF)
val Error = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)
val Surface = Color(0xFFFFFBF2)
val SurfaceContainerLow = Color(0xFFF8F2E6)
val SurfaceContainer = Color(0xFFF1EBDD)
val SurfaceContainerHigh = Color(0xFFEAE3D4)
val InverseSurface = Color(0xFF34312B)
val Blanco = Color(0xFFFFFFFF)


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
                    containerColor = Primary,
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
