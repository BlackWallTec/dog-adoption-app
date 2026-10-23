package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BotonNavegacion(
    etiqueta: String, // el texto de abajo: Catalogo, Ayudar, etc
    icono: ImageVector,
    seleccionado: Boolean, // es el botón activo?
    alHacerClick: () -> Unit, // que pasa al tocarlo
    modifier: Modifier = Modifier, // para que quien lo use pueda ajustarlo desde afuera
) {
    // Column = acomoda las cosas una debajo de otra (ícono arriba, texto abajo)
    Column(
        modifier =
            modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null, // sin efecto de toque
                    onClick = alHacerClick,
                )
                .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Box = la "pastilla" amarilla que va detrás del ícono
        Box(
            modifier =
                Modifier
                    .size(width = 64.dp, height = 32.dp)
                    .background(
                        color = if (seleccionado) Primary else Color.Transparent,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icono,
                contentDescription = etiqueta,
                tint = InverseSurface,
            )
        }

        Text(
            text = etiqueta,
            color = InverseSurface,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview (showBackground = true, name = "Botón Activo")
@Composable
fun BotonNavegacionActivoPreview() {
    BotonNavegacion(
        etiqueta = "Catálogo",
        icono = Icons.Filled.Pets,
        seleccionado = true,
        alHacerClick = {},
    )
}

@Preview(showBackground = true, name = "Botón inactivo")
@Composable
fun BotonNavegacionInactivoPreview() {
    BotonNavegacion(
        etiqueta = "Ajustes",
        icono = Icons.Outlined.Settings,
        seleccionado = false,
        alHacerClick = {},
    )
}
