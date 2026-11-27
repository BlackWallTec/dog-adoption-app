package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Línea divisoria de 1.dp color border.
 *
 * Dueño: P1 (Átomos).
 */
@Composable
fun QDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = QSize.border, color = QTheme.colors.border)
}

@Preview(showBackground = true, name = "QDivider")
@Composable
private fun QDividerPreview() {
    QarinoTheme {
        QDivider(modifier = Modifier.padding(QSpacing.l))
    }
}