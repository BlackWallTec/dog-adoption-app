package com.app.android.presentation.view.atomicDesign.organism

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.presentation.view.atomicDesign.atomic.ErrorMessage
import com.app.android.presentation.view.atomicDesign.atomic.FilterButton
import com.app.android.presentation.view.atomicDesign.molecule.FilterActionBar
import com.app.android.presentation.viewmodel.viewModelVoluntariado.FiltrosProgramasUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FiltrosProgramasPanel(
    modifier: Modifier = Modifier,
    estado: FiltrosProgramasUiState,
    onFiltrosChange: (FiltrosProgramas) -> Unit,
    onAplicarFiltros: () -> Unit,
    onLimpiarFiltros: () -> Unit,
) {
    val context = LocalContext.current
    val filtros = estado.seleccionados

    val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val formatoHorario = DateTimeFormatter.ofPattern("HH:mm")

    // Abre el calendario nativo de android y se comunica la fecha seleccionada
    fun elegirFecha(
        actual: LocalDate?,
        onSeleccionado: (LocalDate) -> Unit,
    ) {
        val inicial =
            actual
                ?: LocalDate.now(ZoneId.of("America/Mexico_City"))

        DatePickerDialog(
            context,
            { _, anio, mes, dia ->
                onSeleccionado(LocalDate.of(anio, mes + 1, dia))
            },
            inicial.year,
            inicial.monthValue - 1,
            inicial.dayOfMonth,
        ).show()
    }

    fun elegirHora(
        actual: LocalTime?,
        onSeleccionado: (LocalTime) -> Unit,
    ) {
        val inicial =
            actual
                ?: LocalTime.of(9, 0)

        TimePickerDialog(
            context,
            { _, hora, minuto ->
                onSeleccionado(LocalTime.of(hora, minuto))
            },
            inicial.hour,
            inicial.minute,
            true,
        ).show()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Filtrar Programa",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Selecciona las fechas y el horario de tu preferencia.",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = "Dia: ",
            style = MaterialTheme.typography.titleSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CampoFiltro(
                titulo = "Desde",
                valor = filtros.fechaDesde?.format(formatoFecha),
                error = estado.errores.fechaDesde,
                modifier = Modifier.weight(1f),
                onClick = {
                    elegirFecha(filtros.fechaDesde) { fecha ->
                        onFiltrosChange(
                            filtros.copy(fechaDesde = fecha),
                        )
                    }
                },
            )

            CampoFiltro(
                titulo = "Hasta",
                valor = filtros.fechaHasta?.format(formatoFecha),
                error = estado.errores.fechaHasta,
                modifier = Modifier.weight(1f),
                onClick = {
                    elegirFecha(filtros.fechaHasta) { fecha ->
                        onFiltrosChange(
                            filtros.copy(fechaHasta = fecha),
                        )
                    }
                },
            )
        }

        Text(
            text = "Hora: ",
            style = MaterialTheme.typography.titleSmall
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CampoFiltro(
                titulo = "Desde",
                valor = filtros.horaDesde?.format(formatoHorario),
                error = estado.errores.horaDesde,
                modifier = Modifier.weight(1f),
                onClick = {
                    elegirHora(filtros.horaDesde) { hora ->
                        onFiltrosChange(
                            filtros.copy(horaDesde = hora),
                        )
                    }
                },
            )
            CampoFiltro(
                titulo = "Hasta",
                valor = filtros.horaHasta?.format(formatoHorario),
                error = estado.errores.horaHasta,
                modifier = Modifier.weight(1f),
                onClick = {
                    elegirHora(filtros.horaHasta) { hora ->
                        onFiltrosChange(
                            filtros.copy(horaHasta = hora),
                        )
                    }
                },
            )
        }

        FilterActionBar(
            onClear = onLimpiarFiltros,
            onApply = onAplicarFiltros,
        )
    }
}

@Composable
private fun CampoFiltro(
    titulo: String,
    valor: String?,
    error: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
        )

        FilterButton(
            text = valor ?: "Seleccionar",
            selected = valor != null,
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            role = Role.Button,
        )

        if (error != null) {
            ErrorMessage(text = error)
        }
    }
}
