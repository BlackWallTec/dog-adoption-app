package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// ORGANISMO: barra de arriba con flecha atrás, título y campanita
@Composable
fun AppTopBar(
    titulo: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNotificacionesClick: () -> Unit = {}, //Ahorita no hace nada
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon button = un icono que se puede picar (notificacione sy flecha)
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Regresar",
                tint = InverseSurface
            )
        }

        Text(
            text = titulo,
            color = InverseSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f) // ocupa el espacio que sobra
            // aka empuja la campana a la derecha
        )

        IconButton(onClick = onNotificacionesClick) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "Notificaciones",
                tint = InverseSurface,
            )
        }
    }
}

@Preview(showBackground = true, name = "Top bar")
@Composable
fun AppTopBarPreview() {
    AppTopBar(
        titulo = "Solicitud de adopción",
        onBackClick = {},
        modifier = Modifier.background(Surface),
    )
}