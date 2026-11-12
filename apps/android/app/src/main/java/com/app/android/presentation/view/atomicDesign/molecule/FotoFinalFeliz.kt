package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.app.android.presentation.view.ui.theme.AndroidTheme

// Foto de un final feliz. SI la url falla, deja un respaldo y no tumba el carrusel :D
@Composable
fun FotoFinalFeliz(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (url.isBlank()) {
            IconoFotoNoDisponible()
        } else {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.dp,
                    )
                },
                error = { IconoFotoNoDisponible() },
            )
        }
    }
}

@Composable
private fun IconoFotoNoDisponible() {
    Icon(
        imageVector = Icons.Filled.BrokenImage,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(40.dp),
    )
}

@Preview(showBackground = true, name = "Foto sin URL")
@Composable
private fun FotoFinalFelizVaciaPreview() {
    AndroidTheme {
        FotoFinalFeliz(
            url = "",
            contentDescription = "Final feliz sin foto",
            modifier = Modifier.size(160.dp),
        )
    }
}