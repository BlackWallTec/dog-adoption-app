package com.app.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.app.android.presentation.navigation.AppNavigation
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.viewsSolicitudes.CompromisoRoute
import com.app.android.presentation.view.viewsSolicitudes.TuHogarRoute
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoViewModel
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
