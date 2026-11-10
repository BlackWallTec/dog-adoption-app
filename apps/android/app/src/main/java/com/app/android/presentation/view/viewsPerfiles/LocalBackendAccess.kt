package com.app.android.presentation.view.viewsPerfiles

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.app.android.BuildConfig
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import java.net.URI

/** Android 17 exige permiso para el servidor local usado durante desarrollo. */
@Composable
fun LocalBackendAccess(content: @Composable () -> Unit) {
    val host = URI(BuildConfig.API_BASE_URL).host.orEmpty()
    val local = host == "localhost" || host.startsWith("10.") || host.startsWith("127.") || host.startsWith("192.168.") || Regex("172\\.(1[6-9]|2[0-9]|3[01])\\..*").matches(host) || host.endsWith(".local")
    if (!BuildConfig.DEBUG || Build.VERSION.SDK_INT < 37 || !local) { content(); return }
    val context = LocalContext.current
    val permission = "android.permission.ACCESS_LOCAL_NETWORK"
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    if (granted) content()
    else Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Conectar con el refugio", style = MaterialTheme.typography.headlineSmall)
            Text("Para esta prueba necesitamos conectarnos al servidor del refugio en tu red local.", Modifier.padding(vertical = 16.dp))
            AppButton("Permitir conexión", { launcher.launch(permission) }, Modifier.fillMaxWidth())
        }
    }
}
