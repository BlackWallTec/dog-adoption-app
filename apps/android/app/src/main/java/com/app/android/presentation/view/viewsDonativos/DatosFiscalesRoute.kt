package com.app.android.presentation.view.viewsDonativos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.presentation.designsystem.pages.QDatosFiscalesPage
import com.app.android.presentation.viewmodel.viewModelDonativos.DatosFiscalesUiState
import com.app.android.presentation.viewmodel.viewModelDonativos.DatosFiscalesViewModel
import com.app.android.presentation.viewmodel.viewModelDonativos.GuardarDatosFiscalesUiState
import kotlinx.coroutines.delay

private const val TIEMPO_AVISO_MS = 4000L

// ROUTE: conecta el DatosFiscalesViewModel con la page QDatosFiscalesPage (HU45)
@Composable
fun DatosFiscalesRoute(
    onBack: () -> Unit,
    onNavSelect: (Int) -> Unit = {},
    viewModel: DatosFiscalesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val guardarState by viewModel.guardarState.collectAsState()
    val context = LocalContext.current

    // 1 cuando se entra se cargan los datos
    LaunchedEffect(Unit) {
        viewModel.onLoadDatosFiscales()
    }

    // Abre los archivos del celular y el ViewModel se queda con la constancia
    val selectorArchivo =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult // se salió sin escoger
            val tipo = context.contentResolver.getType(uri)
            val (nombre, tamano) = leerArchivo(context, uri, "constancia")
            when {
                tipo == null || tipo !in TIPOS_PERMITIDOS -> viewModel.onConstanciaInvalida("Solo se aceptan archivos PDF, JPG o PNG.")
                tamano > MAX_BYTES -> viewModel.onConstanciaInvalida("El archivo pesa más de 10 MB.")
                else -> {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) {
                        viewModel.onConstanciaInvalida("No se pudo leer el archivo.")
                    } else {
                        viewModel.onConstanciaSeleccionada(
                            ConstanciaArchivo(nombre = nombre, tipo = tipo, bytes = bytes),
                            "%.1f MB · Lista para guardar".format(tamano / 1_048_576.0),
                        )
                    }
                }
            }
        }

    // Antes era snackbar: ahora el aviso sale en la page unos segundos y se quita
    LaunchedEffect(guardarState) {
        if (guardarState is GuardarDatosFiscalesUiState.Success || guardarState is GuardarDatosFiscalesUiState.Error) {
            delay(TIEMPO_AVISO_MS)
            viewModel.onMensajeMostrado()
        }
    }

    // 2 Traduccion de los dos estados a parametros para la page
    val success = uiState as? DatosFiscalesUiState.Success

    QDatosFiscalesPage(
        isLoading = uiState is DatosFiscalesUiState.Loading,
        loadError = (uiState as? DatosFiscalesUiState.Error)?.mensaje,
        data = success?.datos,
        hasSavedData = success?.yaGuardados ?: false,
        errors = success?.errores,
        onRfcChange = viewModel::onRfcChange,
        onBusinessNameChange = viewModel::onRazonSocialChange,
        onTaxRegimeChange = viewModel::onRegimenChange,
        onPostalCodeChange = viewModel::onCodigoPostalChange,
        onBillingEmailChange = viewModel::onCorreoChange,
        onPickCertificate = { selectorArchivo.launch(TIPOS_PERMITIDOS) },
        onSave = { viewModel.onGuardar() },
        onRetry = { viewModel.onLoadDatosFiscales() },
        onBack = onBack,
        onTabSelected = { onNavSelect(it.ordinal) },
        newCertificateName = success?.constanciaNueva?.nombre,
        certificateInfo = success?.infoConstancia,
        isSaving = guardarState is GuardarDatosFiscalesUiState.Loading,
        isSaved = guardarState is GuardarDatosFiscalesUiState.Success,
        saveError = (guardarState as? GuardarDatosFiscalesUiState.Error)?.mensaje,
    )
}
