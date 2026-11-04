package com.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.viewsSolicitudes.TuHogarRoute
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inyectamos el ViewModel para pruebas
    private val viewModel: TuHogarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    // Lanzamos la navegación de Tu Hogar
                    TuHogarRoute(
                        idSolicitud = "25a0b8c7-e1d1-4603-9afc-eda4b1dc65d4",
                        onBack = { finish() },
                        onIrACompromiso = { finish() },
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}
