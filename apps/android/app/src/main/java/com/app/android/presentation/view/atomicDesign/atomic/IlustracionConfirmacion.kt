package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface

private val Halo = Color(0xFFF2F2F2)

// 1. EL ÁTOMO: sobre con sello de corazón y check de confirmación
@Composable
fun IlustracionConfirmacion(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 164.dp, height = 116.dp)
            .semantics { contentDescription = "Correo confirmado" },
    ) {
        // Halo gris de fondo
        Box(
            modifier = Modifier
                .offset(x = 28.dp, y = 2.dp)
                .size(110.dp)
                .background(color = Halo, shape = CircleShape),
        )

        // Tarjeta blanca con el sobre
        Box(
            modifier = Modifier
                .offset(x = 34.dp, y = 35.dp)
                .size(width = 96.dp, height = 68.dp)
                .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                .border(width = 2.dp, color = Secondary, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MailOutline,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier
                    .offset(y = 4.dp) // un poco abajo para que el sello lo tape arriba
                    .size(56.dp),
            )
        }

        // Sello de cariño
        IconoCircular(
            icono = Icons.Outlined.FavoriteBorder,
            tamano = 38.dp,
            tamanoIcono = 21.dp,
            modifier = Modifier.offset(x = 63.dp, y = 17.dp),
        )

        // Confirmación
        IconoCircular(
            icono = Icons.Rounded.Check,
            tamano = 30.dp,
            tamanoIcono = 18.dp,
            modifier = Modifier.offset(x = 117.dp, y = 77.dp),
        )
    }
}

// Círculo azul con un icono blanco al centro
@Composable
private fun IconoCircular(
    icono: ImageVector,
    tamano: Dp,
    tamanoIcono: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(tamano)
            .background(color = Secondary, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(tamanoIcono),
        )
    }
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, name = "Ilustración confirmación")
@Composable
fun IlustracionConfirmacionPreview() {
    IlustracionConfirmacion(
        modifier = Modifier
            .background(Surface)
            .padding(16.dp),
    )
}