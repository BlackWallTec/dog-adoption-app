package com.app.android.presentation.view.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// No planemos usar modo oscuro por ahora
// private val DarkColorScheme =
//    darkColorScheme(
//        primary = Purple80,
//        secondary = PurpleGrey80,
//        tertiary = Pink80,
//    )

private val LightColorScheme =
    lightColorScheme(
        primary = Primary,
        primaryContainer = PrimaryContainer,
        secondary = Secondary,
        secondaryContainer = SecondaryContainer,
        error = Error,
        errorContainer = ErrorContainer,
        surface = Surface,
        surfaceContainerLow = SurfaceContainerLow,
        surfaceContainer = SurfaceContainer,
        surfaceContainerHigh = SurfaceContainerHigh,
        inverseSurface = InverseSurface,
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
     */
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun AndroidTheme(
    // darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    // dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}
