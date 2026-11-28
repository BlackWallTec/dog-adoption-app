package com.app.android.presentation.view.viewsConfiguracion

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.pages.QConfiguracionPage
import com.app.android.presentation.designsystem.theme.QarinoTheme

// Menú de Ajustes: lleva a las pantallas de cada módulo.
@Composable
fun ConfiguracionScreen(
    onNavSelect: (Int) -> Unit,
    onMiCuenta: () -> Unit,
    onMisSolicitudes: () -> Unit,
    onMisProgramas: () -> Unit,
    onDatosFiscales: () -> Unit,
) {
    QConfiguracionPage(
        onMyAccountClick = onMiCuenta,
        onMyRequestsClick = onMisSolicitudes,
        onMyProgramsClick = onMisProgramas,
        onFiscalDataClick = onDatosFiscales,
        onTabSelected = { onNavSelect(it.ordinal) },
    )
}

@Preview(name = "Configuración", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ConfiguracionPreview() {
    QarinoTheme {
        ConfiguracionScreen(
            onNavSelect = {},
            onMiCuenta = {},
            onMisSolicitudes = {},
            onMisProgramas = {},
            onDatosFiscales = {},
        )
    }
}