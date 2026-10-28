package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// PAGE: Paso 2 de la solicitud de adopción
@Composable
fun DatosGeneralesScreen(
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 3, // 3 = "Solicitudes"
    onNavSelect: (Int) -> Unit = {},
) {
    // ESTADO TEMPORAL: datos que  son inventados mientras no hay backend
    // Después esto va a estar en el ViewModel y vendra del perfil del usuario
    var nombre by remember { mutableStateOf("Sofía García López") }
    var edad by remember { mutableStateOf("31 años") }
    var ocupacion by remember { mutableStateOf("") } // usuario lo llena
    var domicilio by remember { mutableStateOf("Calle Roble 24, Col. Álamos, Querétaro, Qro. · C.P. 76160") }
    var municipio by remember { mutableStateOf("Querétaro") }
    var telefono by remember { mutableStateOf("442 123 4567") }
    var correo by remember { mutableStateOf("sofia.garcia@correo.com") }

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

            LabeledWritableCard(
                label = "Nombre",
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Escribe tu nombre",
                leadingIcon = Icons.Outlined.Person,
                singleLine = true,
            )
            LabeledWritableCard(
                label = "Edad",
                value = edad,
                onValueChange = { edad = it },
                placeholder = "Escribe tu edad",
                leadingIcon = Icons.Outlined.CalendarMonth,
                helperText = "Nacimiento: 15/03/1995 · Edad al 08/10/2026.",
                singleLine = true,
            )
            LabeledWritableCard(
                label = "Ocupación",
                value = ocupacion,
                onValueChange = { ocupacion = it },
                placeholder = "Escribe tu ocupación",
                leadingIcon = Icons.Outlined.WorkOutline,
                helperText = "Este dato no está guardado en tu perfil.",
                singleLine = true,
            )
            LabeledWritableCard(
                label = "Domicilio",
                value = domicilio,
                onValueChange = { domicilio = it },
                placeholder = "Escribe tu domicilio",
                minLines = 2, // es largo, empieza con 2 renglones
            )
            LabeledWritableCard(
                label = "Delegación/Municipio",
                value = municipio,
                onValueChange = { municipio = it },
                placeholder = "Escribe tu municipio",
                singleLine = true,
            )
            LabeledWritableCard(
                label = "Teléfono",
                value = telefono,
                onValueChange = { telefono = it },
                placeholder = "Escribe tu teléfono",
                leadingIcon = Icons.Outlined.Phone,
                singleLine = true,
            )
            LabeledWritableCard(
                label = "Correo",
                value = correo,
                onValueChange = { correo = it },
                placeholder = "Escribe tu correo",
                leadingIcon = Icons.Outlined.Email,
                singleLine = true,
            )

            AppButton(
                text = "Continuar a Tu hogar",
                onClick = onContinuarClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "Paso 2 - Datos generales")
@Composable
fun DatosGeneralesScreenPreview() {
    AndroidTheme {
        DatosGeneralesScreen(
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}