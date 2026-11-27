// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QPhoto
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val PHOTO_HEIGHT = 125.dp

// QPhoto sin radio propio: la esquina superior redondeada la recorta QCard (QShapes.card).
private val PHOTO_CORNER = 0.dp

/**
 * Tarjeta del catálogo: QPhoto arriba (alto ≈125.dp, solo esquinas de arriba redondeadas), abajo
 * nombre (Title), edad y sexo (Body). Fondo surface, toda la tarjeta presionable.
 *
 * QCard mete QSpacing.l de padding a todo su contenido. La foto compensa ese padding (ver bleed) para
 * llegar de borde a borde, y QCard recorta sus esquinas de arriba.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QCard, QPhoto, QText.
 * Reemplaza a: TarjetaMascota.
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
    QCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            QPhoto(
                model = photo,
                contentDescription = null,
                modifier = Modifier.bleed(horizontal = QSpacing.l, top = QSpacing.l, height = PHOTO_HEIGHT),
                cornerRadius = PHOTO_CORNER,
            )
            QText(
                text = name,
                style = QTextStyle.Title,
                maxLines = 1,
            )
            QText(
                text = age,
                style = QTextStyle.Body,
                maxLines = 1,
            )
            QText(
                text = sex,
                style = QTextStyle.Body,
                maxLines = 1,
            )
        }
    }
}

// Extiende el contenido hasta el borde de QCard: crece horizontal y hacia arriba el padding
// (horizontal, top), sin que ese espacio empuje el texto de abajo.
private fun Modifier.bleed(
    horizontal: Dp,
    top: Dp,
    height: Dp,
): Modifier =
    layout { measurable, constraints ->
        val extraWidth = horizontal.roundToPx() * 2
        val topPx = top.roundToPx()
        val placeable =
            measurable.measure(
                Constraints.fixed(
                    width = constraints.maxWidth + extraWidth,
                    height = height.roundToPx(),
                ),
            )
        layout(constraints.maxWidth, placeable.height - topPx) {
            placeable.place(x = -horizontal.roundToPx(), y = -topPx)
        }
    }

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QPetCard - estados")
@Composable
private fun QPetCardPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.fillMaxWidth().padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QPetCard(
                photo = null,
                name = "Señor Salchicha",
                age = "5 años",
                sex = "Macho",
                onClick = {},
                modifier = Modifier.weight(1f),
            )
            QPetCard(
                photo = null,
                name = "Bethoveen con un nombre muy largo que se corta",
                age = "Menos de 1 año",
                sex = "Hembra",
                onClick = {},
                modifier = Modifier.weight(1f),
            )
        }
    }
}
