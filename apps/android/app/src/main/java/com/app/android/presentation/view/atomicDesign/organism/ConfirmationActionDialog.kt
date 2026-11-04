package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
@Composable
fun ConfirmActionDialog(
    titulo: String,
    descripcion: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    textoVolver: String = "Volver",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            AppButton(
                text = textoConfirmar,
                onClick = onConfirmar,
                variant = ButtonVariant.Danger,
            )
        },
        dismissButton = {
            AppButton(
                text = textoVolver,
                onClick = onDismiss,
                variant = ButtonVariant.Clear,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ConfirmActionDialogPreview() {
    AndroidTheme {
        ConfirmActionDialog(
            titulo = "¿Cancelar tu inscripción?",
            descripcion = "Si cancelas, liberarás tu lugar en Paseos con cariño. ¿Quieres continuar?",
            textoConfirmar = "Sí, cancelar",
            onConfirmar = {},
            onDismiss = {},
        )
    }
}
