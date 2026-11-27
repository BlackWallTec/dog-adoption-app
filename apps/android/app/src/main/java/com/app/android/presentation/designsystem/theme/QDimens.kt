package com.app.android.presentation.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Medidas de la app. NUNCA escribas números de dp sueltos para padding, radios o tamaños
 * fuera de este archivo: usa estos objetos.
 *
 * Dueño: P1 (Átomos).
 */
object QSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    // Margen lateral de TODAS las pantallas
    val screenHorizontal = 20.dp
}

object QRadius {
    val small = 12.dp // campos, recuadros de dato
    val medium = 16.dp // tarjetas, fotos
    val large = 24.dp // bottom sheet
}

object QShapes {
    val small = RoundedCornerShape(QRadius.small)
    val card = RoundedCornerShape(QRadius.medium)
    val large = RoundedCornerShape(QRadius.large)
    val pill = RoundedCornerShape(percent = 50) // botones, chips, tags
}

object QSize {
    val border = 1.dp // grosor de bordes (tags, campos, chips)
    val icon = 24.dp
    val iconSmall = 18.dp
    val buttonHeight = 48.dp
    val buttonSmallHeight = 36.dp
    val fieldHeight = 52.dp
    val chipHeight = 32.dp
    val navBarHeight = 72.dp
    val topBarLogo = 26.dp
    val topBarBlob = 200.dp
}

object QElevation {
    val bar = 6.dp // sombra de la top bar (hacia abajo) y de la nav bar (hacia arriba)
}