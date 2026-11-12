package com.app.android.domain.entities.entitiesCatalogo

const val MAX_FINALES_EN_CARRUSEL = 20 // Este define cuantas historias puede haber a la vez den el carruseñ

private val ESTADOS_ADOPTADO = setOf("adoptado", "adoptada")

// Un final feliez se puede mostra  solo si el refugio lo publicó completo
fun FinalFeliz.esPublicable(): Boolean {
    val estadoNormalizado = estado.trim().lowercase()
    return publicado &&
            estadoNormalizado in ESTADOS_ADOPTADO &&
            idPerro.isNotBlank() &&
            fotoHogarUrl.isNotBlank() &&
            historia.isNotBlank()
}

// Deja solo las diapositivas válidas: sin duplicados, las más recientes primero y con tope
fun List<FinalFeliz>.paraCarrusel(): List<FinalFeliz> =
    asSequence()
        .filter { it.esPublicable() }
        .distinctBy { it.idPerro.trim() }
        .sortedWith(ORDEN_CARRUSEL)
        .take(MAX_FINALES_EN_CARRUSEL)
        .toList()

// Sin fecha van al final
private val ORDEN_CARRUSEL =
    compareBy<FinalFeliz> { it.fechaAdopcion.isNullOrBlank() }
        .thenByDescending { it.fechaAdopcion.orEmpty() }