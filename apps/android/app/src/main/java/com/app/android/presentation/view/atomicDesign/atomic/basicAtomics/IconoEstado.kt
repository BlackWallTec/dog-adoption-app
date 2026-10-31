package com.app.android.presentation.view.atomicDesign.atomic.basicAtomics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.view.ui.theme.AndroidTheme

/*
 * PARÁMETROS CONFIGURABLES:
 * - icono: ImageVector (Obligatorio) -> El vector a renderizar (check, location, etc.).
 * - modifier: Modifier -> Tamaño del ícono o padding (ej: Modifier.size(18.dp)).
 * - contentDescription: String? -> Accesibilidad (null por defecto si el contexto lo explica).
 * - tint: Color -> Tinte/Color del ícono (Por defecto: primary del tema).
 */
@Composable
fun IconoEstado(
    icono: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        imageVector = icono,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint,
    )
}

@Preview(showBackground = true, name = "Átomo: IconoEstado")
@Composable
fun IconoEstadoPreview() {
    AndroidTheme {
        IconoEstado(icono = Icons.Outlined.CheckCircle)
    }
}
