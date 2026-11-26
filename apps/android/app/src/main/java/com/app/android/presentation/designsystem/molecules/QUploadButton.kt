// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.molecules

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.internal.QPlaceholder

/**
 * Subir archivo. Sin archivo: QButton(fillMaxWidth, icon = QIcons.Upload) + helperText (Caption). Con
 * fileName: QCard con QIconLabel(QIcons.Document, fileName) + QTextLink 'Cambiar' (onClick) +
 * QIconButton(QIcons.Close) si onRemoveFile != null.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QButton, QText, QCard, QIconLabel, QTextLink, QIconButton.
 * Reemplaza a: FileUploadCard, FileUploadCardEmpty, DocumentoItem.
 * TODO(P2): reemplazar QPlaceholder por la implementación real. NO cambiar la firma.
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
    QPlaceholder("QUploadButton", modifier)
}
