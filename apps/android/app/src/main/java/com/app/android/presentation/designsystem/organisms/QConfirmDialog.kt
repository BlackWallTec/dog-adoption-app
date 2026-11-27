// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QButtonVariant
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.atoms.QTextLink
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Diálogo de confirmación (AlertDialog) con fondo surface: título (Title), descripción (Body), botón de
 * confirmar de ancho completo (Destructive si destructive) y link de cancelar debajo, centrado.
 *
 * Dueño: P3 (Organismos).
 * Usa (obligatorio): QText, QButton, QTextLink.
 * Reemplaza a: ConfirmActionDialog.
 */
@Composable
fun QConfirmDialog(
    title: String,
    description: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Volver",
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = QShapes.large,
        containerColor = QTheme.colors.surface,
        title = {
            QText(
                text = title,
                style = QTextStyle.Title,
                modifier = Modifier.semantics { heading() },
            )
        },
        text = {
            QText(
                text = description,
                style = QTextStyle.Body,
            )
        },
        // Va en confirmButton para poder apilar los dos botones (AlertDialog los acomoda en fila si son slots separados).
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QSpacing.s),
            ) {
                QButton(
                    text = confirmText,
                    onClick = onConfirm,
                    variant = if (destructive) QButtonVariant.Destructive else QButtonVariant.Action,
                    fillMaxWidth = true,
                )
                QTextLink(text = dismissText, onClick = onDismiss)
            }
        },
    )
}

@Preview(showBackground = true, name = "QConfirmDialog - abierto")
@Composable
private fun QConfirmDialogPreview() {
    QarinoTheme {
        // Se muestra abierto para poder verlo en el preview.
        val visible = remember { mutableStateOf(true) }
        if (visible.value) {
            QConfirmDialog(
                title = "¿Cancelar tu inscripción?",
                description = "Perderás tu lugar en el voluntariado. Podrás volver a inscribirte si hay cupo.",
                confirmText = "Cancelar inscripción",
                onConfirm = { visible.value = false },
                onDismiss = { visible.value = false },
                destructive = true,
            )
        }
    }
}

@Preview(showBackground = true, name = "QConfirmDialog - acción normal")
@Composable
private fun QConfirmDialogNormalPreview() {
    QarinoTheme {
        QConfirmDialog(
            title = "¿Enviar donativo?",
            description = "Confirma que ya realizaste la transferencia.",
            confirmText = "Enviar donativo",
            onConfirm = {},
            onDismiss = {},
        )
    }
}
