package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.WorkOutline
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
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.IndicadorPasos
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// PAGE: Paso 2 de la solicitud de adopcion (stateless: todo llega por parametros)
@Composable
fun DatosGeneralesScreen(
    // Carga de datos
    cargando: Boolean, // se estan trayendo los datos?
    errorCarga: String?, // error al cargar o null
    datos: DatosGenerales?, // los datos (null mientras carga)
    edad: Int?, // calculada en el ViewModel
    // Formulario
    errorOcupacion: String?, // error de validacion del campo ocupacion
    onOcupacionChange: (String) -> Unit,
    // Guardar
    guardando: Boolean, // ¿se esta guardando?
    errorGuardar: String?, // error al guardar, o null
    // Lo demas
    onReintentar: () -> Unit, // volver a cargar si falló
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 3, // 3 = "Solicitudes"
    onNavSelect: (Int) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = {
            AppTopBar(
                titulo = "Solicitud de adopción",
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
        // Box ocupa tod el espacio entre las barras
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
        ) {
            when {
                // 1 Loading → circulito al centro
                cargando -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                // 2 error cuando caraga→ mensaje + reintentar
                errorCarga != null -> {
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(text = errorCarga, color = InverseSurface, fontSize = 16.sp)
                        AppButton(text = "Reintentar", onClick = onReintentar)
                    }
                }

                // 3 Loadedd mdf → el formulario
                datos != null -> {
                    FormularioDatosGenerales(
                        datos = datos,
                        edad = edad,
                        errorOcupacion = errorOcupacion,
                        onOcupacionChange = onOcupacionChange,
                        guardando = guardando,
                        errorGuardar = errorGuardar,
                        onContinuarClick = onContinuarClick,
                    )
                }
            }
        }
    }
}

// El formulario
@Composable
private fun FormularioDatosGenerales(
    datos: DatosGenerales,
    edad: Int?,
    errorOcupacion: String?,
    onOcupacionChange: (String) -> Unit,
    guardando: Boolean,
    errorGuardar: String?,
    onContinuarClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IndicadorPasos(
            pasoActual = 1,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Datos generales",
            color = InverseSurface,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Precargamos los datos de tu perfil. Revísalos y completa tu ocupación antes de continuar.",
            color = InverseSurface,
            fontSize = 16.sp,
        )

        // read only (vienen del perfil, pre cargados)
        LabeledWritableCard(
            label = "Nombre",
            value = datos.nombreCompleto,
            onValueChange = {}, // no hace nada: es de solo lectura
            placeholder = "",
            leadingIcon = Icons.Outlined.Person,
            singleLine = true,
            readOnly = true,
        )
        LabeledWritableCard(
            label = "Edad",
            value = if (edad != null) "$edad años" else "",
            onValueChange = {},
            placeholder = "",
            leadingIcon = Icons.Outlined.CalendarMonth,
            helperText = formatearNacimiento(datos.fechaNacimiento),
            singleLine = true,
            readOnly = true,
        )

        // este si es editable
        LabeledWritableCard(
            label = "Ocupación",
            value = datos.ocupacion,
            onValueChange = onOcupacionChange,
            placeholder = "Escribe tu ocupación",
            leadingIcon = Icons.Outlined.WorkOutline,
            // si hay error lo muestra
            helperText = errorOcupacion ?: "Este dato no está guardado en tu perfil.",
            singleLine = true,
        )

        // read only
        LabeledWritableCard(
            label = "Domicilio",
            value = datos.domicilio,
            onValueChange = {},
            placeholder = "",
            minLines = 2,
            readOnly = true,
        )
        LabeledWritableCard(
            label = "Delegación/Municipio",
            value = datos.municipio,
            onValueChange = {},
            placeholder = "",
            singleLine = true,
            readOnly = true,
        )
        LabeledWritableCard(
            label = "Teléfono",
            value = datos.telefono,
            onValueChange = {},
            placeholder = "",
            leadingIcon = Icons.Outlined.Phone,
            singleLine = true,
            readOnly = true,
        )
        LabeledWritableCard(
            label = "Correo",
            value = datos.correo,
            onValueChange = {},
            placeholder = "",
            leadingIcon = Icons.Outlined.Email,
            singleLine = true,
            readOnly = true,
        )

        if (errorGuardar != null) {
            Text(
                text = errorGuardar,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
            )
        }

        AppButton(
            text = "Continuar a Tu hogar",
            onClick = onContinuarClick,
            isLoading = guardando,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
        )
    }
}

// "1995-03-15" → "Nacimiento: 15/03/1995"
private fun formatearNacimiento(fecha: String?): String? {
    if (fecha.isNullOrBlank()) return null
    val partes = fecha.take(10).split("-") // ["1995", "03", "15"]
    if (partes.size != 3) return null
    return "Nacimiento: ${partes[2]}/${partes[1]}/${partes[0]}"
}

// estos solo son datos para probar
private val datosDePrueba =
    DatosGenerales(
        nombreCompleto = "Sofía García López",
        fechaNacimiento = "1995-03-15",
        ocupacion = "",
        domicilio = "Calle Roble 24, Col. Álamos, Querétaro, Qro. · C.P. 76160",
        municipio = "Querétaro",
        telefono = "442 123 4567",
        correo = "sofia.garcia@correo.com",
    )

@Preview(showBackground = true, name = "Paso 2 - Datos generales")
@Composable
fun DatosGeneralesScreenPreview() {
    var datos by remember { mutableStateOf(datosDePrueba) }
    AndroidTheme {
        DatosGeneralesScreen(
            cargando = false,
            errorCarga = null,
            datos = datos,
            edad = 31,
            errorOcupacion = null,
            onOcupacionChange = { datos = datos.copy(ocupacion = it) },
            guardando = false,
            errorGuardar = null,
            onReintentar = {},
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Paso 2 - Cargando")
@Composable
fun DatosGeneralesCargandoPreview() {
    AndroidTheme {
        DatosGeneralesScreen(
            cargando = true,
            errorCarga = null,
            datos = null,
            edad = null,
            errorOcupacion = null,
            onOcupacionChange = {},
            guardando = false,
            errorGuardar = null,
            onReintentar = {},
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}