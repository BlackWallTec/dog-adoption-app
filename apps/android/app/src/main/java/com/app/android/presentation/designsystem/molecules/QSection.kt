package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * QSectionHeader + contenido con separación QSpacing.s.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QSectionHeader.
 * Reemplaza a: SectionBlock, CareParagraphItem.
 */
@Composable
fun QSection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
    ) {
        QSectionHeader(title = title, subtitle = subtitle)
        content()
    }
}

@Preview(showBackground = true, name = "QSection")
@Composable
private fun QSectionPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.xl),
        ) {
            QSection(title = "Sobre mí") {
                QText(text = "Es un perro dachshund alemán de la franquicia Open Season, conocido por su personalidad dramática.")
            }
            QSection(title = "Salud y cuidados", subtitle = "Información del último chequeo veterinario.") {
                QCheckItem(text = "Vacunado")
                QCheckItem(text = "Esterilizado")
            }
        }
    }
}