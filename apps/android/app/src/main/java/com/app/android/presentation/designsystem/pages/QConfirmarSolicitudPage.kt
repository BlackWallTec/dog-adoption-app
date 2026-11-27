package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QPhoto
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.molecules.QCheckItem
import com.app.android.presentation.designsystem.molecules.QErrorMessage
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QLabeledCheckbox
import com.app.android.presentation.designsystem.organisms.QSummaryCard
import com.app.android.presentation.designsystem.templates.QFormStepTemplate
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.view.atomicDesign.page.CampoResumen
import com.app.android.presentation.view.atomicDesign.page.ConfirmarSolicitudUiState
import com.app.android.presentation.view.atomicDesign.page.MascotaResumen
import com.app.android.presentation.view.atomicDesign.page.SeccionResumen
import com.app.android.presentation.view.atomicDesign.page.SeccionSolicitud

private const val TOTAL_STEPS = 6
private val PET_PHOTO_SIZE = 64.dp

/**
 * Paso 6 de 6 (Confirmar): resumen de la mascota y de cada sección de la solicitud con "Editar" para volver
 * a ese paso, lista de documentos y la casilla de aceptación. "Enviar solicitud" se habilita al aceptar y
 * muestra carga mientras se envía. La casilla es estado visual local (como en la pantalla anterior).
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QFormStepTemplate, QSummaryCard, QInfoMessage, QLabeledCheckbox, QButton (del template).
 * Reemplaza a: ConfirmarSolicitudScreen (ConfirmarRoute).
 */
@Composable
fun QConfirmarSolicitudPage(
    state: ConfirmarSolicitudUiState,
    onBack: () -> Unit,
    onEditSection: (SeccionSolicitud) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
) {
    var acceptedTerms by rememberSaveable { mutableStateOf(false) }

    QFormStepTemplate(
        title = "Solicitud de adopción",
        subtitle = "Confirmar",
        currentStep = 6,
        totalSteps = TOTAL_STEPS,
        onBack = onBack,
        primaryActionText = "Enviar solicitud",
        onPrimaryAction = onSubmit,
        modifier = modifier,
        primaryActionEnabled = acceptedTerms && !state.enviando,
        isLoading = state.enviando,
    ) {
        QText(
            text = "Revisa tus datos antes de enviar. ¡Estás a un paso de conocer a ${state.mascota.nombre}!",
        )

        PetSummaryCard(pet = state.mascota)

        state.secciones.forEach { section ->
            SectionSummary(section = section, onEdit = { onEditSection(section.tipo) })
        }

        QSummaryCard(title = "Documentos", onEdit = { onEditSection(SeccionSolicitud.DOCUMENTOS) }) {
            if (state.documentos.isEmpty()) {
                QText(text = "Pendiente", style = QTextStyle.Caption)
            }
            state.documentos.forEach { fileName -> QCheckItem(text = fileName) }
        }

        QLabeledCheckbox(
            text = "Acepto los requisitos de adopción y el compromiso de cuidado y seguimiento.",
            checked = acceptedTerms,
            onCheckedChange = { acceptedTerms = it },
        )

        QInfoMessage(text = "Este formato no obliga a ninguna parte; es el primer paso del proceso.")

        if (errorMessage != null) {
            QErrorMessage(text = errorMessage)
        }
    }
}

@Composable
private fun SectionSummary(
    section: SeccionResumen,
    onEdit: () -> Unit,
) {
    QSummaryCard(title = section.titulo, onEdit = onEdit) {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
            section.campos.forEach { field -> SummaryField(field = field) }
        }
    }
}

// Etiqueta chica + valor ("Pendiente" cuando no hay dato), leídos juntos por el lector de pantalla.
@Composable
private fun SummaryField(field: CampoResumen) {
    Column(modifier = Modifier.semantics(mergeDescendants = true) {}) {
        QText(text = field.etiqueta, style = QTextStyle.Caption)
        QText(text = field.valor, style = QTextStyle.BodyStrong)
    }
}

@Composable
private fun PetSummaryCard(pet: MascotaResumen) {
    QCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QPhoto(
                model = pet.fotoUrl,
                contentDescription = "Foto de ${pet.nombre}",
                modifier = Modifier.size(PET_PHOTO_SIZE),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(QSpacing.xxs),
            ) {
                QText(text = pet.nombre, style = QTextStyle.Title, maxLines = 1)
                QText(text = pet.detalle)
                QText(text = pet.raza, style = QTextStyle.Caption)
            }
        }
    }
}

private val previewState =
    ConfirmarSolicitudUiState(
        mascota = MascotaResumen(nombre = "Firulais", detalle = "2 años · Macho · Mediano", raza = "Mestizo · Querétaro"),
        secciones =
            listOf(
                SeccionResumen(
                    tipo = SeccionSolicitud.DATOS_GENERALES,
                    titulo = "Datos generales",
                    campos =
                        listOf(
                            CampoResumen("Nombre completo", "Persona Ejemplo"),
                            CampoResumen("Edad", "31 años"),
                            CampoResumen("Ocupación", "Pendiente"),
                            CampoResumen("Correo", "correo@ejemplo.com"),
                        ),
                ),
                SeccionResumen(
                    tipo = SeccionSolicitud.TU_HOGAR,
                    titulo = "Tu hogar",
                    campos =
                        listOf(
                            CampoResumen("¿Has tenido mascotas antes?", "Sí"),
                            CampoResumen("Casa o departamento", "Casa"),
                        ),
                ),
            ),
        documentos = listOf("identificacion.pdf", "comprobante_domicilio.pdf"),
    )

@Preview(showBackground = true, heightDp = 1400, name = "QConfirmarSolicitudPage - normal")
@Composable
private fun QConfirmarSolicitudPagePreview() {
    QarinoTheme {
        QConfirmarSolicitudPage(state = previewState, onBack = {}, onEditSection = {}, onSubmit = {})
    }
}

@Preview(showBackground = true, heightDp = 1400, name = "QConfirmarSolicitudPage - enviando con error")
@Composable
private fun QConfirmarSolicitudPageSendingPreview() {
    QarinoTheme {
        QConfirmarSolicitudPage(
            state = previewState.copy(enviando = true, documentos = emptyList()),
            onBack = {},
            onEditSection = {},
            onSubmit = {},
            errorMessage = "No pudimos enviar tu solicitud. Intenta de nuevo.",
        )
    }
}
