package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.useCases.useCasesSolicitudes.CompromisoErrors
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.molecule.LabeledOptionPicker
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.IndicadorPasos
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// PAGE: paso 4 de la solicitud de adopcion
@Composable
fun CompromisoScreen(
    cargando: Boolean,
    errorCarga: String?,
    datos: Compromiso?,
    errores: CompromisoErrors?,
    onDatosChange: (Compromiso) -> Unit,
    guardando: Boolean,
    errorGuardar: String?,
    onReintentar: () -> Unit,
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    nombreMascota: String = "tu nueva mascota",
    navSeleccionado: Int = 3,
    onNavSelect: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = { AppTopBar(titulo = "Solicitud de adopción", onBackClick = onBackClick) },
        bottomBar = { NavBar(selected = navSeleccionado, onSelect = onNavSelect) },
    ) { paddingInterno ->
        // Box ocupa todo el espacio entre las barras
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
        ) {
            when {
                cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorCarga != null -> {
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = errorCarga,
                            color = InverseSurface,
                            fontSize = 16.sp,
                        )
                        AppButton(text = "Reintentar", onClick = onReintentar)
                    }
                }

                datos != null -> {
                    FormularioCompromiso(
                        datos = datos,
                        errores = errores,
                        onDatosChange = onDatosChange,
                        guardando = guardando,
                        errorGuardar = errorGuardar,
                        onContinuarClick = onContinuarClick,
                        nombreMascota = nombreMascota,
                    )
                }
            }
        }
    }
}

// El formulario

@Composable
private fun FormularioCompromiso(
    datos: Compromiso,
    errores: CompromisoErrors?,
    onDatosChange: (Compromiso) -> Unit,
    guardando: Boolean,
    errorGuardar: String?,
    onContinuarClick: () -> Unit,
    nombreMascota: String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        IndicadorPasos(
            pasoActual = 3,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Compromiso",
            color = InverseSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Cuéntanos cómo cuidarás a $nombreMascota. Tus respuestas nos ayudan a acompañarte en una adopción responsable.",
            color = InverseSurface,
            fontSize = 16.sp,
        )

        Column {
            LabeledWritableCard(
                label = "Menciona los cuidados básicos de una mascota",
                value = datos.petNeeds,
                onValueChange = { onDatosChange(datos.copy(petNeeds = it)) },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            errores?.petNeeds?.let { ErrorMessage(text = it) }
        }
        Column {
            LabeledWritableCard(
                label = "¿Qué opinas de la esterilización?",
                value = datos.petSterilization,
                onValueChange = { onDatosChange(datos.copy(petSterilization = it)) },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            errores?.petSterilization?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledOptionPicker(
                label = "¿Nos permites dar seguimiento (visitas o fotos)?",
                options = listOf("Sí", "No"),
                // true = Sí (0), false = No (1), null = nada seleccionado (-1)
                selectedIndex =
                    when (datos.petFollowing) {
                        true -> 0
                        false -> 1
                        null -> -1
                    },
                onOptionSelected = { onDatosChange(datos.copy(petFollowing = it == 0)) },
            )
            errores?.petFollowing?.let { ErrorMessage(text = it) }
        }

        if (errorGuardar != null) {
            Text(
                text = errorGuardar,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
            )
        }

        AppButton(
            text = "Continuar a Documentos",
            onClick = onContinuarClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
        )
    }
}

@Preview(showBackground = true, name = "Paso 4 - Compromiso")
@Composable
fun CompromisoScreenPreview() {
    var datos by remember { mutableStateOf(Compromiso()) }
    AndroidTheme {
        CompromisoScreen(
            cargando = false,
            errorCarga = null,
            datos = datos,
            errores = null,
            onDatosChange = { datos = it }, // el estado vive en el preview
            guardando = false,
            errorGuardar = null,
            onReintentar = {},
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Paso 4 - Con errores")
@Composable
fun CompromisoConErroresPreview() {
    AndroidTheme {
        CompromisoScreen(
            cargando = false,
            errorCarga = null,
            datos = Compromiso(),
            errores =
                CompromisoErrors(
                    petNeeds = "Menciona al menos un cuidado básico",
                    petSterilization = "Selecciona una opción",
                ),
            onDatosChange = {},
            guardando = false,
            errorGuardar = null,
            onReintentar = {},
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Paso 4 - Cargando")
@Composable
fun CompromisoCargandoPreview() {
    AndroidTheme {
        CompromisoScreen(
            cargando = true,
            errorCarga = null,
            datos = null,
            errores = null,
            onDatosChange = {},
            guardando = false,
            errorGuardar = null,
            onReintentar = {},
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}
