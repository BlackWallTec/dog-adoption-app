package com.app.android.presentation.view.viewsPerfiles

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesPerfiles.CodigoPostal
import java.util.Calendar
import java.util.Locale

@Composable
internal fun FechaNacimientoField(value: String, onChange: (String) -> Unit, enabled: Boolean, error: String?) {
    val context = LocalContext.current
    var abierto by remember { mutableStateOf(false) }
    // Se elimina el diálogo si la pantalla abandona composición.
    if (abierto) DisposableEffect(context) {
        val hoy = Calendar.getInstance()
        val partes = value.split('-').mapNotNull { it.toIntOrNull() }
        val dialogo = DatePickerDialog(context, { _, anio, mes, dia ->
            onChange(String.format(Locale.ROOT, "%04d-%02d-%02d", anio, mes + 1, dia))
            abierto = false
        }, partes.getOrNull(0)?.takeIf { it in 1900..hoy.get(Calendar.YEAR) } ?: 2000,
            (partes.getOrNull(1)?.takeIf { it in 1..12 } ?: 1) - 1,
            partes.getOrNull(2)?.takeIf { it in 1..31 } ?: 1)
        dialogo.datePicker.maxDate = hoy.timeInMillis
        dialogo.setOnDismissListener { abierto = false }
        dialogo.show()
        onDispose { dialogo.setOnDismissListener(null); dialogo.dismiss() }
    }
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text("Fecha de nacimiento") },
        singleLine = true, enabled = enabled, isError = error != null,
        supportingText = { Text(error ?: "AAAA-MM-DD") },
        modifier = Modifier.fillMaxWidth().testTag("register_fecha_nacimiento"), shape = RoundedCornerShape(16.dp),
        trailingIcon = { IconButton(onClick = { abierto = true }, enabled = enabled) {
            Icon(Icons.Outlined.DateRange, "Elegir fecha de nacimiento")
        } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectorColonia(
    codigoPostal: String,
    opciones: List<CodigoPostal>,
    idSeleccionado: String,
    cargando: Boolean,
    error: String?,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    var expanded by remember(codigoPostal) { mutableStateOf(false) }
    val seleccionada = opciones.firstOrNull { it.idCodigoPostal == idSeleccionado }
    val habilitado = enabled && !cargando && opciones.isNotEmpty()
    ExposedDropdownMenuBox(expanded = expanded && habilitado, onExpandedChange = { if (habilitado) expanded = it }) {
        OutlinedTextField(
            value = seleccionada?.colonia.orEmpty(), onValueChange = {}, readOnly = true,
            enabled = habilitado, label = { Text("Colonia") }, isError = error != null,
            supportingText = { Text(error ?: when {
                cargando -> "Buscando colonias…"
                codigoPostal.length != 5 -> "Escribe un código postal de 5 dígitos."
                else -> "Selecciona la colonia que corresponde a tu dirección."
            }) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded && habilitado) },
            modifier = Modifier.fillMaxWidth().testTag("register_colonia")
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = habilitado),
        )
        ExposedDropdownMenu(expanded = expanded && habilitado, onDismissRequest = { expanded = false }) {
            opciones.forEach { opcion -> DropdownMenuItem(
                text = { Text("${opcion.colonia}, ${opcion.municipio}, ${opcion.estado}") },
                onClick = { onSelect(opcion.idCodigoPostal); expanded = false },
            ) }
        }
    }
}

@Composable
internal fun PerfilesError(message: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }) {
        Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
    }
}
