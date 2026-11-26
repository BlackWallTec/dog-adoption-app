// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.atoms

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.designsystem.internal.QPlaceholder

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
 * TODO(P1): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QButton", modifier)
}
