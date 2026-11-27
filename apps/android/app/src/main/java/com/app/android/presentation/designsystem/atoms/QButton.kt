package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

enum class QButtonVariant { Action, Destructive }

enum class QButtonSize { Regular, Small }

/**
 * EL botón de la app. Action = fondo action (#DCEAF8) + texto onAction (#2F6FB5) en negritas, forma
 * pill. Destructive = fondo negative + texto onNegative. Deshabilitado = fondo border + texto
 * disabledContent. isLoading muestra QLoadingIndicator chico en lugar del texto.
 *
 * Dueño: P1 (Átomos).
 * Usa (obligatorio): QLoadingIndicator.
 * Reemplaza a: AppButton, StickyButton, SaveButton, WelcomeCard.
 */
@Composable
fun QButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: QButtonVariant = QButtonVariant.Action,
    size: QButtonSize = QButtonSize.Regular,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    fillMaxWidth: Boolean = false,
) {
    val colors = QTheme.colors
    val containerColor =
        when (variant) {
            QButtonVariant.Action -> colors.action
            QButtonVariant.Destructive -> colors.negative
        }
    val contentColor =
        when (variant) {
            QButtonVariant.Action -> colors.onAction
            QButtonVariant.Destructive -> colors.onNegative
        }
    val isEnabled = enabled && !isLoading
    // Mientras carga se ve con sus colores normales (no como deshabilitado).
    val textColor = if (enabled || isLoading) contentColor else colors.disabledContent
    val minHeight = if (size == QButtonSize.Regular) QSize.buttonHeight else QSize.buttonSmallHeight
    val horizontalPadding = if (size == QButtonSize.Regular) QSpacing.xxl else QSpacing.l
    val widthModifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier

    Button(
        onClick = onClick,
        modifier = modifier.then(widthModifier).heightIn(min = minHeight),
        enabled = isEnabled,
        shape = QShapes.pill,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = if (isLoading) containerColor else colors.border,
                disabledContentColor = textColor,
            ),
        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = QSpacing.xs),
    ) {
        if (isLoading) {
            QLoadingIndicator(modifier = Modifier.size(QSize.iconSmall))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    QIcon(icon = icon, contentDescription = null, tint = textColor, size = QSize.iconSmall)
                    Spacer(modifier = Modifier.width(QSpacing.s))
                }
                QText(text = text, style = QTextStyle.Button, color = textColor, maxLines = 1)
            }
        }
    }
}

@Preview(showBackground = true, name = "QButton - variantes y estados")
@Composable
private fun QButtonPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QButton(text = "¡Quiero Adoptar!", onClick = {}, fillMaxWidth = true)
            QButton(text = "Subir comprobante", onClick = {}, icon = QIcons.Upload, fillMaxWidth = true)
            Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                QButton(text = "Compartir", onClick = {}, size = QButtonSize.Small, icon = QIcons.Share)
                QButton(text = "Inscribirme", onClick = {}, size = QButtonSize.Small)
            }
            QButton(text = "Inscribirme", onClick = {}, enabled = false, fillMaxWidth = true)
            QButton(text = "Enviando", onClick = {}, isLoading = true, fillMaxWidth = true)
            QButton(
                text = "Cancelar inscripción",
                onClick = {},
                variant = QButtonVariant.Destructive,
                fillMaxWidth = true,
            )
        }
    }
}