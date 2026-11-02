package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.molecule.InfoMessageCard
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.ListaProgramas
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState

// PAGE: catálogo de programas de voluntariado (HU39A)
@Composable
fun ProgramasPage(
    uiState: ProgramasUiState,
    onReintentar: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 1, // 1 = "Ayudar", la pestaña de Voluntariado
    onNavSelect: (Int) -> Unit = {},
) {
    val colores = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        containerColor = colores.surface,
        topBar = { AppTopBar(titulo = "Voluntariado", onBackClick = onBackClick) },
        bottomBar = { NavBar(selected = navSeleccionado, onSelect = onNavSelect) },
    ) { paddingInterno ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = "Programas",
                color = colores.inverseSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = "Elige un programa de voluntariado para ayudar a los perritos de Qariño.",
                color = colores.inverseSurface,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (uiState) {
                    is ProgramasUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    is ProgramasUiState.Empty -> {
                        InfoMessageCard(
                            mensaje = "No hay programas disponibles por ahora.",
                            icono = Icons.Outlined.Info,
                        )
                    }

                    is ProgramasUiState.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            ErrorMessage(text = uiState.mensaje)
                            Spacer(modifier = Modifier.height(12.dp))
                            AppButton(text = "Reintentar", onClick = onReintentar)
                        }
                    }

                    is ProgramasUiState.Success -> {
                        ListaProgramas(programas = uiState.programas)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Programas - lista")
@Composable
private fun ProgramasPageSuccessPreview() {
    AndroidTheme {
        ProgramasPage(
            uiState =
                ProgramasUiState.Success(
                    listOf(
                        Programa("1", "Cuidado de Perros", "Bazar de Donativos", "Disponible", "2026-12-11T12:17:08.611", "2026-12-16T12:17:08.611"),
                        Programa("2", "Limpieza", "Jornada de limpieza", "Disponible", "2026-12-20T09:00:00", "2026-12-20T13:00:00"),
                    ),
                ),
            onReintentar = {},
            onBackClick = {},
        )
    }
}

@Preview(showBackground = true, name = "Programas - vacío")
@Composable
private fun ProgramasPageEmptyPreview() {
    AndroidTheme {
        ProgramasPage(uiState = ProgramasUiState.Empty, onReintentar = {}, onBackClick = {})
    }
}

@Preview(showBackground = true, name = "Programas - error")
@Composable
private fun ProgramasPageErrorPreview() {
    AndroidTheme {
        ProgramasPage(
            uiState = ProgramasUiState.Error("No se pudieron cargar los programas"),
            onReintentar = {},
            onBackClick = {},
        )
    }
}