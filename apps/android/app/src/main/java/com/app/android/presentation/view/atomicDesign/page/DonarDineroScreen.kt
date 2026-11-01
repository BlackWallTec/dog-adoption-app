package com.app.android.presentation.view.atomicDesign.page

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.DatoResumen
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.atomic.FileUploadCard
import com.app.android.presentation.view.atomicDesign.atomic.FileUploadCardEmpty
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh
import kotlinx.coroutines.launch

// Lo que decidimos en el spike de la HU46: solo capturas o PDF del banco
private val TIPOS_PERMITIDOS = arrayOf("image/jpeg", "image/png", "application/pdf")
private const val MAX_BYTES = 10L * 1024 * 1024 // 10 MB

// PAGE: HU46 - Registrar donativo económico con comprobante
@Composable
fun DonarDineroScreen(
    onBackClick: () -> Unit,
    onEnviarClick: (String) -> Unit, // manda el monto a la siguiente pantalla
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 2, // 2 = Donar
    onNavSelect: (Int) -> Unit = {},
) {
    // ESTADO TEMPORAL: mientras no hay backend todo vive aquí
    var monto by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var comprobante by remember { mutableStateOf<Uri?>(null) }
    var nombreComprobante by remember { mutableStateOf("") }
    var infoComprobante by remember { mutableStateOf("") }
    var errorComprobante by remember { mutableStateOf<String?>(null) }
    var intentoEnviar by remember { mutableStateOf(false) } // para no marcar errores antes de tiempo

    // DATOS TEMPORALES: los reales los captura el admin en la HU42
    val datosBancarios =
        listOf(
            "Banco" to "[BANCO]",
            "Titular" to "[TITULAR]",
            "CLABE" to "[CLABE]",
            "Cuenta" to "[CUENTA]",
        )
    val portapapeles = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Abre los archivos del celular (no necesita permisos)
    val selectorArchivo =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult // el usuario se salió sin escoger
            val tipo = context.contentResolver.getType(uri)
            val (nombre, tamano) = leerArchivo(context, uri)
            when {
                tipo !in TIPOS_PERMITIDOS -> {
                    comprobante = null
                    errorComprobante = "Solo se aceptan archivos JPG, PNG o PDF."
                }
                tamano > MAX_BYTES -> {
                    comprobante = null
                    errorComprobante = "El archivo pesa más de 10 MB."
                }
                else -> {
                    comprobante = uri
                    nombreComprobante = nombre
                    infoComprobante = "%.1f MB · Listo".format(tamano / 1_048_576.0)
                    errorComprobante = null
                }
            }
        }

    val montoValido = monto.replace("$", "").replace(",", "").trim().toDoubleOrNull()?.let { it > 0 } == true

    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = {
            AppTopBar(
                titulo = "Donar dinero",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Realiza una transferencia bancaria y adjunta el comprobante para que podamos registrar tu donativo.",
                color = InverseSurface,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp),
            )

            LabeledWritableCard(
                label = "Monto donado",
                value = monto,
                onValueChange = { monto = it },
                placeholder = "$0 MXN",
                leadingIcon = Icons.Outlined.Payments,
                singleLine = true,
            )
            if (intentoEnviar && !montoValido) {
                ErrorMessage(text = "Escribe el monto que transferiste.")
            }
            LabeledWritableCard(
                label = "Referencia bancaria (opcional)",
                value = referencia,
                onValueChange = { referencia = it },
                placeholder = "Folio o referencia de tu transferencia",
                leadingIcon = Icons.Outlined.Tag,
                helperText = "Ayuda al refugio a encontrar tu transferencia más rápido.",
                singleLine = true,
            )

            Text(
                text = "Datos bancarios",
                color = InverseSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainerHigh, RoundedCornerShape(16.dp)),
            ) {
                datosBancarios.forEachIndexed { i, (etiqueta, valor) ->
                    if (i > 0) HorizontalDivider(color = Surface) // rayita entre renglones
                    FilaDatoBancario(
                        etiqueta = etiqueta,
                        valor = valor,
                        onCopiar = {
                            scope.launch {
                                portapapeles.setClipEntry(ClipEntry(ClipData.newPlainText(etiqueta, valor)))
                            }
                        },
                    )
                }
            }

            Text(
                text = "Comprobante de transferencia",
                color = InverseSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            // Sin archivo = recuadro vacío; con archivo = nombre y tamaño (tocarlo deja cambiarlo)
            if (comprobante == null) {
                FileUploadCardEmpty(
                    title = "Subir comprobante",
                    onClick = { selectorArchivo.launch(TIPOS_PERMITIDOS) },
                )
            } else {
                FileUploadCard(
                    title = "Comprobante",
                    fileName = nombreComprobante,
                    fileInfo = infoComprobante,
                    onClick = { selectorArchivo.launch(TIPOS_PERMITIDOS) },
                )
            }
            Text(
                text = "Captura o PDF de tu transferencia · JPG, PNG o PDF · Máx. 10 MB",
                color = InverseSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
            // CA-02: sin comprobante no se puede enviar
            val mensajeComprobante =
                errorComprobante
                    ?: if (intentoEnviar && comprobante == null) "Adjunta tu comprobante para enviar el donativo." else null
            if (mensajeComprobante != null) {
                ErrorMessage(text = mensajeComprobante)
            }

            AppButton(
                text = "Enviar donativo",
                onClick = {
                    intentoEnviar = true
                    if (montoValido && comprobante != null) {
                        onEnviarClick(monto.replace("$", "").replace(",", "").trim())
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

// Renglón de un dato bancario con botón para copiarlo
@Composable
private fun FilaDatoBancario(
    etiqueta: String,
    valor: String,
    onCopiar: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DatoResumen(
            etiqueta = etiqueta,
            valor = valor,
            modifier = Modifier.weight(1f), // ocupa todo menos el botón
        )
        IconButton(onClick = onCopiar) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copiar $etiqueta",
                tint = Secondary,
            )
        }
    }
}

// Saca el nombre y el peso del archivo que escogió el usuario
private fun leerArchivo(
    context: Context,
    uri: Uri,
): Pair<String, Long> {
    var nombre = "comprobante"
    var tamano = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val iNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val iTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (iNombre >= 0) nombre = cursor.getString(iNombre)
            if (iTamano >= 0) tamano = cursor.getLong(iTamano)
        }
    }
    return nombre to tamano
}

@Preview(showBackground = true, name = "HU46 - Donar dinero", heightDp = 1400)
@Composable
fun DonarDineroScreenPreview() {
    AndroidTheme {
        DonarDineroScreen(
            onBackClick = {},
            onEnviarClick = {},
        )
    }
}