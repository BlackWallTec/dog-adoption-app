package com.app.android.presentation.view.viewsCatalogo

import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.entities.entitiesCatalogo.ShareEvent
import com.app.android.domain.entities.entitiesCatalogo.ShareUIState
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.model.QMediaItem
import com.app.android.presentation.designsystem.pages.QDetallePerroPage
import com.app.android.presentation.viewmodel.viewModelCatalogo.CompartirViewModel
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogDetailUiState
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogDetailViewModel

private const val SHELTER_NAME = "Refugio Principal"

// Conecta DogDetailViewModel y CompartirViewModel con la página de detalle
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogDetailScreenView(
    viewModel: DogDetailViewModel = hiltViewModel(),
    dogId: String = "0edfe707-cc43-4bcf-bd43-a7f61e312e17",
    onBackClick: () -> Unit,
    onAdoptClick: (String) -> Unit,
    onNavSelect: (Int) -> Unit = {},
    shareViewModel: CompartirViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val shareState by shareViewModel.shareState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(dogId) {
        viewModel.onLoadDogByID(dogId)
    }

    LaunchedEffect(shareViewModel) {
        shareViewModel.eventos.collect { event ->
            if (event == ShareEvent.CopiadoOk) {
                Toast.makeText(context, "Enlace copiado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val success = uiState as? DogDetailUiState.Success
    val content = success?.datos?.toPageContent() ?: DogPageContent()

    QDetallePerroPage(
        isLoading = uiState is DogDetailUiState.Loading,
        errorMessage = (uiState as? DogDetailUiState.Error)?.mensaje,
        name = content.name,
        statusText = content.statusText,
        status = content.status,
        location = content.location,
        media = content.media,
        infoTiles = content.infoTiles,
        onBack = onBackClick,
        onRetry = { viewModel.onLoadDogByID(dogId) },
        onShare = { success?.let { shareViewModel.onCompartirClick(it.datos) } },
        onAdoptClick = { onAdoptClick(dogId) },
        onTabSelected = { onNavSelect(it.ordinal) },
        breedDetails = content.breedDetails,
        healthItems = content.healthItems,
        characteristics = content.characteristics,
        aboutMe = content.aboutMe,
        specialCare = success?.specialCare,
        isEmpty = uiState is DogDetailUiState.Empty,
    )

    val shareMenu = shareState as? ShareUIState.ShareMenu
    if (shareMenu != null) {
        ModalBottomSheet(onDismissRequest = shareViewModel::onCerrarMenuCompartir) {
            ShareMenuContent(
                onCompartir = {
                    compartirTexto(context, shareMenu.content.texto())
                    shareViewModel.onFichaCompartida()
                },
                onCopiar = {
                    copiarTexto(context, shareMenu.content.enlace)
                    shareViewModel.onEnlaceCopiado()
                },
            )
        }
    }
}

private data class DogPageContent(
    val name: String = "",
    val statusText: String = "",
    val status: QStatus = QStatus.Neutral,
    val location: String = "",
    val media: List<QMediaItem> = emptyList(),
    val infoTiles: List<Pair<String, String?>> = emptyList(),
    val breedDetails: List<String> = emptyList(),
    val healthItems: List<String> = emptyList(),
    val characteristics: List<String> = emptyList(),
    val aboutMe: String = "",
)

private fun Dog.toPageContent(): DogPageContent =
    DogPageContent(
        name = nombre,
        statusText = estado.trim().ifBlank { "Sin información" },
        status =
            when (estado.trim().lowercase()) {
                "disponible" -> QStatus.Positive
                "en proceso" -> QStatus.Pending
                else -> QStatus.Neutral
            },
        location = SHELTER_NAME,
        media =
            multimedia
                .filter { it.url.isNotBlank() }
                .map { item ->
                    if (item.tipo.equals("video", ignoreCase = true)) {
                        QMediaItem.Video(videoUrl = item.url)
                    } else {
                        QMediaItem.Image(url = item.url)
                    }
                },
        infoTiles =
            listOf(
                "Edad" to formatAge(edad),
                "Sexo" to sexo.ifBlank { null },
                "Tamaño" to tamanoNombre.ifBlank { null },
                "Raza" to razaNombre.ifBlank { null },
            ),
        breedDetails =
            listOfNotNull(
                razaDescripcion.takeIf { it.isNotBlank() }?.let { "Raza ($razaNombre): $it" },
                tamanoDescripcion.takeIf { it.isNotBlank() }?.let { "Tamaño ($tamanoNombre): $it" },
            ),
        healthItems =
            listOfNotNull(
                "Vacunado".takeIf { vacunado },
                "Esterilizado".takeIf { esterilizado },
            ),
        characteristics = listOfNotNull(perroCaracterNombre.takeIf { it.isNotBlank() }),
        aboutMe = descripcion,
    )

private fun formatAge(years: Int): String =
    when (years) {
        0 -> "Menos de 1 año"
        1 -> "1 año"
        else -> "$years años"
    }