package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.domain.entities.entitiesCatalogo.CareParagraph
import com.app.android.domain.entities.entitiesCatalogo.FormattedSpecialCare
import com.app.android.presentation.designsystem.atoms.QButton
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QTag
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.model.QMediaItem
import com.app.android.presentation.designsystem.molecules.QCheckItem
import com.app.android.presentation.designsystem.molecules.QInfoMessage
import com.app.android.presentation.designsystem.molecules.QSection
import com.app.android.presentation.designsystem.organisms.QDogHeader
import com.app.android.presentation.designsystem.organisms.QInfoTileRow
import com.app.android.presentation.designsystem.organisms.QPhotoCarousel
import com.app.android.presentation.designsystem.organisms.QTab
import com.app.android.presentation.designsystem.templates.QBackground
import com.app.android.presentation.designsystem.templates.QDetailTemplate
import com.app.android.presentation.designsystem.templates.QStateContent
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QarinoTheme

/**
 * Detalle de un perrito: carrusel de fotos/videos, encabezado (nombre, estado, ubicación, Compartir),
 * fila de datos (Edad, Sexo, Tamaño, Raza) y secciones opcionales (raza y tamaño, salud, características,
 * sobre mí, cuidados especiales). Una sección sin datos no se dibuja. Botón "¡Quiero Adoptar!" fijo abajo.
 *
 * Dueño: P5 (Páginas).
 * Usa (obligatorio): QDetailTemplate, QPhotoCarousel, QDogHeader, QInfoTileRow, QSection, QButton, QStateContent.
 * Reemplaza a: DogDetailScreenView, DogDetailTemplate (+ TarjetaInfo).
 */
@Composable
fun QDetallePerroPage(
    isLoading: Boolean,
    errorMessage: String?,
    name: String,
    statusText: String,
    status: QStatus,
    location: String,
    media: List<QMediaItem>,
    infoTiles: List<Pair<String, String?>>,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onShare: () -> Unit,
    onAdoptClick: () -> Unit,
    onTabSelected: (QTab) -> Unit,
    modifier: Modifier = Modifier,
    breedDetails: List<String> = emptyList(),
    healthItems: List<String> = emptyList(),
    characteristics: List<String> = emptyList(),
    aboutMe: String = "",
    specialCare: FormattedSpecialCare? = null,
    isEmpty: Boolean = false,
    onVideoClick: (QMediaItem.Video) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    notificationCount: Int = 0,
) {
    val showContent = !isLoading && errorMessage == null && !isEmpty

    QDetailTemplate(
        title = name.ifBlank { "Detalle" },
        subtitle = "Conoce a tu posible compañero.",
        onBack = onBack,
        selectedTab = QTab.Catalogo,
        onTabSelected = onTabSelected,
        modifier = modifier,
        onNotificationsClick = onNotificationsClick,
        notificationCount = notificationCount,
        background = QBackground.Plain,
        stickyAction =
            if (showContent) {
                { QButton(text = "¡Quiero Adoptar!", onClick = onAdoptClick, fillMaxWidth = true) }
            } else {
                null
            },
    ) {
        QStateContent(
            isLoading = isLoading,
            errorMessage = errorMessage,
            onRetry = onRetry,
            isEmpty = isEmpty,
            emptyMessage = "No hay datos disponibles",
        ) {
            QPhotoCarousel(media = media, onVideoClick = onVideoClick)
            QDogHeader(
                name = name,
                statusText = statusText,
                status = status,
                location = location,
                onShare = onShare,
            )
            QInfoTileRow(items = infoTiles)

            if (breedDetails.isNotEmpty()) {
                QSection(title = "Detalles de Raza y Tamaño") {
                    breedDetails.forEach { detail -> QText(text = "• $detail") }
                }
            }
            if (healthItems.isNotEmpty()) {
                QSection(title = "Salud y cuidados") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
                        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
                    ) {
                        healthItems.forEach { item -> QCheckItem(text = item) }
                    }
                }
            }
            if (characteristics.isNotEmpty()) {
                QSection(title = "Características especiales") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
                        verticalArrangement = Arrangement.spacedBy(QSpacing.s),
                    ) {
                        characteristics.forEach { item -> QTag(text = item) }
                    }
                }
            }
            if (aboutMe.isNotBlank()) {
                QSection(title = "Sobre mí") {
                    QText(text = aboutMe)
                }
            }
            val careParagraphs = specialCare?.paragraphs.orEmpty()
            if (careParagraphs.isNotEmpty()) {
                QSection(title = "Notas y cuidados especiales") {
                    careParagraphs.forEach { paragraph -> CareParagraphItem(paragraph = paragraph) }
                }
            }
        }
    }
}

// Párrafo de cuidados: las alertas (miedos, alergias, medicamentos) se resaltan; las cosas que ayudan van como tags.
@Composable
private fun CareParagraphItem(paragraph: CareParagraph) {
    if (paragraph.isUrgentOrAlert) {
        QInfoMessage(text = paragraph.text, important = true)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(QSpacing.s)) {
            QText(text = paragraph.text)
            if (paragraph.extractedItems.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(QSpacing.s),
                    verticalArrangement = Arrangement.spacedBy(QSpacing.s),
                ) {
                    paragraph.extractedItems.forEach { item -> QTag(text = item) }
                }
            }
        }
    }
}

private val previewCare =
    FormattedSpecialCare(
        hasContent = true,
        paragraphs =
            listOf(
                CareParagraph(text = "Se asusta con los sonidos fuertes y los truenos.", isUrgentOrAlert = true),
                CareParagraph(
                    text = "Para calmarlo le pueden dar juguetes, comida o una pelota.",
                    extractedItems = listOf("Juguetes", "Comida", "Una pelota"),
                ),
            ),
    )

@Composable
private fun PreviewDetalle(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    aboutMe: String = "Me encantan los paseos largos y jugar con la pelota. Busco una familia paciente y cariñosa.",
) {
    QarinoTheme {
        QDetallePerroPage(
            isLoading = isLoading,
            errorMessage = errorMessage,
            name = "Firulais",
            statusText = "Disponible",
            status = QStatus.Positive,
            location = "Refugio Principal",
            media = emptyList(),
            infoTiles = listOf("Edad" to "5 años", "Sexo" to "Macho", "Tamaño" to "Chico", "Raza" to "Mestizo"),
            onBack = {},
            onRetry = {},
            onShare = {},
            onAdoptClick = {},
            onTabSelected = {},
            breedDetails =
                listOf(
                    "Raza (Mestizo): Cruza única e invaluable",
                    "Tamaño (Chico): Menor a 45cm de altura",
                ),
            healthItems = listOf("Vacunado", "Esterilizado"),
            characteristics = listOf("Amigable con niños", "Convive con gatos"),
            aboutMe = aboutMe,
            specialCare = previewCare,
        )
    }
}

@Preview(showBackground = true, heightDp = 1400, name = "QDetallePerroPage - normal")
@Composable
private fun QDetallePerroPagePreview() {
    PreviewDetalle()
}

@Preview(showBackground = true, heightDp = 800, name = "QDetallePerroPage - cargando")
@Composable
private fun QDetallePerroPageLoadingPreview() {
    PreviewDetalle(isLoading = true)
}

@Preview(showBackground = true, heightDp = 800, name = "QDetallePerroPage - error")
@Composable
private fun QDetallePerroPageErrorPreview() {
    PreviewDetalle(errorMessage = "No pudimos cargar la información del perrito.")
}

@Preview(showBackground = true, heightDp = 1600, name = "QDetallePerroPage - texto largo")
@Composable
private fun QDetallePerroPageLongTextPreview() {
    PreviewDetalle(aboutMe = "Soy un perrito muy tranquilo. ".repeat(20))
}
