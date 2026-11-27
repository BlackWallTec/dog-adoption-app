package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QIconButton
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

// Ancho mínimo del número para que los botones no "bailen" al pasar de 9 a 10
private val VALUE_MIN_WIDTH = 32.dp

/**
 * QIconButton(QIcons.Remove) + número + QIconButton(QIcons.Add). Respeta min/max: el botón se
 * deshabilita al llegar al límite. El que llama decide qué hacer en onDecrease / onIncrease.
 *
 * Dueño: P2 (Moléculas).
 * Usa (obligatorio): QIconButton, QText.
 * Reemplaza a: SumRestButton.
 */
@Composable
fun QCounter(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QIconButton(
            icon = QIcons.Remove,
            contentDescription = "Disminuir",
            onClick = onDecrease,
            tint = QTheme.colors.onAction,
            enabled = value > min,
        )
        QText(
            text = value.toString(),
            style = QTextStyle.BodyStrong,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = VALUE_MIN_WIDTH),
        )
        QIconButton(
            icon = QIcons.Add,
            contentDescription = "Aumentar",
            onClick = onIncrease,
            tint = QTheme.colors.onAction,
            enabled = value < max,
        )
    }
}

@Preview(showBackground = true, name = "QCounter - interactivo y en sus límites")
@Composable
private fun QCounterPreview() {
    QarinoTheme {
        var count by remember { mutableIntStateOf(2) }
        Column(
            modifier = Modifier.padding(QSpacing.l),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QCounter(value = count, onDecrease = { count-- }, onIncrease = { count++ }, min = 0, max = 5)
            QCounter(value = 0, onDecrease = {}, onIncrease = {}, min = 0, max = 5)
            QCounter(value = 5, onDecrease = {}, onIncrease = {}, min = 0, max = 5)
            QCounter(value = 12, onDecrease = {}, onIncrease = {})
        }
    }
}