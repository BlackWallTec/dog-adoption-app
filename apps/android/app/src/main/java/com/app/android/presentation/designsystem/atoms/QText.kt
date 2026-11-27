package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QTypography
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Texto con los estilos del tema. Si color es Unspecified usa el color por defecto del estilo
 * (textPrimary; Caption usa textSecondary).
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: TextoTitulo, TextoCuerpo, TextoEtiqueta, SupportingText, HelperText.
 */
@Composable
fun QText(
    text: String,
    modifier: Modifier = Modifier,
    style: QTextStyle = QTextStyle.Body,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    val resolvedColor =
        when {
            color != Color.Unspecified -> color
            style == QTextStyle.Caption -> QTheme.colors.textSecondary
            else -> QTheme.colors.textPrimary
        }
    Text(
        text = text,
        modifier = modifier,
        color = resolvedColor,
        style = QTypography.of(style),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = if (maxLines == Int.MAX_VALUE) TextOverflow.Clip else TextOverflow.Ellipsis,
    )
}

@Preview(showBackground = true, name = "QText - estilos")
@Composable
private fun QTextPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QTextStyle.entries.forEach { style ->
                QText(text = "${style.name}: Encuentra a tu nuevo mejor amigo", style = style)
            }
            QText(text = "Color personalizado (onAction)", color = QTheme.colors.onAction)
            QText(
                text = "Texto muy largo que se corta con puntos suspensivos cuando no cabe en una sola línea",
                maxLines = 1,
            )
            QText(text = "Centrado", textAlign = TextAlign.Center, modifier = Modifier.padding(top = QSpacing.s))
        }
    }
}