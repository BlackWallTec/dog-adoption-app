// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QIcon
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QTypography
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Buscador pill (fondo surface): lupa + campo + botón de filtro. El campo es un BasicTextField con el
 * placeholder dibujado con QText cuando query está vacío.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QIcon, QIconButton, QText.
 * Reemplaza a: SearchBar.
 */
@Composable
fun QSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Buscar un compañero",
) {
    val colors = QTheme.colors
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(QSize.fieldHeight)
                .clip(QShapes.pill)
                .background(colors.surface)
                .padding(start = QSpacing.l, end = QSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QIcon(icon = QIcons.Search, contentDescription = null, tint = colors.onAction)
        Spacer(modifier = Modifier.width(QSpacing.s))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (query.isEmpty()) {
                QText(
                    text = placeholder,
                    color = colors.textSecondary,
                    maxLines = 1,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = QTypography.of(QTextStyle.Body).copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.onAction),
            )
        }
        QIconButton(
            icon = QIcons.Filter,
            contentDescription = "Filtrar",
            onClick = onFilterClick,
            tint = colors.onAction,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QSearchBar - estados")
@Composable
private fun QSearchBarPreview() {
    QarinoTheme {
        val query = remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(QSpacing.l)) {
            QSearchBar(
                query = query.value,
                onQueryChange = { query.value = it },
                onFilterClick = {},
            )
            Spacer(modifier = Modifier.height(QSpacing.m))
            QSearchBar(
                query = "Salchicha",
                onQueryChange = {},
                onFilterClick = {},
            )
        }
    }
}
