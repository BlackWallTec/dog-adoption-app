// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.app.android.presentation.designsystem.internal.QPlaceholder
import com.app.android.presentation.designsystem.theme.QRadius

/**
 * Foto con esquinas redondeadas, ContentScale.Crop. model = URL/Uri/Painter/resId (usar coil3
 * AsyncImage; si es Painter usar Image). Mientras carga: fondo surfaceMuted.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: Multimedia (imagen).
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QPhoto(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = QRadius.medium,
) {
    QPlaceholder("QPhoto", modifier)
}
