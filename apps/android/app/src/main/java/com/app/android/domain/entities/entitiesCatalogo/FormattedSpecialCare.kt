package com.app.android.domain.entities.entitiesCatalogo

data class FormattedSpecialCare(
    val hasContent: Boolean,
    val paragraphs: List<CareParagraph>,
)

data class CareParagraph(
    val text: String,
    val extractedItems: List<String> = emptyList(), // Ej: ["Juguetes", "Comida", "Una pelota"]
    val isUrgentOrAlert: Boolean = false, // Ej: true si detecta "miedo", "truenos", "alergia", "medicamento"
)
