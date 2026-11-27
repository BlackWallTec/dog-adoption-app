package com.app.android.presentation.designsystem.pages

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesVoluntariado.ErroresFiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.organisms.QDateTimeFilterPanel
import com.app.android.presentation.designsystem.organisms.QProgramCard
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QMainTabTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.viewmodel.viewModelVoluntariado.AccionPrograma
import com.app.android.presentation.viewmodel.viewModelVoluntariado.FiltrosProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val SHELTER_ZONE: ZoneId = ZoneId.of("America/Mexico_City")
private const val DEFAULT_PICKER_HOUR = 9

/**
 * Catálogo de programas de voluntariado (pestaña Ayudar): botón "Mis voluntariados", panel para filtrar por
 * día y hora (abre los selectores nativos de fecha y hora) y la lista de QProgramCard. El panel solo se
 * muestra cuando la lista ya cargó. Si los filtros no encuentran nada, ofrece limpiarlos.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QMainTabTemplate, QDateTimeFilterPanel, QProgramCard, QStateContent.
 * Reemplaza a: ProgramasPage (ProgramasScreen).
 */
@Composable
fun QProgramasPage(
    isLoading: Boolean,
    errorMessage: String?,
    programs: List<ProgramaUi>,
    filters: FiltrosProgramasUiState,
    onFiltersChange: (FiltrosProgramas) -> Unit,
    onApplyFilters: () -> Unit,
    onClearFilters: () -> Unit,
    onRetry: () -> Unit,
    onProgramClick: (programId: String) -> Unit,
    onEnroll: (programId: String) -> Unit,
    onCancel: (programId: String) -> Unit,
    onMyVolunteeringClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    val context = LocalContext.current
    val selected = filters.seleccionados
    val noMatches = filters.sinCoincidencias

    QMainTabTemplate(
        title = "Programas",
        subtitle = "Elige un programa de voluntariado para ayudar a los perritos de Qariño.",
        selectedTab = QTab.Ayudar,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
    ) {
        QButton(text = "Mis voluntariados", onClick = onMyVolunteeringClick, fillMaxWidth = true)

        if (!isLoading && errorMessage == null) {
            QDateTimeFilterPanel(
                fromDate = selected.fechaDesde?.format(DATE_FORMAT),
                toDate = selected.fechaHasta?.format(DATE_FORMAT),
                fromTime = selected.horaDesde?.format(TIME_FORMAT),
                toTime = selected.horaHasta?.format(TIME_FORMAT),
                onFromDateClick = {
                    pickDate(context, selected.fechaDesde) { onFiltersChange(selected.copy(fechaDesde = it)) }
                },
                onToDateClick = {
                    pickDate(context, selected.fechaHasta) { onFiltersChange(selected.copy(fechaHasta = it)) }
                },
                onFromTimeClick = {
                    pickTime(context, selected.horaDesde) { onFiltersChange(selected.copy(horaDesde = it)) }
                },
                onToTimeClick = {
                    pickTime(context, selected.horaHasta) { onFiltersChange(selected.copy(horaHasta = it)) }
                },
                onClear = onClearFilters,
                onApply = onApplyFilters,
                errorText = filters.errores.firstMessage(),
            )
        }

        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            isEmpty = programs.isEmpty() || noMatches,
            emptyMessage =
                if (noMatches) "No hay programas en esas fechas u horario." else "No hay programas disponibles por ahora.",
            emptyActionText = if (noMatches) "Limpiar filtros" else null,
            onEmptyAction = if (noMatches) onClearFilters else null,
        ) {
            programs.forEach { program ->
                ProgramItem(
                    program = program,
                    onClick = { onProgramClick(program.id) },
                    onEnroll = { onEnroll(program.id) },
                    onCancel = { onCancel(program.id) },
                )
            }
        }
    }
}

// Traduce lo que decidió el ViewModel (accion, botonHabilitado) a los botones de QProgramCard.
@Composable
private fun ProgramItem(
    program: ProgramaUi,
    onClick: () -> Unit,
    onEnroll: () -> Unit,
    onCancel: () -> Unit,
) {
    val isFull = program.progresoCupo >= 1f
    val canCancel = program.accion == AccionPrograma.CANCELAR
    // Lleno: el botón se ve deshabilitado. Otros motivos (ya comenzó, no disponible): sin botón.
    val showEnroll = !canCancel && (program.botonHabilitado || isFull)
    QProgramCard(
        type = program.tipo,
        title = program.titulo,
        date = program.fecha,
        time = program.horario,
        location = program.ubicacion,
        capacityText = program.textoCupo,
        capacityProgress = program.progresoCupo,
        onClick = onClick,
        isFull = isFull,
        enrolled = canCancel,
        onEnroll = if (showEnroll) onEnroll else null,
        onCancel = if (canCancel) onCancel else null,
    )
}

// QDateTimeFilterPanel solo tiene un mensaje de error: se muestra el primero que haya.
private fun ErroresFiltrosProgramas.firstMessage(): String? = fechaDesde ?: fechaHasta ?: horaDesde ?: horaHasta

private fun pickDate(
    context: Context,
    current: LocalDate?,
    onPicked: (LocalDate) -> Unit,
) {
    val initial = current ?: LocalDate.now(SHELTER_ZONE)
    DatePickerDialog(
        context,
        { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}

private fun pickTime(
    context: Context,
    current: LocalTime?,
    onPicked: (LocalTime) -> Unit,
) {
    val initial = current ?: LocalTime.of(DEFAULT_PICKER_HOUR, 0)
    TimePickerDialog(
        context,
        { _, hour, minute -> onPicked(LocalTime.of(hour, minute)) },
        initial.hour,
        initial.minute,
        true,
    ).show()
}

private fun previewProgram(
    id: String,
    title: String,
    capacityText: String,
    progress: Float,
    enabled: Boolean = true,
    action: AccionPrograma = AccionPrograma.INSCRIBIRSE,
) = ProgramaUi(
    id = id,
    tipo = "Voluntariado",
    titulo = title,
    fecha = "Dom. 15 nov. 2026",
    horario = "09:00 – 14:00 h",
    ubicacion = "Refugio Qariño · Querétaro",
    textoCupo = capacityText,
    progresoCupo = progress,
    textoBoton = if (enabled) "Inscribirme" else "Sin lugares",
    botonHabilitado = enabled,
    accion = action,
)

private val previewPrograms =
    listOf(
        previewProgram("1", "Paseos y socialización de fin de semana", "8/25 lugares ocupados · 17 disponibles", 8f / 25f),
        previewProgram("2", "Jornada de limpieza", "10/10 lugares ocupados · 0 disponibles", 1f, enabled = false),
        previewProgram("3", "Bazar de donativos", "3/15 lugares ocupados · 12 disponibles", 0.2f, action = AccionPrograma.CANCELAR),
    )

@Composable
private fun PreviewProgramas(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    programs: List<ProgramaUi> = previewPrograms,
    filters: FiltrosProgramasUiState = FiltrosProgramasUiState(),
) {
    QarinoTheme {
        QProgramasPage(
            isLoading = isLoading,
            errorMessage = errorMessage,
            programs = programs,
            filters = filters,
            onFiltersChange = {},
            onApplyFilters = {},
            onClearFilters = {},
            onRetry = {},
            onProgramClick = {},
            onEnroll = {},
            onCancel = {},
            onMyVolunteeringClick = {},
            onTabSelected = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 1600, name = "QProgramasPage - lista")
@Composable
private fun QProgramasPagePreview() {
    PreviewProgramas()
}

@Preview(showBackground = true, heightDp = 800, name = "QProgramasPage - cargando")
@Composable
private fun QProgramasPageLoadingPreview() {
    PreviewProgramas(isLoading = true)
}

@Preview(showBackground = true, heightDp = 900, name = "QProgramasPage - sin coincidencias")
@Composable
private fun QProgramasPageNoMatchesPreview() {
    PreviewProgramas(
        programs = emptyList(),
        filters =
            FiltrosProgramasUiState(
                seleccionados = FiltrosProgramas(fechaDesde = LocalDate.of(2026, 12, 1)),
                errores = ErroresFiltrosProgramas(fechaHasta = "Elige una fecha final."),
                sinCoincidencias = true,
            ),
    )
}

@Preview(showBackground = true, heightDp = 800, name = "QProgramasPage - error")
@Composable
private fun QProgramasPageErrorPreview() {
    PreviewProgramas(errorMessage = "No se pudieron cargar los programas")
}
