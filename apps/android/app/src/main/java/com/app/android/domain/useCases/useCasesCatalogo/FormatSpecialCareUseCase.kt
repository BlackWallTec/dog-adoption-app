package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.CareParagraph
import com.app.android.domain.entities.entitiesCatalogo.FormattedSpecialCare
import javax.inject.Inject

class FormatSpecialCareUseCase
    @Inject
    constructor() {
        private val alertKeywords =
            listOf(
                "miedo",
                "asusta",
                "truenos",
                "tormenta",
                "pirotecnia",
                "alergia",
                "alérgico",
                "medicamento",
                "pastilla",
                "cuidado",
                "peligro",
                "nunca",
                "prohibido",
                "evitar",
                "no darle",
            )

        // Regex mejorado: captura solo las últimas palabras antes de las comas y después de "y/o"
        // Evita tragarse el verbo inicial ("Se recomienda dieta de...")
        private val listPatternRegex =
            Regex(
                pattern = """(?:\b(?:de|con|darle|comer|como|son|usar)\s+)?([a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]+(?:\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]+){0,2}(?:,\s*[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]+(?:\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]+){0,2})+\s+(?:y|o)\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]+(?:\s+[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9]+){0,2})""",
                option = RegexOption.IGNORE_CASE,
            )

        operator fun invoke(rawText: String?): FormattedSpecialCare {
            // 1. BLINDAJE CONTRA NULL, VACÍOS O LA CADENA LITERAL "null"
            val cleanInput = rawText?.trim()
            if (cleanInput.isNullOrEmpty() || cleanInput.equals("null", ignoreCase = true)) {
                return FormattedSpecialCare(hasContent = false, paragraphs = emptyList())
            }

            // 2. SEPARAR ORACIONES POR PUNTO, SALTO DE LÍNEA O SIGNOS DE EXCLAMACIÓN (¡...!)
            val rawSentences =
                cleanInput
                    .split(Regex("""(?<=[.!])\s+|\s*(?=¡)|\n+"""))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && it != "." }

            val formattedParagraphs =
                rawSentences.map { sentence ->
                    val capitalized =
                        sentence.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }

                    // Detecta alerta (ahora también detecta "¡Nunca darle zanahorias!" como alerta)
                    val isAlert =
                        alertKeywords.any { keyword ->
                            capitalized.contains(keyword, ignoreCase = true)
                        }

                    val (displayText, extractedItems) = extractListAndCleanText(capitalized)

                    CareParagraph(
                        text = displayText,
                        extractedItems = extractedItems,
                        isUrgentOrAlert = isAlert,
                    )
                }

            return FormattedSpecialCare(
                hasContent = formattedParagraphs.isNotEmpty(),
                paragraphs = formattedParagraphs,
            )
        }

        private fun extractListAndCleanText(sentence: String): Pair<String, List<String>> {
            val match = listPatternRegex.find(sentence) ?: return Pair(sentence, emptyList())

            // Grupo 1 contiene solo los ítems enumerados (ej: "croquetas, pescado o carne")
            val itemsOnlyString = match.groupValues.getOrNull(1) ?: return Pair(sentence, emptyList())

            val items =
                itemsOnlyString
                    .split(Regex(""",|\s+(?:y|o)\s+""", RegexOption.IGNORE_CASE))
                    .map { it.trim().removeSuffix(".").removeSuffix("!") }
                    .filter { it.length in 2..25 }

            // Si encontramos 3 o más ítems claros, limpiamos la oración para no repetirlos
            return if (items.size >= 3) {
                val formattedChips =
                    items.map { item ->
                        item.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                // Quitamos la lista del párrafo y dejamos el contexto limpio (ej. "Se recomienda dieta de:")
                val cleanedSentence =
                    sentence
                        .replace(itemsOnlyString, "")
                        .trim()
                        .removeSuffix(".")
                        .trimEnd() + ":"

                Pair(cleanedSentence, formattedChips)
            } else {
                Pair(sentence, emptyList())
            }
        }
    }
