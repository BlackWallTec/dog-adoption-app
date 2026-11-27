package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Indicador de carga circular en onAction.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: EstadoCargando (indicador).
 */
@Composable
fun QLoadingIndicator(
    modifier: Modifier = Modifier,
    contentDescription: String = "Cargando",
) {
    val description = contentDescription
    CircularProgressIndicator(
        modifier = modifier.semantics { this.contentDescription = description },
        color = QTheme.colors.onAction,
        trackColor = QTheme.colors.surfaceMuted,
        strokeWidth = QSpacing.xs,
    )
}

@Preview(showBackground = true, name = "QLoadingIndicator")
@Composable
private fun QLoadingIndicatorPreview() {
    QarinoTheme {
        QLoadingIndicator(modifier = Modifier.padding(QSpacing.l))
    }
}