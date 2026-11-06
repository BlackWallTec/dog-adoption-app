package com.app.android.presentation.view.atomicDesign.page

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.entities.entitiesDonativos.REGIMENES_FISCALES
import com.app.android.domain.useCases.useCasesDonativos.ErroresDatosFiscales
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.atomic.FileUploadCard
import com.app.android.presentation.view.atomicDesign.atomic.dashedBorder
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.Error
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainer
import com.app.android.presentation.view.ui.theme.SurfaceContainerLow

// Igual que el comprobante de la HU46: solo PDF o imágenes y máximo 10 MB
private val TIPOS_CONSTANCIA = arrayOf("application/pdf", "image/jpeg", "image/png")
private const val MAX_BYTES = 10L * 1024 * 1024

// PAGE: HU45 - Datos fiscales del donante (Configuración · Datos fiscales en Figma)
@Composable
fun DatosFiscalesScreen(
    cargando: Boolean,
    errorCarga: String?,
    datos: DatosFiscales?,
    yaGuardados: Boolean,
    constanciaNueva: String?, // nombre del archivo que escogió y aún no se guarda
    infoConstancia: String,
    errores: ErroresDatosFiscales?,
    guardando: Boolean,
    guardadoOk: Boolean,
    errorGuardar: String?,
    onRfcChange: (String) -> Unit,
    onRazonSocialChange: (String) -> Unit,
    onRegimenChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onConstanciaSeleccionada: (ConstanciaArchivo, String) -> Unit,
    onConstanciaInvalida: (String) -> Unit,
    onMensajeMostrado: () -> Unit,
    onReintentar: () -> Unit,
    onGuardarClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 4, // 4 = Configuración
    onNavSelect: (Int) -> Unit = {},
) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    // Abre los archivos del celular (no necesita permisos)
    val selectorArchivo =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri == null) return@rememberLauncherForActivityResult // se salió sin escoger
            val tipo = context.contentResolver.getType(uri)
            val (nombre, tamano) = leerInfoArchivo(context, uri)
            when {
                tipo == null || tipo !in TIPOS_CONSTANCIA -> onConstanciaInvalida("Solo se aceptan archivos PDF, JPG o PNG.")
                tamano > MAX_BYTES -> onConstanciaInvalida("El archivo pesa más de 10 MB.")
                else -> {
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) {
                        onConstanciaInvalida("No se pudo leer el archivo.")
                    } else {
                        onConstanciaSeleccionada(
                            ConstanciaArchivo(nombre = nombre, tipo = tipo, bytes = bytes),
                            "%.1f MB · Lista para guardar".format(tamano / 1_048_576.0),
                        )
                    }
                }
            }
        }

    // Avisos de guardado abajo de la pantalla
    LaunchedEffect(guardadoOk, errorGuardar) {
        val mensaje =
            when {
                guardadoOk -> "Tus datos fiscales se guardaron."
                errorGuardar != null -> errorGuardar
                else -> null
            }
        if (mensaje != null) {
            snackbar.showSnackbar(mensaje)
            onMensajeMostrado()
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = {
            AppTopBar(
                titulo = "Datos fiscales",
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            NavBar(
                selected = navSeleccionado,
                onSelect = onNavSelect,
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { paddingInterno ->
        when {
            cargando -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingInterno),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Secondary)
                }
            }

            errorCarga != null || datos == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(paddingInterno).padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ErrorMessage(text = errorCarga ?: "No se pudieron cargar tus datos fiscales.")
                    AppButton(
                        text = "Reintentar",
                        onClick = onReintentar,
                        variant = ButtonVariant.Secondary,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            else -> {
                FormularioDatosFiscales(
                    datos = datos,
                    yaGuardados = yaGuardados,
                    constanciaNueva = constanciaNueva,
                    infoConstancia = infoConstancia,
                    errores = errores ?: ErroresDatosFiscales(),
                    guardando = guardando,
                    onRfcChange = onRfcChange,
                    onRazonSocialChange = onRazonSocialChange,
                    onRegimenChange = onRegimenChange,
                    onCodigoPostalChange = onCodigoPostalChange,
                    onCorreoChange = onCorreoChange,
                    onSubirClick = { selectorArchivo.launch(TIPOS_CONSTANCIA) },
                    onGuardarClick = onGuardarClick,
                    onCancelarClick = onBackClick,
                    modifier = Modifier.padding(paddingInterno),
                )
            }
        }
    }
}

@Composable
private fun FormularioDatosFiscales(
    datos: DatosFiscales,
    yaGuardados: Boolean,
    constanciaNueva: String?,
    infoConstancia: String,
    errores: ErroresDatosFiscales,
    guardando: Boolean,
    onRfcChange: (String) -> Unit,
    onRazonSocialChange: (String) -> Unit,
    onRegimenChange: (String) -> Unit,
    onCodigoPostalChange: (String) -> Unit,
    onCorreoChange: (String) -> Unit,
    onSubirClick: () -> Unit,
    onGuardarClick: () -> Unit,
    onCancelarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Guarda la información que usarás al solicitar un comprobante fiscal (CFDI).",
            color = InverseSurface,
            fontSize = 16.sp,
        )

        // Aviso solo si nunca ha completado sus datos
        if (!yaGuardados) {
            Text(
                text = "Aún no has guardado tus datos fiscales. Consulta tu constancia para completarlos.",
                color = InverseSurface,
                fontSize = 14.sp,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(SurfaceContainer, RoundedCornerShape(12.dp))
                        .padding(12.dp),
            )
        }

        // Todos los campos en una sola tarjeta con rayitas entre renglones (como en Figma)
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(SurfaceContainerLow, RoundedCornerShape(16.dp)),
        ) {
            CampoFiscal(
                etiqueta = "RFC",
                valor = datos.rfc,
                onValueChange = onRfcChange,
                placeholder = "Ingresa tu RFC",
                error = errores.rfc,
                mayusculas = true,
            )
            HorizontalDivider(color = Surface)
            CampoFiscal(
                etiqueta = "Razón social",
                valor = datos.razonSocial,
                onValueChange = onRazonSocialChange,
                placeholder = "Como aparece en tu constancia",
                error = errores.razonSocial,
            )
            HorizontalDivider(color = Surface)
            SelectorRegimen(
                claveSeleccionada = datos.regimenFiscal,
                onRegimenChange = onRegimenChange,
                error = errores.regimenFiscal,
            )
            HorizontalDivider(color = Surface)
            CampoFiscal(
                etiqueta = "Código postal",
                valor = datos.codigoPostal,
                onValueChange = onCodigoPostalChange,
                placeholder = "Código postal fiscal",
                error = errores.codigoPostal,
                teclado = KeyboardType.Number,
            )
            HorizontalDivider(color = Surface)
            CampoFiscal(
                etiqueta = "Correo de facturación",
                valor = datos.correoFacturacion,
                onValueChange = onCorreoChange,
                placeholder = "correo@ejemplo.com",
                error = errores.correoFacturacion,
                icono = Icons.Outlined.Email,
                teclado = KeyboardType.Email,
            )
        }

        Text(
            text = "Constancia de situación fiscal",
            color = InverseSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        // Nueva = la que acaba de escoger; si no, la que ya estaba guardada; si no, el recuadro vacío
        when {
            constanciaNueva != null -> {
                FileUploadCard(
                    title = "Constancia",
                    fileName = constanciaNueva,
                    fileInfo = infoConstancia,
                    onClick = onSubirClick,
                )
            }
            datos.constanciaUrl != null -> {
                FileUploadCard(
                    title = "Constancia",
                    fileName = datos.constanciaUrl.substringAfterLast("/"),
                    fileInfo = "Guardada · Toca para cambiarla",
                    onClick = onSubirClick,
                )
            }
            else -> {
                SubirConstanciaVacia(onClick = onSubirClick)
            }
        }
        if (errores.constancia != null) {
            ErrorMessage(text = errores.constancia)
        }

        Text(
            text = "Revisa que tu RFC, razón social y código postal coincidan con tu constancia.",
            color = InverseSurface.copy(alpha = 0.7f),
            fontSize = 13.sp,
        )

        Column(
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppButton(
                text = "Guardar datos fiscales",
                onClick = onGuardarClick,
                isLoading = guardando,
                loadingDescription = "Guardando",
                modifier = Modifier.fillMaxWidth(),
            )
            AppButton(
                text = "Cancelar",
                onClick = onCancelarClick,
                variant = ButtonVariant.Outlined,
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// Renglón de la tarjeta: etiqueta chiquita arriba y lo que escribe abajo
@Composable
private fun CampoFiscal(
    etiqueta: String,
    valor: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    error: String?,
    icono: ImageVector? = null,
    teclado: KeyboardType = KeyboardType.Text,
    mayusculas: Boolean = false,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(
            text = etiqueta,
            color = InverseSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icono != null) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = Secondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            BasicTextField(
                value = valor,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(color = InverseSurface, fontSize = 16.sp),
                cursorBrush = SolidColor(InverseSurface),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = teclado,
                        capitalization = if (mayusculas) KeyboardCapitalization.Characters else KeyboardCapitalization.Sentences,
                    ),
                modifier = Modifier.weight(1f),
                decorationBox = { campo ->
                    // placeholder mientras está vacío
                    if (valor.isEmpty()) {
                        Text(text = placeholder, color = InverseSurface.copy(alpha = 0.8f), fontSize = 16.sp)
                    }
                    campo()
                },
            )
        }
        if (error != null) {
            Text(
                text = error,
                color = Error,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// Renglón del régimen: se pica y sale la lista del SAT
@Composable
private fun SelectorRegimen(
    claveSeleccionada: String,
    onRegimenChange: (String) -> Unit,
    error: String?,
) {
    var abierto by remember { mutableStateOf(false) }
    val seleccionado = REGIMENES_FISCALES.firstOrNull { it.first == claveSeleccionada }

    Box {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { abierto = true }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Régimen fiscal",
                color = InverseSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.size(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text =
                        when {
                            seleccionado != null -> "${seleccionado.first} · ${seleccionado.second}"
                            claveSeleccionada.isNotBlank() -> claveSeleccionada // clave que no está en la lista
                            else -> "Selecciona tu régimen"
                        },
                    color = InverseSurface.copy(alpha = if (claveSeleccionada.isBlank()) 0.8f else 1f),
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = "Ver regímenes",
                    tint = Secondary,
                )
            }
            if (error != null) {
                Text(
                    text = error,
                    color = Error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            containerColor = SurfaceContainerLow,
        ) {
            REGIMENES_FISCALES.forEach { (clave, descripcion) ->
                DropdownMenuItem(
                    text = { Text("$clave · $descripcion", color = InverseSurface, fontSize = 14.sp) },
                    onClick = {
                        onRegimenChange(clave)
                        abierto = false
                    },
                )
            }
        }
    }
}

// Recuadro punteado para subir la constancia (diseño de Figma)
@Composable
private fun SubirConstanciaVacia(onClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .dashedBorder(color = InverseSurface.copy(alpha = 0.3f), strokeWidth = 1.dp, cornerRadius = 16.dp)
                .clickable(onClick = onClick)
                .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.FileUpload,
            contentDescription = null,
            tint = Secondary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = "Subir constancia",
            color = InverseSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "PDF, JPG o PNG · Máx. 10 MB",
            color = InverseSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

// Saca el nombre y el peso del archivo que escogió el usuario
private fun leerInfoArchivo(
    context: Context,
    uri: Uri,
): Pair<String, Long> {
    var nombre = "constancia"
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

@Preview(showBackground = true, name = "HU45 - Datos fiscales vacío", heightDp = 1400)
@Composable
fun DatosFiscalesScreenVacioPreview() {
    AndroidTheme {
        DatosFiscalesScreen(
            cargando = false,
            errorCarga = null,
            datos = DatosFiscales(correoFacturacion = "sofia.garcia@correo.com"),
            yaGuardados = false,
            constanciaNueva = null,
            infoConstancia = "",
            errores = null,
            guardando = false,
            guardadoOk = false,
            errorGuardar = null,
            onRfcChange = {},
            onRazonSocialChange = {},
            onRegimenChange = {},
            onCodigoPostalChange = {},
            onCorreoChange = {},
            onConstanciaSeleccionada = { _, _ -> },
            onConstanciaInvalida = {},
            onMensajeMostrado = {},
            onReintentar = {},
            onGuardarClick = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, name = "HU45 - Datos fiscales con errores", heightDp = 1400)
@Composable
fun DatosFiscalesScreenErroresPreview() {
    AndroidTheme {
        DatosFiscalesScreen(
            cargando = false,
            errorCarga = null,
            datos = DatosFiscales(rfc = "GALS95", regimenFiscal = "626", correoFacturacion = "sofia.garcia@correo.com"),
            yaGuardados = false,
            constanciaNueva = null,
            infoConstancia = "",
            errores =
                ErroresDatosFiscales(
                    rfc = "Revisa tu RFC, debe tener 12 o 13 caracteres",
                    razonSocial = "Escribe tu razón social",
                    codigoPostal = "El código postal tiene 5 números",
                    constancia = "Sube tu constancia de situación fiscal",
                ),
            guardando = false,
            guardadoOk = false,
            errorGuardar = null,
            onRfcChange = {},
            onRazonSocialChange = {},
            onRegimenChange = {},
            onCodigoPostalChange = {},
            onCorreoChange = {},
            onConstanciaSeleccionada = { _, _ -> },
            onConstanciaInvalida = {},
            onMensajeMostrado = {},
            onReintentar = {},
            onGuardarClick = {},
            onBackClick = {},
        )
    }
}
