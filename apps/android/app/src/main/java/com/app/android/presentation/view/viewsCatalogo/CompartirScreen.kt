package com.app.android.presentation.view.viewsCatalogo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api              ///
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.entities.entitiesCatalogo.ShareEvent
import com.app.android.domain.entities.entitiesCatalogo.ShareUIState
import com.app.android.presentation.viewmodel.viewModelCatalogo.CompartirViewModel
import com.app.android.domain.entities.entitiesCatalogo.Dog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompartirScreen (
    perro: Dog,
    viewModel: CompartirViewModel =hiltViewModel()
){
    val shareState by viewModel.shareState.collectAsState()
    val context=LocalContext.current
    val snackbarHostState=remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventos.collect { evento ->
            if (evento == ShareEvent.CopiadoOk){
                snackbarHostState.showSnackbar("Enlace copiado")
            }
        }
    }
    Column(modifier=Modifier.padding(8.dp)){
        Button(onClick={viewModel.onCompartirClick(perro)}, colors= ButtonDefaults.buttonColors(containerColor=Color(0xFFFFC832), contentColor=Color(0xFF3E2723)
        )
        ){
            Text("Compartir")
        }
        SnackbarHost(hostState=snackbarHostState)
    }

    val state = shareState
    if (state is ShareUIState.ShareMenu){
        ModalBottomSheet(
            onDismissRequest= { viewModel.onCerrarMenuCompartir()}
        ){
            ShareMenuContent(
                onCompartir={
                    compartirTexto(context, state.content.texto())
                    viewModel.onFichaCompartida()
                },
                onCopiar={
                    copiarTexto(context, state.content.enlace)
                    viewModel.onEnlaceCopiado()
                }
            )
        }
    }
}


@Composable
private fun ShareOption(
    icono:ImageVector,
    texto: String,
    onClick: () -> Unit
){
    val colorTexto=Color(0xFF3E2723)
    Surface(
        onClick=onClick,
        shape=RoundedCornerShape(16.dp),
        color=Color(0xFFF3EAD8),
        modifier=Modifier.fillMaxWidth()
    ){
        Row(verticalAlignment=Alignment.CenterVertically,
            modifier=Modifier.padding(horizontal = 12.dp, vertical=12.dp)
        ){
            Box(
                contentAlignment=Alignment.Center,modifier=Modifier.size(40.dp).background(Color(0xFFEBDDC8), shape= CircleShape)
            ){
                Icon(
                    imageVector=icono,
                    contentDescription=null,        //
                    tint=colorTexto,
                    modifier=Modifier.size(20.dp)
                )
            }
            Spacer(modifier=Modifier.width(16.dp))
            Text(text=texto, style=MaterialTheme.typography.bodyLarge, color=colorTexto, modifier=Modifier.weight(1f)
            )
            Icon(
                imageVector=Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription=null,
                tint=colorTexto
            )
        }
    }
}
@Composable
internal fun ShareMenuContent(
    onCompartir:() -> Unit,
    onCopiar: () -> Unit
){
    Column(modifier =Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical=8.dp)){
        Text(text="Compartir ficha", style= MaterialTheme.typography.titleMedium, color= Color(0xFF3E2723))
        Spacer(modifier=Modifier.height(16.dp))
        ShareOption(icono= Icons.Default.Share, texto="Compartir", onClick=onCompartir)
        Spacer(modifier= Modifier.height(8.dp))
        ShareOption(icono= Icons.Default.Link, texto="Copiar enlace", onClick=onCopiar)
        Spacer(modifier= Modifier.height(24.dp))
    }
}

internal fun  compartirTexto(context: Context, texto: String){
    val intent = Intent(Intent.ACTION_SEND).apply{
        type="text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir ficha"))
}

internal fun copiarTexto(context: Context, texto: String){
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Ficha", texto))
}