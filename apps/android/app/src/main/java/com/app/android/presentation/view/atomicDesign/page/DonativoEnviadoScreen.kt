package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.molecule.ConfirmationHeader
import com.app.android.presentation.view.atomicDesign.molecule.InfoMessageCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh

// PAGE: HU46 - Confirmación después de enviar el donativo
// CA-01: el donativo queda "Pendiente de validación" (no decimos que ya se recibió)
@Composable
fun DonativoEnviadoScreen(
    monto: String,
    onBackClick: () -> Unit,
    onVolverDonarClick: () -> Unit,
    onVerMisDonativosClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 2, // 2 = Donar
    onNavSelect: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = {
            AppTopBar(
                titulo = "Donativo",
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            NavBar(
                selected = navSeleccionado,
                onSelect = onNavSelect,
            )
        },
    ) { paddingInterno ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            ConfirmationHeader(
                titulo = "Registramos tu donativo, lo revisaremos pronto.",
                subtitulo = "Estamos revisando tu transferencia. Te avisaremos cuando esté confirmada.",
                modifier = Modifier.padding(top = 24.dp),
            )

            // Resumen del donativo
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Donativo registrado",
                    color = InverseSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = monto,
                    color = InverseSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Transferencia · Pendiente de validación",
                    color = InverseSurface.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                )
            }

            InfoMessageCard(
                mensaje = "Puedes consultar el estado de tu transferencia en Mis donativos.",
                icono = Icons.Outlined.Info,
            )

            Column(
                modifier = Modifier.padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AppButton(
                    text = "Volver a Donar",
                    onClick = onVolverDonarClick,
                    modifier = Modifier.fillMaxWidth(),
                )
                AppButton(
                    text = "Ver mis donativos",
                    onClick = onVerMisDonativosClick,
                    variant = ButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "HU46 - Donativo enviado", heightDp = 1000)
@Composable
fun DonativoEnviadoScreenPreview() {
    AndroidTheme {
        DonativoEnviadoScreen(
            monto = "$200 MXN",
            onBackClick = {},
            onVolverDonarClick = {},
            onVerMisDonativosClick = {},
        )
    }
}