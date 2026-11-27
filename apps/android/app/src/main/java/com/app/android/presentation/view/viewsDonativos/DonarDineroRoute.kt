package com.app.android.presentation.view.viewsDonativos

import android.content.ClipData
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.presentation.designsystem.pages.QDonarDineroPage
import com.app.android.presentation.viewmodel.viewModelDonativos.DonarDineroUiState
import com.app.android.presentation.viewmodel.viewModelDonativos.DonarDineroViewModel
import kotlinx.coroutines.launch

// DATOS TEMPORALES: los reales los captura el admin en la HU42
private val datosBancarios =
    listOf(
        "Banco" to "[BANCO]",
        "Titular" to "[TITULAR]",
        "CLABE" to "[CLABE]",
        "Cuenta" to "[CUENTA]",
    )

// ROUTE: conecta el DonarDineroViewModel con la page QDonarDineroPage (HU46)
@Composable
fun DonarDineroRoute(
    onBack: () -> Unit, // la pestaña Donar ya no tiene flecha, se queda para no mover la nav
    onEnviado: (String) -> Unit,
    onNavSelect: (Int) -> Unit = {},
    onVerMisDonativos: () -> Unit = {},
    viewModel: DonarDineroViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val portapapeles = LocalClipboard.current
    val scope = rememberCoroutineScope()

    // El archivo se queda aqui, la page solo enseña el nombre o el error
    var comprobante by remember { mutableStateOf<ComprobanteArchivo?>(null) }
    var errorComprobante by remember { mutableStateOf<String?>(null) }

    // Abre los archivos del celular (no necesita permisos)
    val selectorArchivo =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult // se salió sin escoger
            val tipo = context.contentResolver.getType(uri)
            val (nombre, tamano) = leerArchivo(context, uri, "comprobante")
            comprobante = null
            errorComprobante =
                when {
                    tipo == null || tipo !in TIPOS_PERMITIDOS -> "Solo se aceptan archivos JPG, PNG o PDF."
                    tamano > MAX_BYTES -> "El archivo pesa más de 10 MB."
                    else -> {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        if (bytes == null) {
                            "No se pudo leer el archivo."
                        } else {
                            comprobante = ComprobanteArchivo(nombre = nombre, tipo = tipo, bytes = bytes)
                            null
                        }
                    }
                }
        }

    // CA-01: ya quedó en la BD, se pasa a la confirmación
    LaunchedEffect(uiState) {
        (uiState as? DonarDineroUiState.Enviado)?.let {
            viewModel.onEnviadoMostrado()
            onEnviado(it.monto)
        }
    }

    QDonarDineroPage(
        bankData = datosBancarios,
        receiptFileName = comprobante?.nombre,
        receiptError = errorComprobante,
        onPickReceipt = { selectorArchivo.launch(TIPOS_PERMITIDOS) },
        onRemoveReceipt = {
            comprobante = null
            errorComprobante = null
        },
        onCopy = { etiqueta, valor ->
            scope.launch { portapapeles.setClipEntry(ClipEntry(ClipData.newPlainText(etiqueta, valor))) }
            Toast.makeText(context, "Copiado", Toast.LENGTH_SHORT).show()
        },
        onSubmit = { monto, referencia ->
            // la page ya revisó que haya monto y comprobante
            comprobante?.let { viewModel.onEnviar(monto, referencia, it) }
        },
        onMyDonationsClick = onVerMisDonativos,
        onTabSelected = { onNavSelect(it.ordinal) },
        isSending = uiState is DonarDineroUiState.Enviando,
        sendError = (uiState as? DonarDineroUiState.Error)?.mensaje,
    )
}
