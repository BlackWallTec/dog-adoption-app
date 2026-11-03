package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.useCases.useCasesSolicitudes.TuHogarErrors
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.molecule.LabeledOptionPicker
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.molecule.SumRestButton
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

private val opcionesSiNo = listOf("Si", "No")
private val opcionesVivienda = listOf("Casa", "Departamento", "Otro")

// Picker trabaja entre opciones (0 = primera opción,
// 1 = segunda, -1 = ninguna)
// pero el dominio trabaja con Boolean? (true = si (0),
// false = no (1), null = nada (-1))
private fun Boolean?.aIndice(): Int =
    when (this) {
        true -> 0
        false -> 1
        null -> -1
    }

// PAGE: paso 3 de la solicitud de adopcion
// Update: Ya sin estados, todo llega por parametros)
@Composable
fun TuHogarScreen(
    // Carga de datos
    cargando: Boolean, // se estan trayendo las respuestas guardadas?
    errorCarga: String?, // error al cargar o null
    datos: TuHogar?, // las respuestas (null mientras carga)
    // Formulario
    errores: TuHogarErrors?, // un mensaje por campo (null = sin errores)
    onDatosChange: (TuHogar) -> Unit, // manda el TuHogar completo con el cambio
    // Guardar
    guardando: Boolean, // ¿se esta guardando?
    errorGuardar: String?, // error al guardar, o null
    // El resto de las funciones (para navegar)
    onReintentar: () -> Unit, // volver a cargar si falló
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    nombreMascota: String = "tu nueva mascota",
    navSeleccionado: Int = 3, // 3 = "Solicitudes"
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
                // 1 Loading -> circulito al centro
                cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // 2 Error -> mensaje y botón para reintentar
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

                // 3 Cargando -> el formulario
                datos != null -> {
                    FormularioTuHogar(
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
private fun FormularioTuHogar(
    datos: TuHogar,
    errores: TuHogarErrors?,
    onDatosChange: (TuHogar) -> Unit,
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
        Text(
            text = "Tu Hogar",
            color = InverseSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Cuéntanos dónde vivirá $nombreMascota y quiénes serán su nueva familia",
            color = InverseSurface,
            fontSize = 16.sp,
        )
        // Si / No
        Column {
            LabeledOptionPicker(
                label = "¿Has tenido mascotas antes?",
                options = opcionesSiNo,
                selectedIndex = datos.hadPet.aIndice(),
                onOptionSelected = { onDatosChange(datos.copy(hadPet = it == 0)) },
            )
            errores?.hadPet?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledOptionPicker(
                label = "¿Tienes otros animales en casa?",
                options = opcionesSiNo,
                selectedIndex = datos.otherPets.aIndice(),
                onOptionSelected = { onDatosChange(datos.copy(otherPets = it == 0)) },
            )
        }

        Column {
            LabeledWritableCard(
                label = "¿Por qué deseas adoptar?",
                value = datos.whyAdopt,
                onValueChange = { onDatosChange(datos.copy(whyAdopt = it)) },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            errores?.whyAdopt?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledWritableCard(
                label = "¿Donde vivirá y dormirá el perrito?",
                value = datos.whereLive,
                onValueChange = { onDatosChange(datos.copy(whereLive = it)) },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            errores?.whereLive?.let { ErrorMessage(text = it) }
        }

        // Contador de personas: texto a la izquierda, botón − / + a la derecha
        Column {
            Text(
                text = "¿Cuántas personas viven contigo?",
                color = InverseSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Personas que viven contigo",
                    color = InverseSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                SumRestButton(
                    value = datos.peopleInHouse,
                    onDecrease = {
                        if (datos.peopleInHouse > 1) {
                            onDatosChange(
                                datos.copy(
                                    peopleInHouse =
                                        datos.peopleInHouse - 1,
                                ),
                            )
                        }
                    }, // nunca menos de 1
                    onIncrease = { onDatosChange(datos.copy(peopleInHouse = datos.peopleInHouse + 1)) },
                )
            }
            errores?.peopleInHouse?.let { ErrorMessage(text = it) }
        }

        Column {
            LabeledOptionPicker(
                label = "¿Todos están de acuerdo?",
                options = opcionesSiNo,
                selectedIndex = datos.allAgree.aIndice(),
                onOptionSelected = { onDatosChange(datos.copy(allAgree = it == 0)) },
            )
        }

        // Vivienda
        Column {
            LabeledOptionPicker(
                label = "Casa o Departamento",
                options = opcionesVivienda,
                selectedIndex = datos.houseType.ordinal,
                onOptionSelected = { onDatosChange(datos.copy(houseType = HouseType.entries[it])) },
            )
        }

        Column {
            LabeledWritableCard(
                label = "¿Tienes jardin o patio?",
                value = datos.hasGarden,
                onValueChange = { onDatosChange(datos.copy(hasGarden = it)) },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            errores?.hasGarden?.let { ErrorMessage(text = it) }
        }

        // Opcional si renta

        LabeledOptionPicker(
            label = "Si rentas ¿Tu casero acepta mascotas?",
            options = opcionesSiNo,
            selectedIndex = datos.petAcept.aIndice(),
            onOptionSelected = { onDatosChange(datos.copy(petAcept = it == 0)) },
        )

        Column {
            LabeledWritableCard(
                label = "Cuéntanos sobre tus mascotas",
                value = datos.aboutPets,
                onValueChange = { onDatosChange(datos.copy(aboutPets = it)) },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            errores?.aboutPets?.let { ErrorMessage(text = it) }
        }

        if (errorGuardar != null) {
            Text(
                text = errorGuardar,
                color = MaterialTheme.colorScheme.error,
                fontSize = 16.sp,
            )
        }

        AppButton(
            text = "Continuar a Compromiso",
            onClick = onContinuarClick,
            isLoading = guardando,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
        )
    }
}

@Preview(showBackground = true, name = "Paso 3 - Tu Hogar")
@Composable
fun TuHogarScreenPreview() {
    var datos by remember { mutableStateOf(TuHogar()) }
    AndroidTheme {
        TuHogarScreen(
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

@Preview(showBackground = true, name = "Paso 3 - Con errores")
@Composable
fun TuHogarScreenErrorPreview() {
    AndroidTheme {
        TuHogarScreen(
            cargando = false,
            errorCarga = null,
            datos = TuHogar(),
            errores =
                TuHogarErrors(
                    hadPet = "Selecciona una opción",
                    whyAdopt = "Cuéntanos por qué quieres adoptar",
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

@Preview(showBackground = true, name = "Paso 3 - Cargando")
@Composable
fun TuHogarCargandoPreview() {
    AndroidTheme {
        TuHogarScreen(
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
