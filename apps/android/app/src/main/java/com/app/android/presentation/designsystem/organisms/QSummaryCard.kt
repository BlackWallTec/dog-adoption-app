// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.atoms.QTextLink
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Tarjeta de resumen (paso Confirmar): fila con título (Title, heading) + QTextLink "Editar" si onEdit != null,
 * y debajo el contenido separado por QSpacing.s.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QText, QTextLink.
 * Reemplaza a: TarjetaResumen.
 */
@Composable
fun QSummaryCard(
    title: String,
    onEdit: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    QCard(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QText(
                    text = title,
                    style = QTextStyle.Title,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                if (onEdit != null) {
                    QTextLink(text = "Editar", onClick = onEdit)
                }
            }
            content()
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QSummaryCard - estados")
@Composable
private fun QSummaryCardPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QSummaryCard(
                title = "Datos generales",
                onEdit = {},
            ) {
                QText(text = "Nombre: Ana López", style = QTextStyle.Body)
                QText(text = "Teléfono: 55 1234 5678", style = QTextStyle.Body)
            }
            QSummaryCard(
                title = "Tu hogar",
                onEdit = null,
            ) {
                QText(text = "Casa con patio amplio", style = QTextStyle.Body)
            }
        }
    }
}
