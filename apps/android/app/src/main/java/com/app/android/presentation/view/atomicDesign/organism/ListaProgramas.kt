package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaVoluntariado
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.viewmodel.viewModelVoluntariado.AccionPrograma
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi

// Solo pinta las tarjetas: textos, cupo y botón ya vienen listos del ViewModel
@Composable
fun ListaProgramas(
    programas: List<ProgramaUi>,
    onInscribirse: (String) -> Unit, // evento: id del programa
    onCancelar: (String) -> Unit, // evento HU37: id del programa
    modifier: Modifier = Modifier,
    onVerDetalle: ((String) -> Unit)? = null, // evento HU36: id del programa (null = tarjeta no tocable)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = programas,
            key = { it.id },
        ) { programa ->
            val cancela = programa.accion == AccionPrograma.CANCELAR
            TarjetaVoluntariado(
                tipo = programa.tipo,
                titulo = programa.titulo,
                fecha = programa.fecha,
                horario = programa.horario,
                ubicacion = programa.ubicacion,
                textoCupo = programa.textoCupo,
                progresoCupo = programa.progresoCupo,
                // HU36: al tocar la tarjeta abre el detalle
                alVerDetalle = onVerDetalle?.let { abrir -> { abrir(programa.id) } },
                // HU37: si ya está inscrito, el botón cancela; si no, inscribe
                alAccionBoton = { if (cancela) onCancelar(programa.id) else onInscribirse(programa.id) },
                textoBoton = programa.textoBoton,
                botonHabilitado = programa.botonHabilitado,
                varianteBoton = if (cancela) ButtonVariant.Danger else ButtonVariant.Primary,
            )
        }
    }
}

private fun programaUiDePrueba(
    id: String,
    titulo: String,
    textoCupo: String,
    progresoCupo: Float,
    textoBoton: String,
    botonHabilitado: Boolean,
    accion: AccionPrograma = AccionPrograma.INSCRIBIRSE,
) = ProgramaUi(
    id = id,
    tipo = "Cuidado de Perros",
    titulo = titulo,
    fecha = "Vie. 11 dic. 2026",
    horario = "12:17 – 15:17 h",
    ubicacion = "Refugio Qariño · Querétaro",
    textoCupo = textoCupo,
    progresoCupo = progresoCupo,
    textoBoton = textoBoton,
    botonHabilitado = botonHabilitado,
    accion = accion,
)

@Preview(showBackground = true, widthDp = 360, name = "Lista de programas")
@Composable
private fun ListaProgramasPreview() {
    AndroidTheme {
        ListaProgramas(
            programas =
                listOf(
                    programaUiDePrueba("1", "Bazar de Donativos", "8/15 lugares ocupados · 7 disponibles", 8f / 15f, "Inscribirme", true),
                    programaUiDePrueba("2", "Jornada de limpieza", "10/10 lugares ocupados · 0 disponibles", 1f, "Sin lugares", false),
                    programaUiDePrueba("3", "Paseo de perritos", "Sin límite de lugares", 0f, "Inscribirme", true),
                    programaUiDePrueba("4", "Baño de perritos", "5/10 lugares ocupados · 5 disponibles", 0.5f, "Cancelar", true, AccionPrograma.CANCELAR),
                ),
            onInscribirse = {},
            onCancelar = {},
            modifier = Modifier.padding(16.dp),
            onVerDetalle = {},
        )
    }
}