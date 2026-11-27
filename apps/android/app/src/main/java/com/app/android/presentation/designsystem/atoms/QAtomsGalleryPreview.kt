package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.designsystem.theme.QIcons
import com.app.android.presentation.designsystem.theme.QSize
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private const val PHOTO_RATIO = 16f / 9f

/**
 * Galería con TODOS los átomos en una sola pantalla para revisarlos juntos.
 * Ábrela en Split/Design. Con "Start Interactive Mode" (o "Run Preview" en el emulador) puedes tocar
 * los checkbox, switches, filtros y escribir en los campos.
 *
 * Dueño: P1 (Átomos). Solo es un preview: no se usa en la app.
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 2400, name = "Galería de átomos")
@Composable
private fun QAtomsGalleryPreview() {
    QarinoTheme {
        var checked by remember { mutableStateOf(true) }
        var radio by remember { mutableStateOf(0) }
        var switchOn by remember { mutableStateOf(true) }
        var filterMacho by remember { mutableStateOf(true) }
        var filterGatos by remember { mutableStateOf(false) }
        var amount by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("secreto") }

        Column(
            modifier =
                Modifier
                    .background(QTheme.colors.background)
                    .verticalScroll(rememberScrollState())
                    .padding(QSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(QSpacing.l),
        ) {
            GallerySection("QText") {
                QTextStyle.entries.forEach { style -> QText(text = style.name, style = style) }
            }
            GallerySection("QIcon · QIconButton") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QIcon(icon = QIcons.Paw, contentDescription = null, size = QSize.topBarLogo)
                    QIconButton(icon = QIcons.Back, contentDescription = "Regresar", onClick = {})
                    QIconButton(icon = QIcons.Notifications, contentDescription = "Notificaciones", onClick = {}, badgeCount = 3)
                    QIconButton(icon = QIcons.Copy, contentDescription = "Copiar", onClick = {}, tint = QTheme.colors.onAction)
                }
            }
            GallerySection("QButton · QTextLink · QLoadingIndicator") {
                QButton(text = "¡Quiero Adoptar!", onClick = {}, fillMaxWidth = true)
                QButton(text = "Subir comprobante", onClick = {}, icon = QIcons.Upload, fillMaxWidth = true)
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QButton(text = "Compartir", onClick = {}, size = QButtonSize.Small, icon = QIcons.Share)
                    QButton(text = "Inscribirme", onClick = {}, size = QButtonSize.Small, enabled = false)
                }
                QButton(text = "Enviando", onClick = {}, isLoading = true, fillMaxWidth = true)
                QButton(text = "Cancelar inscripción", onClick = {}, variant = QButtonVariant.Destructive, fillMaxWidth = true)
                QTextLink(text = "Ver detalles", onClick = {}, trailingIcon = QIcons.ExpandMore)
                QLoadingIndicator()
            }
            GallerySection("QStatusChip · QTag · QFilterChip") {
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QStatusChip(text = "Disponible", status = QStatus.Positive)
                    QStatusChip(text = "En revisión", status = QStatus.Pending)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QStatusChip(text = "No aprobada", status = QStatus.Negative)
                    QStatusChip(text = "Borrador", status = QStatus.Neutral)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QTag(text = "Amigable con niños")
                    QTag(text = "Paseo")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.s)) {
                    QFilterChip(text = "Macho", selected = filterMacho, onClick = { filterMacho = !filterMacho })
                    QFilterChip(text = "Convive con gatos", selected = filterGatos, onClick = { filterGatos = !filterGatos })
                }
            }
            GallerySection("QTextField · QSelectField") {
                QTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    placeholder = "\$0 MXN",
                    leadingIcon = QIcons.Money,
                    modifier = Modifier.fillMaxWidth(),
                )
                QTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Contraseña",
                    isPassword = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                QTextField(
                    value = "correo-mal",
                    onValueChange = {},
                    placeholder = "Correo",
                    isError = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                QSelectField(value = null, onClick = {}, modifier = Modifier.fillMaxWidth())
                QSelectField(value = "Dom. 15 nov. 2026", onClick = {}, modifier = Modifier.fillMaxWidth())
            }
            GallerySection("QCheckbox · QRadioButton · QSwitch") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    QCheckbox(checked = checked, onCheckedChange = { checked = it })
                    QRadioButton(selected = radio == 0, onClick = { radio = 0 })
                    QRadioButton(selected = radio == 1, onClick = { radio = 1 })
                    QSwitch(checked = switchOn, onCheckedChange = { switchOn = it })
                }
            }
            GallerySection("QProgressBar") {
                QProgressBar(progress = 0.53f)
                QProgressBar(progress = 1f, isFull = true)
            }
            GallerySection("QPhoto · QCard · QDivider") {
                QPhoto(
                    model = ColorPainter(QTheme.colors.brandStrong),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(PHOTO_RATIO),
                )
                QCard(modifier = Modifier.fillMaxWidth(), onClick = {}) {
                    QText(text = "Tarjeta presionable", style = QTextStyle.Title)
                    QDivider(modifier = Modifier.padding(vertical = QSpacing.s))
                    QText(text = "Contenido de la tarjeta")
                }
            }
        }
    }
}

@Composable
private fun GallerySection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
        QText(text = title, style = QTextStyle.SectionTitle, color = QTheme.colors.onActionStrong)
        content()
    }
}