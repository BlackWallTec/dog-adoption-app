package com.app.android.presentation.view.viewsDonativos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.presentation.view.atomicDesign.page.DonarDineroScreen
import com.app.android.presentation.viewmodel.viewModelDonativos.DonarDineroUiState
import com.app.android.presentation.viewmodel.viewModelDonativos.DonarDineroViewModel

// ROUTE: conecta el DonarDineroViewModel con la page DonarDineroScreen (HU46)
@Composable
fun DonarDineroRoute(
    idUsuario: String,
    onBack: () -> Unit,
    onEnviado: (String) -> Unit,
    onNavSelect: (Int) -> Unit = {},
    viewModel: DonarDineroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // CA-01: ya quedó en la BD, se pasa a la confirmación
    LaunchedEffect(uiState) {
        (uiState as? DonarDineroUiState.Enviado)?.let {
            viewModel.onEnviadoMostrado()
            onEnviado(it.monto)
        }
    }

    DonarDineroScreen(
        enviando = uiState is DonarDineroUiState.Enviando,
        errorEnviar = (uiState as? DonarDineroUiState.Error)?.mensaje,
        onBackClick = onBack,
        onNavSelect = onNavSelect,
        onEnviarClick = { monto, referencia, comprobante ->
            viewModel.onEnviar(idUsuario, monto, referencia, comprobante)
        },
    )
}
