package com.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.viewmodel.viewModelCatalogo.DogDetailViewModel
import com.app.android.ui.theme.AndroidTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inyectamos el ViewModel para pruebas
    private val viewModel: DogDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Dispara la prueba una sola vez al arrancar la pantalla
            LaunchedEffect(Unit) {
                viewModel.onLoadDogByID("131180a6-05c0-4e76-a125-175623a5c8db") // Pon un ID real de tu BD
            }

            // Texto temporal mientras no hay UI
            androidx.compose.material3.Text("Probrando conexión en consola Logcat...")
        }
    }
}
