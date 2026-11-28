package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.app.android.presentation.designsystem.pages.QDonativoEnviadoPage

// PAGE: HU46 - Confirmación después de enviar el donativo
// CA-01: el donativo queda "Pendiente de validación" (no decimos que ya se recibió)
// Solo delega a la página nueva del design system; la navegación la sigue llamando igual.
@Composable
fun DonativoEnviadoScreen(
    monto: String,
    onBackClick: () -> Unit,
    onVolverDonarClick: () -> Unit,
    onVerMisDonativosClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavSelect: (Int) -> Unit = {},
) {
    QDonativoEnviadoPage(
        amount = monto,
        onBack = onBackClick,
        onDonateAgainClick = onVolverDonarClick,
        onMyDonationsClick = onVerMisDonativosClick,
        onTabSelected = { onNavSelect(it.ordinal) },
        modifier = modifier,
    )
}