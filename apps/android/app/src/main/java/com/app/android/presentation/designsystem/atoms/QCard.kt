package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Contenedor tarjeta: fondo surface (o containerColor), radio QRadius.medium, padding interno
 * QSpacing.l, sin sombra ni borde. Si onClick != null es presionable.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: Card/Surface sueltos.
 */
@Composable
fun QCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    val color = if (containerColor != Color.Unspecified) containerColor else QTheme.colors.surface
    val clickModifier =
        if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier
    Column(
        modifier =
            modifier
                .clip(QShapes.card)
                .background(color)
                .then(clickModifier)
                .padding(QSpacing.l),
        content = content,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QCard")
@Composable
private fun QCardPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QCard(modifier = Modifier.fillMaxWidth()) {
                QText(text = "Tarjeta normal", style = QTextStyle.Title)
                QText(text = "Fondo blanco sobre el fondo arena.")
            }
            QCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                QText(text = "Tarjeta presionable", style = QTextStyle.Title)
            }
            QCard(modifier = Modifier.fillMaxWidth(), containerColor = QTheme.colors.pending) {
                QText(text = "Con containerColor (pending)")
            }
        }
    }
}