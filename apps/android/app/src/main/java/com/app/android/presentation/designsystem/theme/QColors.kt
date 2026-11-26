// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Colores de la app. NUNCA escribas Color(0x...) fuera de este archivo:
 * usa QTheme.colors.<nombre>.
 *
 * Dueño: P1 (Átomos).
 */
@Immutable
data class QColors(
    // Fondos
    val background: Color, // Fondo de casi todas las pantallas (arena)
    val backgroundPlain: Color, // Fondo de las pantallas de detalle (blanco)
    val surface: Color, // Tarjetas, campos, buscador
    val surfaceMuted: Color, // Recuadros dentro de una tarjeta (Edad, Sexo...), pista de barras
    // Marca (amarillo): top bar y acentos. NUNCA en botones.
    val brand: Color,
    val brandStrong: Color,
    val brandDark: Color,
    // Acción: TODOS los botones, filtro seleccionado, pestaña activa
    val action: Color,
    val onAction: Color,
    val onActionStrong: Color,
    // Estados
    val positive: Color,
    val onPositive: Color,
    val pending: Color,
    val onPending: Color,
    val negative: Color,
    val onNegative: Color,
    val negativeStrong: Color,
    // Tags (etiquetas descriptivas)
    val tag: Color,
    val tagBorder: Color,
    // Texto
    val textPrimary: Color,
    val textSecondary: Color,
    // Bordes y otros
    val border: Color,
    val fieldBorder: Color,
    val disabledContent: Color,
    val error: Color,
    val shadow: Color,
)

val QarinoColors =
    QColors(
        background = Color(0xFFF3EEDF),
        backgroundPlain = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceMuted = Color(0xFFF3EEDF),
        brand = Color(0xFFFCF0C8),
        brandStrong = Color(0xFFF6D675),
        brandDark = Color(0xFFEFC95A),
        action = Color(0xFFDCEAF8),
        onAction = Color(0xFF2F6FB5),
        onActionStrong = Color(0xFF1F4F86),
        positive = Color(0xFFBFE3CB),
        onPositive = Color(0xFF1F5C3A),
        pending = Color(0xFFFCF0C8),
        onPending = Color(0xFF2E2A22),
        negative = Color(0xFFF6D5CC),
        onNegative = Color(0xFF7A2E1F),
        negativeStrong = Color(0xFFE3A08F),
        tag = Color(0xFFFFFBF2),
        tagBorder = Color(0xFF2E2A22),
        textPrimary = Color(0xFF2E2A22),
        textSecondary = Color(0xFF6E685C),
        border = Color(0xFFE6DFCF),
        fieldBorder = Color(0xFFCFC6B2),
        disabledContent = Color(0xFF9A948A),
        error = Color(0xFFBA1A1A),
        shadow = Color(0xFFB8AC90),
    )
