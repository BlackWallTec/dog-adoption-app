package com.app.android.presentation.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.atoms.QCard
import com.app.android.presentation.designsystem.atoms.QDivider
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

// Un bloque de la galería: título de grupo + sus moléculas
@Composable
private fun GallerySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(QSpacing.m)) {
        QText(text = title, style = QTextStyle.SectionTitle)
        content()
    }
}

/** Galería de TODAS las moléculas del design system, agrupadas por tema, con estado interactivo. */
@Preview(showBackground = true, widthDp = 360, heightDp = 2400, name = "Galería de moléculas")
@Composable
private fun QMoleculesGalleryPreview() {
    QarinoTheme {
        var name by remember { mutableStateOf("") }
        var reference by remember { mutableStateOf("") }
        var accepted by remember { mutableStateOf(false) }
        var housing by remember { mutableIntStateOf(0) }
        var notifications by remember { mutableStateOf(true) }
        var sizes by remember { mutableStateOf(setOf(0, 2)) }
        var sex by remember { mutableStateOf(setOf(0)) }
        var people by remember { mutableIntStateOf(2) }

        Column(
            modifier =
                Modifier
                    .background(QTheme.colors.background)
                    .verticalScroll(rememberScrollState())
                    .padding(QSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(QSpacing.xxl),
        ) {
            GallerySection(title = "Datos y etiquetas") {
                QIconLabel(icon = QIcons.Location, text = "Refugio Principal")
                QIconLabel(icon = QIcons.Calendar, text = "Dom. 15 nov. 2026", style = QTextStyle.BodyStrong)
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QInfoTile(label = "Edad", value = "5 años")
                    QInfoTile(label = "Sexo", value = "Macho")
                    QInfoTile(label = "Raza", value = null)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.l)) {
                    QCheckItem(text = "Vacunado")
                    QCheckItem(text = "Esterilizado", checked = false)
                }
            }

            GallerySection(title = "Campos de formulario") {
                QLabeledTextField(label = "Nombre completo", value = name, onValueChange = { name = it }, placeholder = "Escribe tu nombre")
                QLabeledTextField(
                    label = "Referencia (opcional)",
                    value = reference,
                    onValueChange = { reference = it },
                    placeholder = "Folio o referencia",
                    leadingIcon = QIcons.Hash,
                    helperText = "Ayuda al refugio a encontrar tu transferencia.",
                )
                QLabeledTextField(
                    label = "Correo",
                    value = "correo-invalido",
                    onValueChange = {},
                    placeholder = "correo@ejemplo.com",
                    errorText = "Escribe un correo válido.",
                )
                QLabeledSelectField(label = "Desde", value = null, onClick = {})
                QLabeledSelectField(label = "Hasta", value = "15 nov. 2026", onClick = {})
            }

            GallerySection(title = "Selección") {
                QLabeledCheckbox(
                    text = "Acepto el aviso de privacidad",
                    checked = accepted,
                    onCheckedChange = { accepted = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                listOf("Casa con jardín", "Departamento").forEachIndexed { index, option ->
                    QLabeledRadioButton(
                        text = option,
                        selected = index == housing,
                        onClick = { housing = index },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                QLabeledSwitch(
                    text = "Notificaciones",
                    checked = notifications,
                    onCheckedChange = { notifications = it },
                    supportingText = "Te avisamos cuando cambie tu solicitud.",
                    modifier = Modifier.fillMaxWidth(),
                )
                QFilterChipGroup(
                    options = listOf("Chico", "Mediano", "Grande"),
                    selectedIndices = sizes,
                    onToggle = { index -> sizes = if (index in sizes) sizes - index else sizes + index },
                )
                QFilterChipGroup(
                    options = listOf("Macho", "Hembra"),
                    selectedIndices = sex,
                    onToggle = { index -> sex = setOf(index) },
                )
                QCounter(value = people, onDecrease = { people-- }, onIncrease = { people++ }, min = 0, max = 6)
            }

            GallerySection(title = "Mensajes") {
                QInfoMessage(text = "Aún no tienes voluntariados agendados.")
                QInfoMessage(title = "Importante", text = "Tu solicitud se revisa en 3 días hábiles.", important = true)
                QErrorMessage(text = "No se pudo enviar tu solicitud.")
            }

            GallerySection(title = "Secciones") {
                QSectionHeader(title = "Datos bancarios", subtitle = "Realiza tu transferencia a esta cuenta.")
                QSection(title = "Salud y cuidados") {
                    QCheckItem(text = "Vacunado")
                    QCheckItem(text = "Esterilizado")
                }
            }

            GallerySection(title = "Donativos") {
                QCard(modifier = Modifier.fillMaxWidth()) {
                    QCopyRow(label = "Banco", value = "BBVA", onCopy = {})
                    QDivider()
                    QCopyRow(label = "CLABE", value = "012345678901234567", onCopy = {})
                }
                QUploadButton(onClick = {}, helperText = "JPG, PNG o PDF · Máx. 10 MB")
                QUploadButton(onClick = {}, fileName = "comprobante.pdf", onRemoveFile = {})
            }

            GallerySection(title = "Programas") {
                QCapacity(text = "13/25 lugares ocupados · 12 disponibles", progress = 0.52f)
                QCapacity(text = "25/25 lugares ocupados · 0 disponibles", progress = 1f, isFull = true)
            }

            GallerySection(title = "Navegación") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    QNavItem(label = "Catálogo", icon = QIcons.Paw, selected = true, onClick = {})
                    QNavItem(label = "Ayudar", icon = QIcons.Help, selected = false, onClick = {})
                    QNavItem(label = "Donar", icon = QIcons.Donate, selected = false, onClick = {})
                }
                QStepIndicator(currentStep = 3, totalSteps = 6)
                QCard(modifier = Modifier.fillMaxWidth()) {
                    QListItem(title = "Mi cuenta", subtitle = "Nombre y contraseña", icon = QIcons.Settings, onClick = {})
                    QDivider()
                    QListItem(title = "Cerrar sesión", onClick = {})
                }
            }
        }
    }
}