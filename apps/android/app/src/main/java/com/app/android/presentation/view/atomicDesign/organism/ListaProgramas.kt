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
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaVoluntariado
import com.app.android.presentation.view.ui.theme.AndroidTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import com.app.android.presentation.view.viewsVoluntariado.fechaTexto
import com.app.android.presentation.view.viewsVoluntariado.horarioTexto
import com.app.android.presentation.view.viewsVoluntariado.ubicacionTexto

@Composable
fun ListaProgramas(
    programas: List<Programa>,
    onInscribirse: (Programa) -> Unit,
    onCancelar: (Programa) -> Unit,
    modifier: Modifier = Modifier,
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

            val boton = botonDe(programa)

            TarjetaVoluntariado(
                tipo = programa.tipo,
                titulo = programa.nombre,
                fecha = programa.fechaTexto(),
                horario = programa.horarioTexto(),
                ubicacion = programa.ubicacionTexto(),
                textoCupo = "",
                progresoCupo = 0f,
                // HU37: si ya está inscrito, el botón cancela; si no, inscribe
                alAccionBoton = { if (programa.usuarioInscrito) onCancelar(programa) else onInscribirse(programa) },
                textoBoton = boton.texto,
                botonHabilitado = boton.habilitado,
                varianteBoton = boton.variante,
            )
        }
    }
}

// Cómo se ve el botón según el estado del programa
private data class EstadoBoton(
    val texto: String,
    val habilitado: Boolean,
    val variante: ButtonVariant = ButtonVariant.Primary,
)

// El botón siempre se muestra; su texto y si se puede tocar dependen del estado del programa
private fun botonDe(programa: Programa): EstadoBoton =
    when {
        // Inscrito: se puede cancelar solo mientras el programa no haya iniciado (HU37)
        programa.usuarioInscrito && programa.fechaInicio.isAfter(Instant.now()) ->
            EstadoBoton("Cancelar", true, ButtonVariant.Danger)
        programa.usuarioInscrito -> EstadoBoton("Inscrito", false)
        programa.estado != EstadoPrograma.DISPONIBLE -> EstadoBoton("No disponible", false)
        else -> EstadoBoton("Inscribirme", true)
    }

private fun programaDePrueba(
    id: String,
    nombre: String,
    tipo: String,
    inicio: String,
    fin: String,
    estado: EstadoPrograma = EstadoPrograma.DISPONIBLE,
    inscrito: Boolean = false,
) = Programa(
    id = id,
    nombre = nombre,
    tipo = tipo,
    descripcion = null,
    fechaInicio = LocalDateTime.parse(inicio).toInstant(ZoneOffset.UTC),
    fechaFin = LocalDateTime.parse(fin).toInstant(ZoneOffset.UTC),
    ubicacion = null,
    cupoMaximo = null,
    cupoDisponible = null,
    estado = estado,
    usuarioInscrito = inscrito,
)

@Preview(showBackground = true, widthDp = 360, name = "Lista de programas")
@Composable
private fun ListaProgramasPreview() {
    AndroidTheme {
        ListaProgramas(
            programas =
                listOf(
                    programaDePrueba("1", "Bazar de Donativos", "Cuidado de Perros", "2099-12-11T12:17:08", "2099-12-16T12:17:08"),
                    programaDePrueba("2", "Jornada de limpieza", "Limpieza", "2099-12-20T09:00:00", "2099-12-20T13:00:00", inscrito = true),
                ),
            onInscribirse = {},
            onCancelar = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}