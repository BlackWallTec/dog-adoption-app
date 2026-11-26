// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Tarjeta del catálogo: QPhoto arriba (alto ≈125.dp, solo esquinas de arriba redondeadas), abajo
 * nombre (Title), edad y sexo (Body). Fondo surface, toda la tarjeta presionable.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QPhoto, QText.
 * Reemplaza a: TarjetaMascota.
 * TODO(P3): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
 */
@Composable
fun QPetCard(
    photo: Any?,
    name: String,
    age: String,
    sex: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QPlaceholder("QPetCard", modifier)
}
