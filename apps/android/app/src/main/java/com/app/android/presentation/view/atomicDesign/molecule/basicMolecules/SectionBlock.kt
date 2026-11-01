package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoCuerpo
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoTitulo

/*
 * MOLÉCULA DE ESTRUCTURA: SectionBlock
 * Aplica el Caso 2 (Parámetros + Slot) de Slot API[cite: 1, 2].
 * Recibe el título de la sección y abre un slot lambda para renderizar su contenido.
 */
@Composable
fun SectionBlock(
    titulo: String,
    modifier: Modifier = Modifier,
    espaciadoInterno: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        TextoTitulo(texto = titulo)
        Spacer(modifier = Modifier.height(espaciadoInterno))
        content()
    }
}

@Preview(showBackground = true, name = "Molécula: SectionBlock")
@Composable
fun SectionBlockPreview() {
    SectionBlock(titulo = "Sobre mí") {
        TextoCuerpo(texto = "¡Hola, soy Bruno! Me encantan los paseos.")
    }
}
