package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val BAR_HEIGHT = 6.dp

/**
 * Barra de progreso 0f..1f, alto 6.dp, pill. Pista surfaceMuted; relleno onAction, o negativeStrong si
 * isFull.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: BarraCapacidad.
 */
@Composable
fun QProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    isFull: Boolean = false,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val fillColor = if (isFull) QTheme.colors.negativeStrong else QTheme.colors.onAction
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(BAR_HEIGHT)
                .clip(QShapes.pill)
                .background(QTheme.colors.surfaceMuted)
                .semantics { progressBarRangeInfo = ProgressBarRangeInfo(clamped, 0f..1f) },
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clamped)
                    .clip(QShapes.pill)
                    .background(fillColor),
        )
    }
}

@Preview(showBackground = true, name = "QProgressBar")
@Composable
private fun QProgressBarPreview() {
    QarinoTheme {
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QProgressBar(progress = 0f)
            QProgressBar(progress = 0.53f)
            QProgressBar(progress = 1f, isFull = true)
        }
    }
}