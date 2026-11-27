package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Título de sección (SectionTitle) + subtítulo opcional (Body, textSecondary).
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QText.
 * Reemplaza a: HeatherSection, FormHeader.
 */
@Composable
fun QSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(QSpacing.xs),
    ) {
        // heading(): permite que el lector de pantalla salte de sección en sección
        QText(text = title, style = QTextStyle.SectionTitle, modifier = Modifier.semantics { heading() })
        if (subtitle != null) {
            QText(text = subtitle, color = QTheme.colors.textSecondary)
        }
    }
}

@Preview(showBackground = true, name = "QSectionHeader")
@Composable
private fun QSectionHeaderPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QSectionHeader(title = "Datos bancarios")
            QSectionHeader(title = "Filtrar programas", subtitle = "Selecciona las fechas y el horario de tu preferencia.")
        }
    }
}