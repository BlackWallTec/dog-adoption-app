package com.app.android.presentation.view.atomicDesign.page

//Necessary libraries for the design part
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.view.ui.theme.AndroidTheme

//imports of atoms and molecules
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.atomic.FileUploadCardEmpty
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.atomic.InfoCard
import com.app.android.presentation.view.atomicDesign.atomic.HeatherSection

//Main function that calls the atoms and molecules
@Composable
//Parameters that we are going to recieve
fun UploadDocuments(
    onBackClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onContinuarClick: () -> Unit = {},
    onNavSelect: (Int) -> Unit = {}
) {
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
        //We locate the bottom bar
        bottomBar = {
            NavBar(
                selected = 3,
                onSelect = onNavSelect
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            //More functions needed
            HeatherSection(
                titulo = "Documentos",
                descripcion = "Adjunta tus documentos. La información se usará únicamente para tu proceso de adopción."
            )
            FileUploadCardEmpty(
                title = "Identificación Oficial",
                onClick = {}
            )
            FileUploadCardEmpty(
                title = "Comprobante de domicilio",
                onClick = {}
            )
            AppButton (
                text = "Continuar",
                onClick = onContinuarClick,
                variant = ButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth()
            )
            InfoCard(
                text = "Tus documentos están protegidos y solo los revisará el equipo de adopciones"
            )
        }
    }
}

//To load the preview
@Preview(showBackground = true, name = "Subir documentos")
@Composable
fun UploadDocumentsPreview() {
    AndroidTheme { UploadDocuments()
    }
}