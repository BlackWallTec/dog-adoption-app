package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.app.android.presentation.view.atomicDesign.molecule.LabeledOptionPicker
import com.app.android.presentation.view.atomicDesign.molecule.LabeledWritableCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// PAGE: paso 4 de la solicitud de adopcion
@Composable
fun CompromisoScreen(
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    nombreMascota: String = "Bruno",
    navSeleccionado: Int = 3,
    onNavSelect: (Int) -> Unit = {},
) {
    var cuidados by remember { mutableStateOf("") }
    var esterilizacion by remember { mutableStateOf("") }
    var seguimiento by remember { mutableStateOf(-1) } // -1 = nada seleccioonado

    Scaffold(
        modifier = modifier,
        containerColor = Surface,
        topBar = { AppTopBar(titulo = "Solicitud de adopción", onBackClick = onBackClick) },
        bottomBar = { NavBar(selected = navSeleccionado, onSelect = onNavSelect) },
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

            LabeledWritableCard(
                label = "Menciona los cuidados básicos de una mascota",
                value = cuidados,
                onValueChange = { cuidados = it },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )
            LabeledWritableCard(
                label = "¿Qué opinas de la esterilización?",
                value = esterilizacion,
                onValueChange = { esterilizacion = it },
                placeholder = "Escribe tu respuesta...",
                minLines = 3,
            )

            LabeledOptionPicker(
                label = "¿Nos permites dar seguimiento (visitas o fotos)?",
                options = listOf("Sí", "No"),
                selectedIndex = seguimiento,
                onOptionSelected = { seguimiento = it },
            )

            AppButton(
                text = "Continuar a Documentos",
                onClick = onContinuarClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "Paso 4 - Compromiso")
@Composable
fun CompromisoScreenPreview() {
    AndroidTheme {
        CompromisoScreen(onBackClick = {}, onContinuarClick = {})
    }
}