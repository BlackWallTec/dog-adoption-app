package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.molecule.InfoMessageCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Primary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh
import com.app.android.presentation.viewmodel.viewModelDonativos.MisDonativosUiState

// PAGE: HU49 - Mis donativos y pedir CFDI
// CA-02: el botón de CFDI solo sale si el donativo ya está validado
@Composable
fun MisDonativosScreen(
    state: MisDonativosUiState,
    onBackClick: () -> Unit,
    onReintentar: () -> Unit,
    onPedirCfdi: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNavSelect: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = { AppTopBar(titulo = "Mis donativos", onBackClick = onBackClick) },
        bottomBar = { NavBar(selected = 2, onSelect = onNavSelect) },
    ) { paddingInterno ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.mensaje?.let { mensaje ->
                item { InfoMessageCard(mensaje = mensaje, icono = Icons.Outlined.Info) }
            }
            when {
                state.cargando -> item { CircularProgressIndicator() }
                state.error != null -> item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = state.error, color = InverseSurface)
                        AppButton(text = "Reintentar", onClick = onReintentar)
                    }
                }
                state.donativos.isEmpty() -> item {
                    Text(text = "Todavía no tienes donativos.", color = InverseSurface)
                }
                else -> items(state.donativos, key = { it.id }) { donativo ->
                    TarjetaDonativo(
                        donativo = donativo,
                        enviando = state.enviandoCfdi == donativo.id,
                        onPedirCfdi = { onPedirCfdi(donativo.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaDonativo(
    donativo: Donativo,
    enviando: Boolean,
    onPedirCfdi: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "\$" + "%,.2f".format(donativo.monto) + " MXN",
            color = InverseSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "${donativo.fecha} · ${donativo.estado}",
            color = InverseSurface.copy(alpha = 0.7f),
            fontSize = 14.sp,
        )
        when {
            donativo.cfdiSolicitado -> Text(
                text = "CFDI solicitado",
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            donativo.puedePedirCfdi -> AppButton(
                text = "Pedir CFDI",
                onClick = onPedirCfdi,
                enabled = !enviando,
                isLoading = enviando,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "HU49 - Mis donativos")
@Composable
fun MisDonativosScreenPreview() {
    AndroidTheme {
        MisDonativosScreen(
            state =
                MisDonativosUiState(
                    cargando = false,
                    donativos =
                        listOf(
                            Donativo("1", 500.0, "2026-11-10", "Validado", false),
                            Donativo("2", 200.0, "2026-11-09", "Pendiente de validación", false),
                            Donativo("3", 1000.0, "2026-11-01", "Validado", true),
                        ),
                ),
            onBackClick = {},
            onReintentar = {},
            onPedirCfdi = {},
        )
    }
}