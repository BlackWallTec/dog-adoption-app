package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.atoms.QTextLink
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Subir archivo. Sin archivo: QButton (ancho completo, ícono Upload) + helperText (Caption). Con
 * fileName: QCard con QIconLabel(Document, fileName) + QTextLink 'Cambiar' (onClick) + QIconButton(Close)
 * si onRemoveFile != null. Mientras isLoading los botones de la tarjeta se deshabilitan.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QButton, QText, QCard, QIconLabel, QTextLink, QIconButton.
 * Reemplaza a: FileUploadCard, FileUploadCardEmpty, DocumentoItem.
 */
@Composable
fun QUploadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Subir comprobante",
    helperText: String? = null,
    fileName: String? = null,
    onRemoveFile: (() -> Unit)? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    if (fileName == null) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(QSpacing.s),
        ) {
            QButton(
                text = text,
                onClick = onClick,
                icon = QIcons.Upload,
                enabled = enabled,
                isLoading = isLoading,
                fillMaxWidth = true,
            )
            if (helperText != null) {
                QText(text = helperText, style = QTextStyle.Caption)
            }
        }
    } else {
        val actionsEnabled = enabled && !isLoading
        QCard(modifier = modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QIconLabel(
                    icon = QIcons.Document,
                    text = fileName,
                    iconTint = QTheme.colors.onAction,
                    style = QTextStyle.BodyStrong,
                    modifier = Modifier.weight(1f),
                )
                QTextLink(text = "Cambiar", onClick = onClick, enabled = actionsEnabled)
                if (onRemoveFile != null) {
                    QIconButton(
                        icon = QIcons.Close,
                        contentDescription = "Quitar $fileName",
                        onClick = onRemoveFile,
                        enabled = actionsEnabled,
                    )
                }
            }
            if (helperText != null) {
                QText(text = helperText, style = QTextStyle.Caption)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3EEDF, name = "QUploadButton - estados")
@Composable
private fun QUploadButtonPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            QUploadButton(onClick = {}, helperText = "Captura o PDF de tu transferencia. JPG, PNG o PDF · Máx. 10 MB")
            QUploadButton(onClick = {}, isLoading = true)
            QUploadButton(onClick = {}, enabled = false, text = "Subir identificación")
            QUploadButton(onClick = {}, fileName = "comprobante-nov-2026.pdf", onRemoveFile = {})
            QUploadButton(onClick = {}, fileName = "identificacion-oficial-con-un-nombre-muy-largo-que-no-cabe.jpg")
            QUploadButton(onClick = {}, fileName = "comprobante.pdf", onRemoveFile = {}, isLoading = true)
        }
    }
}