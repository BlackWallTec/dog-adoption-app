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
import com.app.android.presentation.view.atomicDesign.atomic.CheckBox
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.molecule.LabeledOptionPicker
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.IndicadorPasos
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.viewmodel.viewModelSolicitudes.Acceptances

// PAGE: paso 4 de la solicitud de adopcion
@Composable
fun CompromisoScreen(
    cargando: Boolean,
    errorCarga: String?,
    datos: Compromiso?,
    errores: CompromisoErrors?,
    onDatosChange: (Compromiso) -> Unit,
    acceptances: Acceptances, // los 3 checkbox de compromiso
    onAcceptancesChange: (Acceptances) -> Unit,
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
                        acceptances = acceptances,
                        onAcceptancesChange = onAcceptancesChange,
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
    acceptances: Acceptances,
    onAcceptancesChange: (Acceptances) -> Unit,
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
                placeholder = "Escribe los cuidados que le darías día a día...",
                minLines = 3,
            )
            errores?.petNeeds?.let { ErrorMessage(text = it) }
        }
        Column {
            LabeledWritableCard(
                label = "¿Qué opinas de la esterilización?",
                value = datos.petSterilization,
                onValueChange = { onDatosChange(datos.copy(petSterilization = it)) },
                placeholder = "Cuéntanos qué opinas y por qué...",
                minLines = 3,
            )
            errores?.petSterilization?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledOptionPicker(
                label = "¿Tienes un veterinario de confianza?",
                options = listOf("Sí", "No"),
                // true = Sí (0), false = No (1), null = nada seleccionado (-1)
                selectedIndex =
                    when (datos.hasVet) {
                        true -> 0
                        false -> 1
                        null -> -1
                    },
                onOptionSelected = { onDatosChange(datos.copy(hasVet = it == 0)) },
            )
            errores?.hasVet?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledWritableCard(
                label = "Si tienes veterinario, ¿cuál es? Si no, ¿cómo piensas conseguir uno?",
                value = datos.vetDetail,
                onValueChange = { onDatosChange(datos.copy(vetDetail = it)) },
                placeholder = "Escribe tu respuesta...",
            )
            LabeledWritableCard(
                label = "¿Cuántas horas al día estaría solo el perro y quién lo cuidaría si viajas o sales de vacaciones?",
                value = datos.timeAlone,
                onValueChange = { onDatosChange(datos.copy(timeAlone = it)) },
                placeholder = "Nombre del veterinario o clínica, o cómo lo buscarías...",
                minLines = 3,
            )
            errores?.vetDetail?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledWritableCard(
                label = "Describe un día normal para tu mascota (paseos, comida, juego, dónde duerme)",
                value = datos.dailyRoutine,
                onValueChange = { onDatosChange(datos.copy(dailyRoutine = it)) },
                placeholder = "Cuéntanos cómo sería su día, de la mañana a la noche...",
                minLines = 3,
            )
            errores?.dailyRoutine?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledWritableCard(
                label = "Si tu mascota ladra mucho, rompe cosas o se porta agresiva con otros perros, ¿qué harías?",
                value = datos.dogManagement,
                onValueChange = { onDatosChange(datos.copy(dogManagement = it)) },
                placeholder = "Cuéntanos qué harías, paso a paso...",
                minLines = 3,
            )
            errores?.dogManagement?.let { ErrorMessage(text = it) }
        }

        Text(
            text = "Mis compromisos",
            color = InverseSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        CheckBox(
            checked = acceptances.acceptsReturn,
            onCheckedChange = { onAcceptancesChange(acceptances.copy(acceptsReturn = it)) },
            text = @Suppress("ktlint:standard:max-line-length")
            "Si en algún momento no puedo quedarme con mi mascota, me comprometo a devolverla a la organización y no regalarla, venderla ni abandonarla.",
            modifier = Modifier.fillMaxWidth(),
        )

        CheckBox(
            checked = acceptances.petSpending,
            onCheckedChange = { onAcceptancesChange(acceptances.copy(petSpending = it)) },
            text = "Entiendo que la adopción implica gastos continuos (alimento, vacunas, desparasitación y consultas veterinarias).",
            modifier = Modifier.fillMaxWidth(),
        )

        CheckBox(
            checked = acceptances.petFollowing,
            onCheckedChange = { onAcceptancesChange(acceptances.copy(petFollowing = it)) },
            text = "Acepto que me den seguimiento (visitas o fotos) después de la adopción.",
            modifier = Modifier.fillMaxWidth(),
        )

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

@Preview(showBackground = true, name = "Paso 4 - Completo")
@Composable
fun CompromisoCompletoPreview() {
    AndroidTheme {
        CompromisoScreen(
            cargando = false,
            errorCarga = null,
            datos = Compromiso(),
            errores = null,
            onDatosChange = {},
            acceptances = Acceptances(acceptsReturn = true, petSpending = true, petFollowing = true),
            onAcceptancesChange = {},
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
                    petNeeds = "Escribe tu respuesta",
                    hasVet = "Selecciona una opción",
                    vetDetail = "Escribe tu respuesta",
                    timeAlone = "Escribe tu respuesta",
                    dailyRoutine = "Escribe tu respuesta",
                    dogManagement = "Escribe tu respuesta",
                    petSterilization = "Escribe tu respuesta",
                ),
            onDatosChange = {},
            acceptances = Acceptances(acceptsReturn = true, petSpending = true, petFollowing = true),
            onAcceptancesChange = {},
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
            acceptances = Acceptances(),
            onAcceptancesChange = {},
            guardando = false,
            errorGuardar = null,
            onReintentar = {},
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}
