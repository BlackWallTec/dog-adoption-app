// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LocalQColors = staticCompositionLocalOf { QarinoColors }

/** Acceso a los colores: QTheme.colors.action, QTheme.colors.textPrimary, etc. */
object QTheme {
    val colors: QColors
        @Composable
        @ReadOnlyComposable
        get() = LocalQColors.current
}

// Esquema de Material para que los componentes de Material (Checkbox, Switch, cursor, etc.)
// salgan con los colores de la app.
private val QarinoMaterialScheme =
    lightColorScheme(
        primary = QarinoColors.onAction,
        onPrimary = Color.White,
        primaryContainer = QarinoColors.action,
        onPrimaryContainer = QarinoColors.onActionStrong,
        secondary = QarinoColors.onAction,
        onSecondary = Color.White,
        secondaryContainer = QarinoColors.action,
        onSecondaryContainer = QarinoColors.onActionStrong,
        tertiary = QarinoColors.onPositive,
        tertiaryContainer = QarinoColors.positive,
        onTertiaryContainer = QarinoColors.onPositive,
        background = QarinoColors.background,
        onBackground = QarinoColors.textPrimary,
        surface = QarinoColors.surface,
        onSurface = QarinoColors.textPrimary,
        surfaceVariant = QarinoColors.surfaceMuted,
        onSurfaceVariant = QarinoColors.textSecondary,
        outline = QarinoColors.fieldBorder,
        outlineVariant = QarinoColors.border,
        error = QarinoColors.error,
        errorContainer = QarinoColors.negative,
        onErrorContainer = QarinoColors.onNegative,
    )

/**
 * Tema de la app. Reemplaza a AndroidTheme en MainActivity (lo hace P5).
 *
 * Dueño: P1 (Átomos).
 */
@Composable
fun QarinoTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalQColors provides QarinoColors) {
        MaterialTheme(
            colorScheme = QarinoMaterialScheme,
            content = content,
        )
    }
}
