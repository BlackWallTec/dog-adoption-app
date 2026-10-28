package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.molecule.AppOptionPicker
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.molecule.SumRestButton
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

private val opcionesSiNo = listOf("Si","No")

// PAGE: paso 3 de la solicitud de adopcion
@Composable
fun TuHogarScreen(
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    nombreMascota: String = "Bruno",
    navSeleccionado: Int = 3,
    onNavSelect: (Int) -> Unit = {},
) {
    // Otro estado temporal (que despues va a estar en viewmodel)
    // Para los pickers: 0 = primera opcion, 1 = segunda.... y -1 = ninguna
    var tuvoMascotas by remember { mutableIntStateOf(0) }
    var otrosAnimales by remember { mutableIntStateOf(0) }
    var porQueAdoptar by remember { mutableStateOf("") }
    var dondeVivira by remember { mutableStateOf("") }
    var personas by remember { mutableIntStateOf(3) }
    var todosDeAcuerdo by remember { mutableIntStateOf(0) }
    var tipoVivienda by remember { mutableIntStateOf(0) }
    var jardin by remember { mutableStateOf("") }
    var caseroAcepta by remember { mutableIntStateOf(-1) }
    var sobreMascotas by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = { AppTopBar(titulo = "Solicitud de adopción", onBackClick = onBackClick) },
        bottomBar = {NavBar(selected = navSeleccionado, onSelect = onNavSelect)},
    ) { paddingInterno ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
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

            PreguntaOpciones(
                pregunta = "¿Has tenido mascotas antes?",
                opciones = opcionesSiNo,
                seleccionado = tuvoMascotas,
                onSeleccion = { tuvoMascotas = it },
            )
            PreguntaOpciones(
                pregunta = "¿Tienes otros animales en casa?",
                opciones = opcionesSiNo,
                seleccionado = otrosAnimales,
                onSeleccion = { otrosAnimales = it },
            )

            LabeledWritableCard(
                label = "¿Por qué deseas adoptar?",
                value = porQueAdoptar,
                onValueChange = { porQueAdoptar = it },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )

            LabeledWritableCard(
                label = "¿Donde vivirá y dormirá el perrito?",
                value = dondeVivira,
                onValueChange = { dondeVivira = it },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )

            // Contador de personas: texto a la izquierda, botón − / + a la derecha
            Column {
                Text(
                    text = "¿Cuántas personas viven contigo?",
                    color = InverseSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Personas que viven contigo",
                        color = InverseSurface,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                    )
                    SumRestButton(
                        value = personas,
                        onDecrease = { if (personas > 1) personas-- }, // nunca menos de 1
                        onIncrease = { personas++ },
                    )
                }
            }

            PreguntaOpciones(
                pregunta = "¿Todos están de acuerdo?",
                opciones = opcionesSiNo,
                seleccionado = todosDeAcuerdo,
                onSeleccion = { todosDeAcuerdo = it },
            )
            PreguntaOpciones(
                pregunta = "Casa o Departamento",
                opciones = listOf("Casa", "Departamento", "Otro"),
                seleccionado = tipoVivienda,
                onSeleccion = { tipoVivienda = it },
            )

            LabeledWritableCard(
                label = "¿Tienes jardin o patio?",
                value = jardin,
                onValueChange = { jardin = it },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )

            PreguntaOpciones(
                pregunta = "Si rentas ¿Tu casero acepta mascotas?",
                opciones = opcionesSiNo,
                seleccionado = caseroAcepta,
                onSeleccion = { caseroAcepta = it },
            )

            LabeledWritableCard(
                label = "Cuéntanos sobre tus mascotas",
                value = sobreMascotas,
                onValueChange = { sobreMascotas = it },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )

            AppButton(
                text = "Continuar a Compromiso",
                onClick = onContinuarClick,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

// Pregunta + botones de opciones
@Composable
private fun PreguntaOpciones(
    pregunta: String,
    opciones: List<String>,
    seleccionado: Int,
    onSeleccion: (Int) -> Unit,
) {
    Column {
        Text(
            text = pregunta,
            color = InverseSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        AppOptionPicker(
            options = opciones,
            selectedIndex = seleccionado,
            onOptionSelected = onSeleccion,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview(showBackground = true, name = "Paso 3 - Tu hogar")
@Composable
fun TuHogarScreenPreview() {
    AndroidTheme {
        TuHogarScreen(onBackClick = {}, onContinuarClick = {})
    }
}