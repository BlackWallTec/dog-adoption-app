package com.app.android.presentation.view.viewsSolicitudes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.presentation.view.atomicDesign.page.EnviadaScreen
import com.app.android.presentation.view.atomicDesign.page.MascotaResumen
import com.app.android.presentation.viewmodel.viewModelSolicitudes.EnviadaViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun EnviadaRoute(
    solicitudId: String,
    onBack: () -> Unit,
    onVerSolicitudes: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    viewModel: EnviadaViewModel = hiltViewModel(),
) {
    val confirmacion by viewModel.confirmacionEnviada.collectAsStateWithLifecycle()
    val mascota =
        confirmacion?.mascota ?: MascotaResumen(
            nombre = "Mascota seleccionada",
            detalle = "",
            raza = "",
        )

    EnviadaScreen(
        solicitudId = solicitudId,
        mascota = mascota,
        onBackClick = onBack,
        onVerSolicitudesClick = onVerSolicitudes,
        onNavSelect = onNavSelect,
    )
}
