package com.app.android.presentation.view.atomicDesign.organism.basicOrganisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesCatalogo.CareParagraph
import com.app.android.domain.entities.entitiesCatalogo.FormattedSpecialCare
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoCuerpo
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoTitulo
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.CareParagraphItem
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.ChipTag
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.SectionBlock

// 1. ENUM PARA ORDENAR Y CONTROLAR SECCIONES
enum class SeccionInfoTarjeta {
    UBICACION,
    SOBRE_MI,
    METRICAS_GENERALES,

    CARACTERISTICAS,

    SALUD_Y_CUIDADOS,

    CUIDADOS_ESPECIALES,

    DETALLES_RAZA_Y_TAMANO,
}

// 2. MODELO DE DATOS DESDE EL VIEWMODEL (BD)
data class DogDetailState(
    val nombre: String,
    val estado: String,
    val ubicacion: String,
    val metricas: Map<String, String>, // ej. "Edad" to "2 años"
    val salud: List<String>, // ej. ["Vacunado", "Esterilizado"]
    val caracteristicas: List<String>, // ej. ["Convive con gatos", "Cariñoso"]
    val sobreMi: String,
    val descripcionRaza: String = "",
    val descripcionTamano: String = "",
)

// 3. ORGANISMO SUPER MODULAR
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TarjetaInfo(
    modifier: Modifier = Modifier,
    dogState: DogDetailState,
    cuidadosEspeciales: FormattedSpecialCare? = null,
    // Por defecto renderiza todas las secciones en el orden estándar del diseño original
    ordenSecciones: List<SeccionInfoTarjeta> =
        listOf(
            SeccionInfoTarjeta.UBICACION,
            SeccionInfoTarjeta.SOBRE_MI,
            SeccionInfoTarjeta.METRICAS_GENERALES,
            SeccionInfoTarjeta.CARACTERISTICAS,
            SeccionInfoTarjeta.SALUD_Y_CUIDADOS,
            SeccionInfoTarjeta.CUIDADOS_ESPECIALES,
        ),
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Cabecera Fija: Nombre + Estado de Disponibilidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextoTitulo(texto = dogState.nombre)
                ChipTag(
                    texto = dogState.estado,
                    icono = Icons.Outlined.Circle,
                    colorFondo = Color(0xFFE8F5E9),
                    colorTexto = Color(0xFF2E7D32),
                )
            }

            // Renderizado dinámico según el orden especificado (Caso 5: Bucle Repetidor)
            ordenSecciones.forEach { seccion ->
                when (seccion) {
                    // A. UBICACIÓN
                    SeccionInfoTarjeta.UBICACION -> {
                        ChipTag(
                            texto = dogState.ubicacion,
                            icono = Icons.Outlined.LocationOn,
                            conChip = false,
                            colorTexto = Color.Gray,
                        )
                    }

                    // B. MÉTRICAS (Edad, Sexo, Tamaño, Raza)
                    SeccionInfoTarjeta.METRICAS_GENERALES -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            dogState.metricas.forEach { (titulo, valor) ->
                                ChipTag(
                                    texto = "$titulo\n$valor",
                                    colorFondo = Color(0xFFF5F5F5),
                                    colorTexto = Color.DarkGray,
                                )
                            }
                        }
                    }

                    // C. SALUD Y CUIDADOS
                    SeccionInfoTarjeta.SALUD_Y_CUIDADOS -> {
                        if (dogState.salud.isNotEmpty()) {
                            SectionBlock(titulo = "Salud y cuidados") {
                                // 💡 CAMBIO: Cambiamos Row por FlowRow y manejamos tanto espacio horizontal como vertical
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    dogState.salud.forEach { item ->
                                        ChipTag(
                                            texto = item,
                                            icono = Icons.Outlined.CheckCircle,
                                            conChip = false,
                                            tintIcono = Color(0xFF1976D2),
                                            colorTexto = Color.DarkGray,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // D. CARACTERÍSTICAS ESPECIALES (FlowRow acomoda chips automáticamente)
                    SeccionInfoTarjeta.CARACTERISTICAS -> {
                        if (dogState.caracteristicas.isNotEmpty()) {
                            SectionBlock(titulo = "Características") {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    dogState.caracteristicas.forEach { item ->
                                        ChipTag(
                                            texto = item,
                                            colorFondo = Color(0xFFE3F2FD),
                                            colorTexto = Color(0xFF1976D2),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // E. SOBRE MÍ (Biografía)[cite: 4]
                    SeccionInfoTarjeta.SOBRE_MI -> {
                        if (dogState.sobreMi.isNotBlank()) {
                            SectionBlock(titulo = "Sobre mí") {
                                TextoCuerpo(texto = dogState.sobreMi)
                            }
                        }
                    }

                    // F. DETALLES DE LA RAZA Y TAMAÑO
                    SeccionInfoTarjeta.DETALLES_RAZA_Y_TAMANO -> {
                        if (dogState.descripcionRaza.isNotBlank() || dogState.descripcionTamano.isNotBlank()) {
                            SectionBlock(titulo = "Detalles de Raza y Tamaño") {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (dogState.descripcionRaza.isNotBlank()) {
                                        TextoCuerpo(texto = "• Raza (${dogState.metricas["Raza"]}): ${dogState.descripcionRaza}")
                                    }
                                    if (dogState.descripcionTamano.isNotBlank()) {
                                        TextoCuerpo(texto = "• Tamaño (${dogState.metricas["Tamaño"]}): ${dogState.descripcionTamano}")
                                    }
                                }
                            }
                        }
                    }

                    // G. CUIDADOS ESPECIALES
                    // NUEVA SECCIÓN: CUIDADOS ESPECIALES (Párrafos enriquecidos del cuidador)
                    SeccionInfoTarjeta.CUIDADOS_ESPECIALES -> {
                        if (cuidadosEspeciales != null && cuidadosEspeciales.paragraphs.isNotEmpty()) {
                            SectionBlock(titulo = "Notas y cuidados especiales") {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    cuidadosEspeciales.paragraphs.forEach { paragraph ->
                                        CareParagraphItem(paragraph = paragraph)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4. PREVISUALIZACIONES (PREVIEWS)

// Dato dummy reutilizable para tus Previews

@Preview(showBackground = true, name = "Orden Normal")
@Composable
fun TarjetaInfoNormalPreview() {
    val dogInfo =
        DogDetailState(
            nombre = "Bruno",
            estado = "Disponible",
            ubicacion = "Querétaro, Qro.",
            metricas = mapOf("Edad" to "2 años", "Sexo" to "Macho", "Tamaño" to "Mediano", "Raza" to "Mestizo"),
            salud = listOf("Vacunado", "Esterilizado"),
            caracteristicas = listOf("Convive con gatos", "Cariñoso", "Juguetón"),
            sobreMi = "¡Hola, soy Bruno! Me encantan los paseos y jugar con mi pelota.",
        )
    TarjetaInfo(dogState = dogInfo)
}

@Preview(showBackground = true, name = "Orden Personalizado (Sobre mí primero)")
@Composable
fun TarjetaInfoReordenadaPreview() {
    val dogInfo =
        DogDetailState(
            nombre = "Bruno",
            estado = "Disponible",
            ubicacion = "Querétaro, Qro.",
            metricas = mapOf("Edad" to "2 años", "Sexo" to "Macho", "Tamaño" to "Mediano", "Raza" to "Mestizo"),
            salud = listOf("Vacunado", "Esterilizado"),
            caracteristicas = listOf("Convive con gatos", "Cariñoso"),
            sobreMi = "¡Hola, soy Bruno! Busco un hogar cariñoso.",
        )

    val dummySpecialCare =
        FormattedSpecialCare(
            hasContent = true,
            paragraphs =
                listOf(
                    CareParagraph(
                        text = "Bruno se asusta mucho con los sonidos fuertes y truenos.",
                        isUrgentOrAlert = true,
                    ),
                    CareParagraph(
                        text = "Para calmarlo le pueden dar juguetes, comida o una pelota.",
                        extractedItems = listOf("Juguetes", "Comida", "Una pelota"),
                        isUrgentOrAlert = false,
                    ),
                ),
        )

    // Invertimos el orden enviando la lista en la secuencia que queramos
    TarjetaInfo(
        dogState = dogInfo,
        ordenSecciones =
            listOf(
                SeccionInfoTarjeta.SOBRE_MI,
                SeccionInfoTarjeta.CARACTERISTICAS,
                SeccionInfoTarjeta.SALUD_Y_CUIDADOS,
                SeccionInfoTarjeta.METRICAS_GENERALES,
            ),
        cuidadosEspeciales = dummySpecialCare,
    )
}
