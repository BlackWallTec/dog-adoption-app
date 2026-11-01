package com.app.android.presentation.view.atomicDesign.atomic.basicAtomics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.ui.theme.AndroidTheme

// Analogía a los Anchors de Godot para posicionar el botón flotante
enum class StickyAnchor {
    BOTTOM_CENTER, // Flota en la parte inferior centrado
    BOTTOM_FILL, // Flota en la parte inferior ocupando todo el ancho
    TOP_CENTER, // Flota en la parte superior centrado
    TOP_FILL, // Flota en la parte superior ocupando todo el ancho
}

/*
 * MOLÉCULA: StickyButton
 * Envuelve el AppButton de los compañeros y le aplica anclaje fijo sobre la pantalla.
 *
 * PARÁMETROS:
 * - text: String (Obligatorio) -> Texto del botón.
 * - onClick: () -> Unit (Obligatorio) -> Acción del clic.
 * - modifier: Modifier -> Modificadores externos.
 * - anchor: StickyAnchor -> Posición del anclaje (Por defecto: BOTTOM_FILL).
 * - variant: ButtonVariant -> Estilo visual del AppButton (Primary, Secondary, etc.).
 * - enabled: Boolean -> Estado habilitado/deshabilitado.
 * - isLoading: Boolean -> Estado de carga.
 * - conDegradadoFondo: Boolean -> Agrega un difuminado sutil para leer el contenido que hace scroll por detrás.
 * - paddingHorizontal: Dp -> Margen lateral (Por defecto: 16.dp).
 * - paddingVertical: Dp -> Margen vertical (Por defecto: 16.dp).
 */
@Composable
fun StickyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    anchor: StickyAnchor = StickyAnchor.BOTTOM_FILL,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    conDegradadoFondo: Boolean = true,
    paddingHorizontal: Dp = 16.dp,
    paddingVertical: Dp = 16.dp,
) {
    // Mapeo del Anchor Enum hacia la Alineación nativa de Compose
    val alignment =
        when (anchor) {
            StickyAnchor.BOTTOM_CENTER, StickyAnchor.BOTTOM_FILL -> Alignment.BottomCenter
            StickyAnchor.TOP_CENTER, StickyAnchor.TOP_FILL -> Alignment.TopCenter
        }

    // Determina si el botón debe extenderse o mantener su tamaño natural
    val buttonModifier =
        when (anchor) {
            StickyAnchor.BOTTOM_FILL, StickyAnchor.TOP_FILL -> Modifier.fillMaxWidth()
            StickyAnchor.BOTTOM_CENTER, StickyAnchor.TOP_CENTER -> Modifier
        }

    // Degradado sutil de fondo para separarlo del contenido del scroll
    val backgroundModifier =
        if (conDegradadoFondo) {
            val colorSuperficie = MaterialTheme.colorScheme.surface
            val brush =
                when (anchor) {
                    StickyAnchor.BOTTOM_CENTER, StickyAnchor.BOTTOM_FILL -> {
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, colorSuperficie.copy(alpha = 0.95f)),
                        )
                    }

                    StickyAnchor.TOP_CENTER, StickyAnchor.TOP_FILL -> {
                        Brush.verticalGradient(
                            colors = listOf(colorSuperficie.copy(alpha = 0.95f), Color.Transparent),
                        )
                    }
                }
            Modifier.background(brush)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .then(backgroundModifier),
        contentAlignment = alignment,
    ) {
        Box(
            modifier =
                Modifier.padding(
                    horizontal = paddingHorizontal,
                    vertical = paddingVertical,
                ),
        ) {
            AppButton(
                text = text,
                onClick = onClick,
                modifier = buttonModifier,
                variant = variant,
                enabled = enabled,
                isLoading = isLoading,
            )
        }
    }
}

// -----------------------------------------------------------------------
// PREVISUALIZACIONES (PREVIEWS)
// -----------------------------------------------------------------------

@Preview(showBackground = true, name = "Sticky Anchor: Bottom Fill (Adoptar)")
@Composable
fun StickyButtonBottomFillPreview() {
    AndroidTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            StickyButton(
                text = "Quiero Adoptar",
                onClick = {},
                anchor = StickyAnchor.BOTTOM_FILL,
                variant = ButtonVariant.Primary,
            )
        }
    }
}

@Preview(showBackground = true, name = "Sticky Anchor: Bottom Center (Filtros)")
@Composable
fun StickyButtonBottomCenterPreview() {
    AndroidTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            StickyButton(
                text = "Ver Filtros",
                onClick = {},
                anchor = StickyAnchor.BOTTOM_CENTER,
                variant = ButtonVariant.Secondary,
            )
        }
    }
}
