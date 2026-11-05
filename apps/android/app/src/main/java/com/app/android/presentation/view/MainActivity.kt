package com.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.app.android.presentation.navigation.AppNavigation
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.app.android.presentation.view.atomicDesign.page.UploadDocumentsRoute

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inyectamos el ViewModel para pruebas
    private val viewModel: DogDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTheme {
                UploadDocumentsRoute(
                    solicitudId = "ff028456-acf9-4781-a560-98dd168a5516",
                    onNavSelect = { index -> 3 }
                )
            }
        }
    }
}
