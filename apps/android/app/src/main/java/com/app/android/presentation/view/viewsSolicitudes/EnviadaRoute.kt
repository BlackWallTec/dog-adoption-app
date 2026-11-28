package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.designsystem.pages.QSolicitudEnviadaPage
import com.app.android.presentation.viewmodel.viewModelSolicitudes.EnviadaViewModel
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MascotaResumen

// Conecta EnviadaViewModel con la pagina nueva del design system
@Suppress("ktlint:standard:function-naming")
@Composable
fun EnviadaRoute(
    solicitudId: String,
    onBack: () -> Unit,
    onVerSolicitudes: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    onVerCatalogo: () -> Unit = onBack,
    viewModel: EnviadaViewModel = hiltViewModel(),
) {
    val confirmacion by viewModel.confirmacionEnviada.collectAsStateWithLifecycle()
    val mascota =
        confirmacion?.mascota ?: MascotaResumen(
            nombre = "Mascota seleccionada",
            detalle = "",
            raza = "",
        )

    QSolicitudEnviadaPage(
        requestId = solicitudId,
        pet = mascota,
        onBack = onBack,
        onMyRequestsClick = onVerSolicitudes,
        onCatalogClick = onVerCatalogo,
        onTabSelected = { onNavSelect(it.ordinal) },
    )
}
