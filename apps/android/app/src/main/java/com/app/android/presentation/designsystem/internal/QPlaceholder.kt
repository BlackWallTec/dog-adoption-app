// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.internal

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * SOLO PARA EL ESQUELETO: dibuja un recuadro "TODO <nombre>" mientras el dueño del componente
 * lo implementa. Cuando ya no lo use nadie (grep QPlaceholder = 0), P5 borra este archivo.
 */
@Composable
fun QPlaceholder(
    name: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    Column(modifier = modifier.border(1.dp, Color.Magenta).padding(4.dp)) {
        Text(text = "TODO $name")
        content()
    }
}
