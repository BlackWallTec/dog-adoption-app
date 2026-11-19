package com.app.android.presentation.view.viewsPerfiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.viewmodel.viewModelPerfiles.SesionUiState

@Composable
fun InicioSesionScreen(state: SesionUiState, onRetry: () -> Unit, onGuest: () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.comprobando) { CircularProgressIndicator(); Text("Comprobando tu sesión…", Modifier.padding(top = 16.dp)) }
            else {
                state.error?.let { PerfilesError(it) }
                AppButton("Reintentar", onRetry, Modifier.fillMaxWidth().padding(top = 16.dp))
                TextButton(onClick = onGuest) { Text("Explorar sin cuenta") }
            }
        }
    }
}
