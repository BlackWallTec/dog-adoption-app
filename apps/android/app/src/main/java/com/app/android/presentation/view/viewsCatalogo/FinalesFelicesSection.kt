package com.app.android.presentation.view.viewsCatalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.organism.HistoriaFinalFelizDialog
import com.app.android.presentation.view.atomicDesign.organism.basicOrganisms.CarruselFinalesFelices
import com.app.android.presentation.viewmodel.viewModelCatalogo.FinalesFelicesUiState
import com.app.android.presentation.viewmodel.viewModelCatalogo.FinalesFelicesViewModel

// Conecta el ViewModel con la sección. El grid del catálogo no comparte este estado.
@Composable
fun FinalesFelicesSection(
    modifier: Modifier = Modifier,
    viewModel: FinalesFelicesViewModel = hiltViewModel(),
) {
    val estado by viewModel.uiState.collectAsState()
    var seleccionado by remember { mutableStateOf<FinalFeliz?>(null) }
    LaunchedEffect(estado, seleccionado?.idPerro) {
        val idAbierto = seleccionado?.idPerro ?: return@LaunchedEffect
        val siguePublicado =
            when (val actual = estado) {
                is FinalesFelicesUiState.Success -> actual.finales.any { it.idPerro == idAbierto }
                FinalesFelicesUiState.Empty -> false
                FinalesFelicesUiState.Loading,
                is FinalesFelicesUiState.Error,
                    -> true
            }
        if (!siguePublicado) seleccionado = null
    }
    when (val actual = estado) {
        FinalesFelicesUiState.Empty -> Unit
        FinalesFelicesUiState.Loading -> {
            BloqueFinalesFelices(modifier) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                    Text("Cargando finales felices...")
                }
            }
        }
        is FinalesFelicesUiState.Error -> {
            BloqueFinalesFelices(modifier) {
                ErrorMessage(text = actual.mensaje)
                Spacer(Modifier.height(8.dp))
                AppButton(
                    text = "Reintentar",
                    onClick = viewModel::cargar,
                    variant = ButtonVariant.Secondary,
                )
            }
        }
        is FinalesFelicesUiState.Success -> {
            BloqueFinalesFelices(modifier) {
                CarruselFinalesFelices(
                    finales = actual.finales,
                    modalAbierto = seleccionado != null,
                    onFotoClick = { seleccionado = it },
                )
            }
        }
    }
    seleccionado?.let { final ->
        HistoriaFinalFelizDialog(
            final = final,
            onCerrar = { seleccionado = null },
        )
    }
}
@Composable
private fun BloqueFinalesFelices(
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
    ) {
        Text(
            text = "Finales Felices",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        contenido()
    }
}
