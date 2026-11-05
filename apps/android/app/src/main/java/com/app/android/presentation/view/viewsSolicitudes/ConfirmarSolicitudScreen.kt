package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.IndicadorPasos
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Primary
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainer
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh
import com.app.android.presentation.view.ui.theme.SurfaceContainerLow

// ---------- UI state (plain data, no ViewModel yet) ----------

enum class SeccionSolicitud { DATOS_GENERALES, TU_HOGAR, COMPROMISO, DOCUMENTOS }

data class CampoResumen(
    val etiqueta: String,
    val valor: String, // "Pendiente" cuando no hay dato
)

data class SeccionResumen(
    val tipo: SeccionSolicitud,
    val titulo: String,
    val campos: List<CampoResumen>,
)

data class MascotaResumen(
    val nombre: String,
    val detalle: String, // "2 años · Macho · Mediano"
    val raza: String, // "Mestizo · Querétaro"
    val fotoUrl: String? = null,
)

data class ConfirmarSolicitudUiState(
    val mascota: MascotaResumen,
    val secciones: List<SeccionResumen>, // datos generales, tu hogar, compromiso
    val documentos: List<String>, // nombres de archivo
    val enviando: Boolean = false,
)

// ---------- Screen ----------

// PAGE: paso 6 de 6 (Confirmar). Sin estado propio: todo entra por parametros.
@Composable
fun ConfirmarSolicitudScreen(
    state: ConfirmarSolicitudUiState,
    aceptaTerminos: Boolean,
    onAceptaTerminosChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onEditarClick: (SeccionSolicitud) -> Unit,
    onEnviarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Surface),
    ) {
        AppTopBar(
            titulo = "Solicitud de adopción",
            onBackClick = onBackClick,
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IndicadorPasos(pasoActual = 5)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Confirmar",
                    color = InverseSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text =
                        "Revisa tus datos antes de enviar. " +
                                "¡Estás a un paso de conocer a ${state.mascota.nombre}!",
                    color = InverseSurface,
                    fontSize = 16.sp,
                )
            }

            TarjetaMascotaResumen(mascota = state.mascota)

            state.secciones.forEach { seccion ->
                TarjetaSeccion(
                    titulo = seccion.titulo,
                    onEditarClick = { onEditarClick(seccion.tipo) },
                ) {
                    seccion.campos.forEach { campo ->
                        CampoDeResumen(campo)
                    }
                }
            }

            TarjetaSeccion(
                titulo = "Documentos",
                onEditarClick = { onEditarClick(SeccionSolicitud.DOCUMENTOS) },
            ) {
                state.documentos.forEach { nombre ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = nombre,
                            color = InverseSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            FilaAceptarTerminos(
                checked = aceptaTerminos,
                onCheckedChange = onAceptaTerminosChange,
            )

            Text(
                text = "Este formato no obliga a ninguna parte; es el primer paso del proceso",
                color = InverseSurface.copy(alpha = 0.7f),
                fontSize = 12.sp,
            )

            Button(
                onClick = onEnviarClick,
                enabled = aceptaTerminos && !state.enviando,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                shape = RoundedCornerShape(50),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = InverseSurface,
                        disabledContainerColor = SurfaceContainerHigh,
                        disabledContentColor = InverseSurface.copy(alpha = 0.5f),
                    ),
            ) {
                Text(
                    text = if (state.enviando) "Enviando..." else "Enviar solicitud",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ---------- Private building blocks ----------
// Se quedan locales hasta que otra pantalla las necesite (luego se mueven a atomic/molecule).

@Composable
private fun TarjetaMascotaResumen(
    mascota: MascotaResumen,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainer)
                .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val fotoModifier =
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))

        if (mascota.fotoUrl != null) {
            AsyncImage(
                model = mascota.fotoUrl,
                contentDescription = "Foto de ${mascota.nombre}",
                contentScale = ContentScale.Crop,
                modifier = fotoModifier,
            )
        } else {
            Box(
                modifier = fotoModifier.background(SurfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Pets,
                    contentDescription = null,
                    tint = InverseSurface.copy(alpha = 0.6f),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = mascota.nombre,
                color = InverseSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(text = mascota.detalle, color = InverseSurface, fontSize = 14.sp)
            Text(
                text = mascota.raza,
                color = InverseSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun TarjetaSeccion(
    titulo: String,
    onEditarClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceContainer)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titulo,
                color = InverseSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onEditarClick,
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                Text(
                    text = "Editar",
                    color = InverseSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        content()
    }
}

@Composable
private fun CampoDeResumen(
    campo: CampoResumen,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = campo.etiqueta,
            color = InverseSurface.copy(alpha = 0.7f),
            fontSize = 12.sp,
        )
        Text(
            text = campo.valor,
            color = InverseSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FilaAceptarTerminos(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Toda la fila es clickeable; el Checkbox solo muestra el estado
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    role = Role.Checkbox,
                    onValueChange = onCheckedChange,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            colors =
                CheckboxDefaults.colors(
                    checkedColor = Secondary,
                    uncheckedColor = InverseSurface,
                ),
        )
        Text(
            text = "Acepto los requisitos de adopción y el compromiso de cuidado y seguimiento.",
            color = InverseSurface,
            fontSize = 14.sp,
        )
    }
}

// ---------- Preview ----------

private val previewState =
    ConfirmarSolicitudUiState(
        mascota =
            MascotaResumen(
                nombre = "Bruno",
                detalle = "2 años · Macho · Mediano",
                raza = "Mestizo · Querétaro",
            ),
        secciones =
            listOf(
                SeccionResumen(
                    tipo = SeccionSolicitud.DATOS_GENERALES,
                    titulo = "Datos generales",
                    campos =
                        listOf(
                            CampoResumen("Nombre completo", "Sofía García López"),
                            CampoResumen("Fecha de nacimiento", "15/03/1995"),
                            CampoResumen("Edad", "31 años"),
                            CampoResumen("Ocupación", "Pendiente"),
                            CampoResumen("Domicilio", "Calle Roble 24, Col. Álamos, Querétaro, Qro. C.P. 76160"),
                            CampoResumen("Delegación/Municipio", "Querétaro"),
                            CampoResumen("Teléfono", "442 123 4567"),
                            CampoResumen("Correo", "sofia.garcia@correo.com"),
                        ),
                ),
                SeccionResumen(
                    tipo = SeccionSolicitud.TU_HOGAR,
                    titulo = "Tu hogar",
                    campos =
                        listOf(
                            CampoResumen("¿Has tenido mascotas antes?", "Sí · Actualmente convives con Mía"),
                            CampoResumen(
                                "¿Tienes otros animales en casa?",
                                "Mía, gata de 3 años, esterilizada y vacunada. Tranquila; convive con perros.",
                            ),
                            CampoResumen("¿Todos están de acuerdo en adoptar?", "Sí · Todos estamos de acuerdo en adoptar."),
                            CampoResumen("Casa o departamento", "Casa"),
                            CampoResumen("¿Tienes jardín o patio?", "Patio cerrado"),
                            CampoResumen("Si rentas ¿tu casero acepta mascotas?", "Pendiente"),
                        ),
                ),
                SeccionResumen(
                    tipo = SeccionSolicitud.COMPROMISO,
                    titulo = "Compromiso",
                    campos =
                        listOf(
                            CampoResumen("Menciona los cuidados básicos de una mascota", "Pendiente"),
                            CampoResumen("¿Qué opinas de la esterilización?", "Pendiente"),
                            CampoResumen("¿Nos permites dar seguimiento (visitas o fotos)?", "Pendiente"),
                        ),
                ),
            ),
        documentos = listOf("INE_Sofia_Garcia.pdf", "Domicilio_Sofia_Garcia.pdf"),
    )

@Preview(showBackground = true, heightDp = 1600, name = "Confirmar solicitud")
@Composable
fun ConfirmarSolicitudScreenPreview() {
    AndroidTheme {
        var acepta by remember { mutableStateOf(false) }
        ConfirmarSolicitudScreen(
            state = previewState,
            aceptaTerminos = acepta,
            onAceptaTerminosChange = { acepta = it },
            onBackClick = {},
            onEditarClick = {},
            onEnviarClick = {},
        )
    }
}