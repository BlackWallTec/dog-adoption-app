// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.app.android.presentation.designsystem.internal.QPlaceholder

enum class QTab(
    val label: String,
) {
    Catalogo("Catálogo"),
    Ayudar("Ayudar"),
    Donar("Donar"),
    Solicitudes("Solicitudes"),
    Ajustes("Ajustes"),
}

/**
 * Nav bar de abajo con las 5 pestañas de QTab en ese orden (Catálogo, Ayudar, Donar, Solicitudes,
 * Ajustes) usando QNavItem. Fondo containerColor (Unspecified = background #F3EEDF), sombra hacia
 * arriba (QElevation.bar), navigationBarsPadding.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QNavItem.
 * Reemplaza a: NavBar.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QNavBar(
    selected: QTab,
    onSelect: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
) {
    QPlaceholder("QNavBar", modifier)
}
