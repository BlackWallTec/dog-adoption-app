package com.app.android.presentation.view.atomicDesign.organism.basicOrganisms

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.app.android.domain.entities.entitiesCatalogo.FinalFeliz
import com.app.android.presentation.view.atomicDesign.molecule.FotoFinalFeliz
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.ChipTag
import com.app.android.presentation.view.ui.theme.AndroidTheme
import kotlinx.coroutines.delay

private const val INTERVALO_AUTOAVANCE_MS = 5_000L
private val ALTURA_CARRUSEL = 200.dp

/**
 * Carrusel de finales felices. Una diapositiva es un perro.
 * Con dos o más fotos avanza solo, salvo que el cliente lo esté tocando, el modal esté abierto,
 * la pantalla no esté en primer plano o el sistema tenga el movimiento reducido.
 */
@Composable
fun CarruselFinalesFelices(
    finales: List<FinalFeliz>,
    modalAbierto: Boolean,
    onFotoClick: (FinalFeliz) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (finales.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { finales.size })
    val hayVarias = finales.size > 1
    val movimientoReducido = movimientoReducido(LocalContext.current)
    val cicloDeVida = LocalLifecycleOwner.current.lifecycle
    val estadoCiclo by cicloDeVida.currentStateFlow.collectAsState()
    val enPrimerPlano = estadoCiclo.isAtLeast(Lifecycle.State.RESUMED)
    val autoavanceActivo =
        hayVarias && !modalAbierto && !movimientoReducido && enPrimerPlano && !pagerState.isScrollInProgress

    LaunchedEffect(finales.size) {
        val ultima = finales.lastIndex
        if (pagerState.currentPage > ultima) {
            pagerState.scrollToPage(ultima.coerceAtLeast(0))
        }
    }

    LaunchedEffect(autoavanceActivo, pagerState.currentPage, finales.size) {
        if (!autoavanceActivo) return@LaunchedEffect
        delay(INTERVALO_AUTOAVANCE_MS)
        if (!pagerState.isScrollInProgress && finales.size > 1) {
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % finales.size)
        }
    }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .height(ALTURA_CARRUSEL),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = hayVarias,
                modifier = Modifier.fillMaxSize(),
            ) { pagina ->
                val final = finales[pagina]
                FotoFinalFeliz(
                    url = final.fotoHogarUrl,
                    contentDescription = "Final feliz de ${final.nombre}. Toca para ver su historia.",
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = !pagerState.isScrollInProgress,
                                onClick = { onFotoClick(final) },
                            ),
                )
            }

            if (hayVarias) {
                ChipTag(
                    texto = "${pagerState.currentPage + 1}/${finales.size}",
                    colorFondo = Color.Black.copy(alpha = 0.5f),
                    colorTexto = Color.White,
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                )
                Row(
                    modifier =
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(finales.size) { indice ->
                        val esActual = pagerState.currentPage == indice
                        Box(
                            modifier =
                                Modifier
                                    .size(if (esActual) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (esActual) Color(0xFF1976D2) else Color.White.copy(alpha = 0.6f),
                                    ),
                        )
                    }
                }
            }
        }
    }
}

private fun movimientoReducido(context: Context): Boolean =
    try {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    } catch (_: Exception) {
        false
    }

@Preview(showBackground = true, name = "Carrusel: dos finales")
@Composable
private fun CarruselFinalesFelicesPreview() {
    AndroidTheme {
        CarruselFinalesFelices(
            finales =
                listOf(
                    finalDePreview("Luna"),
                    finalDePreview("Canela"),
                ),
            modalAbierto = false,
            onFotoClick = {},
        )
    }
}

private fun finalDePreview(nombre: String) =
    FinalFeliz(
        idPerro = nombre,
        nombre = nombre,
        fotoHogarUrl = "",
        historia = "$nombre ya tiene un hogar.",
        fechaAdopcion = "2026-03-01",
        estado = "Adoptado",
        publicado = true,
    )