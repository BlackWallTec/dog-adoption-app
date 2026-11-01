package com.app.android.presentation.view.atomicDesign.page

//Necessary libraries for the design part
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.presentation.util.utilSolicitudes.readPickedFile
import com.app.android.presentation.view.ui.theme.AndroidTheme

//imports of atoms and molecules
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.atomic.FileUploadCardEmpty
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.FileUploadCard
import com.app.android.presentation.view.atomicDesign.atomic.InfoCard
import com.app.android.presentation.view.atomicDesign.atomic.HeatherSection
import com.app.android.presentation.view.atomicDesign.atomic.HelperText
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DocumentSlot
import com.app.android.presentation.viewmodel.viewModelSolicitudes.UploadDocumentsUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.UploadDocumentsViewModel

@Composable
fun UploadDocumentsRoute(
    //Paramters that we are going to recieve
    onBackClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onContinuarClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit,
    viewModel: UploadDocumentsViewModel = hiltViewModel()
) {
    //We observe the state of the card: each change it restructures the screen
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //We save which of the two cards was used, while the user chooses a file
    var pendingType by rememberSaveable {mutableStateOf<DocumentType?>(null)}
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val type = pendingType
        if (uri != null && type != null){
            //We send the link made by hilt to the viewModel
            viewModel.onFileSelected(type, context.readPickedFile(uri))
        }
        pendingType = null
    }
    UploadDocuments(
        uiState = uiState,
        onPickFile = { type ->
            pendingType = type
            filePicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png"))
        },
        onErrorShown = viewModel::onErrorShown,
        onBackClick = onBackClick,
        onNotificacionesClick = onNotificacionesClick,
        onContinuarClick = onContinuarClick,
        onNavSelect = onNavSelect


    )
}
//Main function that calls the atoms and molecules
@Composable
//Parameters that we are going to recieve
fun UploadDocuments(
    uiState: UploadDocumentsUiState,
    onPickFile: (DocumentType) -> Unit,
    onErrorShown: () -> Unit,
    onBackClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onContinuarClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit = {},
) {
    //We create a variable for errors
    val snackbarHostState = remember { SnackbarHostState() }
    //Show the error one time and alerts the viewModel to clean the mistake
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            //cleaning of the error
            snackbarHostState.showSnackbar(it)
            onErrorShown()
        }
    }
    //We Use scaffold to make it according to the screen
    Scaffold(
        topBar = {
            //We use the top bar and give different values
            AppTopBar(
                titulo = "Solicitud de adopción",
                onBackClick = onBackClick,
                onNotificacionesClick = onNotificacionesClick
            )
        },
        //We show the mistake in top of the app
        snackbarHost = { SnackbarHost(snackbarHostState) },
        //We locate the bottom bar
        bottomBar = {
            Column {
                AppButton(
                    text = "Continuar",
                    onClick = onContinuarClick,
                    enabled = uiState.canContinue,
                    variant = ButtonVariant.Primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )

                NavBar(
                    selected = 3,
                    onSelect = onNavSelect
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            //More functions needed
            HeatherSection(
                titulo = "Documentos",
                descripcion = "Adjunta tus documentos. La información se usará únicamente para tu proceso de adopción."
            )
            DocumentCard(
                title = "Identificación Oficial",
                slot = uiState.slots.getValue(DocumentType.INE),
                onPick = {onPickFile (DocumentType.INE)}
            )
            DocumentCard(
                title = "Comprobante de domicilio",
                slot = uiState.slots.getValue(DocumentType.DOMICILIO),
                onPick = {onPickFile (DocumentType.DOMICILIO)}
            )

            HelperText(
                text = "PDF, JPG o PNG · Máximo 5 MB por archivo. Asegúrate de que los datos sean legibles."
            )
            InfoCard(
                text = "Tus documentos están protegidos y solo los revisará el equipo de adopciones"
            )
        }
    }
}

//It chooses the card that is going to display based on the state
@Composable
private fun DocumentCard(
    title: String,
    slot: DocumentSlot,
    onPick: () -> Unit
) {
    if (slot.isLoaded) {
        FileUploadCard(
            title = title,
            fileName = slot.fileName.orEmpty(),
            fileInfo = slot.fileInfo,
            onClick = onPick,
            onChangeClick = onPick,
            enabled = !slot.isUploading
        )
    } else {
        FileUploadCardEmpty(
            title = title,
            onClick = onPick,
            enabled = !slot.isUploading
        )
    }
}

//To load the preview
@Preview(
    showBackground = true,
    name = "Subir documentos",
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun UploadDocumentsPreview() {
    AndroidTheme { UploadDocuments(
        uiState = UploadDocumentsUiState (
            slots = mapOf (
                DocumentType.INE to DocumentSlot ("INE_Sofia_Garcia.pdf", "1.2 MB · Carga completa"),
                DocumentType.DOMICILIO to DocumentSlot()
            )
        ),
        onPickFile = {},
        onErrorShown = {}
    )
    }
}