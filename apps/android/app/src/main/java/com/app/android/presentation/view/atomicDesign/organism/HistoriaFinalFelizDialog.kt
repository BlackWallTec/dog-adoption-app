package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoCuerpo
import com.app.android.presentation.view.atomicDesign.molecule.FotoFinalFeliz
import com.app.android.presentation.view.ui.theme.AndroidTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Modal de solo lectura. No ofrece adoptar ni muestra datos de la familia.
@Composable
fun HistoriaFinalFelizDialog(
    final: FinalFeliz,
    onCerrar: () -> Unit,
) {
    val fecha = fechaAdopcionLegible(final.fechaAdopcion)
    Dialog(onDismissRequest = onCerrar) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                FotoFinalFeliz(
                    url = final.fotoHogarUrl,
                    contentDescription = "Foto de ${final.nombre} en su nuevo hogar",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                )
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = final.nombre,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (fecha != null) {
                        TextoCuerpo(
                            texto = "Adoptado el $fecha",
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    TextoCuerpo(
                        texto = final.historia,
                        modifier = Modifier.padding(top = 12.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    TextButton(
                        onClick = onCerrar,
                        modifier =
                            Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp),
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

fun fechaAdopcionLegible(fecha: String?): String? {
    val texto = fecha?.trim().orEmpty()
    if (texto.isEmpty()) return null
    val fechaLocal = runCatching { LocalDate.parse(texto.take(10)) }.getOrNull() ?: return texto
    val formato = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-MX"))
    return fechaLocal.format(formato)
}

@Preview(showBackground = true, name = "Historia de un final feliz")
@Composable
private fun HistoriaFinalFelizDialogPreview() {
    AndroidTheme {
        Surface {
            HistoriaFinalFelizDialog(
                final =
                    FinalFeliz(
                        idPerro = "1",
                        nombre = "Luna",
                        fotoHogarUrl = "",
                        historia = "Luna llegó tímida y hoy duerme en el sillón de su nueva familia.",
                        fechaAdopcion = "2026-03-15",
                        estado = "Adoptado",
                        publicado = true,
                    ),
                onCerrar = {},
            )
        }
    }
}