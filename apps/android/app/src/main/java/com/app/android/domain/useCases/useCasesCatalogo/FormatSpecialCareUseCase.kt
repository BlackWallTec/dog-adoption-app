package com.app.android.domain.useCases.useCasesCatalogo

import com.app.android.domain.entities.entitiesCatalogo.CareParagraph
import com.app.android.domain.entities.entitiesCatalogo.FormattedSpecialCare
import java.text.Normalizer
import javax.inject.Inject

class FormatSpecialCareUseCase
    @Inject
    constructor() {
        operator fun invoke(rawText: String?): FormattedSpecialCare {
            val cleanInput = normalizeInput(rawText)
            if (cleanInput.isEmpty()) {
                return FormattedSpecialCare(hasContent = false, paragraphs = emptyList())
            }

            val paragraphs =
                splitSentences(cleanInput).map { raw ->
                    val sentence = capitalizeFirstLetter(raw)
                    val (displayText, items) = extractListAndCleanText(sentence)
                    CareParagraph(
                        text = displayText,
                        extractedItems = items,
                        isUrgentOrAlert = isAlert(sentence),
                    )
                }

            return FormattedSpecialCare(
                hasContent = paragraphs.isNotEmpty(),
                paragraphs = paragraphs,
            )
        }

        // ---------- 1. Limpieza ----------

        private fun normalizeInput(rawText: String?): String {
            val trimmed = rawText?.trim().orEmpty()
            if (trimmed.isEmpty() || trimmed.equals("null", ignoreCase = true)) return ""
            return trimmed
                .replace(markdownLinkRegex, "\$1") // [texto](url) -> texto
                .replace(spacesRegex, " ")
        }

        // ---------- 2. Separación de oraciones ----------

        private fun splitSentences(text: String): List<String> =
            text
                .lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .flatMap { splitBlock(it) }

        private fun splitBlock(block: String): List<String> {
            val sentences = mutableListOf<String>()
            var start = 0
            for (match in sentenceEndRegex.findAll(block)) {
                if (!isRealBoundary(block, match)) continue
                val end = match.range.last + 1
                sentences.add(block.substring(start, end).trim())
                start = end
            }
            val tail = block.substring(start).trim()
            if (tail.isNotEmpty()) sentences.add(tail)
            return sentences.filter { s -> s.any { it.isLetterOrDigit() } }
        }

        /** Decide si el signo + espacio encontrado realmente termina una oración. */
        private fun isRealBoundary(
            block: String,
            match: MatchResult,
        ): Boolean {
            val terminator = match.groupValues[1]
            val next = block.getOrNull(match.range.last + 1) ?: return true
            val nextContinues = next.isLowerCase() || next.isDigit()

            // "!", "?", "..." o "…": solo continúan si lo siguiente es minúscula
            if (terminator.length > 1 || terminator[0] != '.') return !next.isLowerCase()

            // Punto simple: revisamos la palabra que lo precede
            val rawToken =
                block
                    .substring(0, match.range.first)
                    .takeLastWhile { !it.isWhitespace() }
                    .trim { !it.isLetterOrDigit() }
            if (rawToken.isEmpty()) return true

            val token = rawToken.stripAccents().lowercase()
            return when {
                token in alwaysContinue -> false

                rawToken.length == 1 && rawToken[0].isUpperCase() -> false

                // inicial: "J. López"
                (rawToken.length == 1 && rawToken[0].isLetter()) || token in softAbbreviations -> !nextContinues

                else -> true
            }
        }

        // ---------- 3. Alertas ----------

        private fun isAlert(sentence: String): Boolean = alertRegex.containsMatchIn(sentence.stripAccents())

        // ---------- 4. Listas -> chips ----------

        private fun extractListAndCleanText(sentence: String): Pair<String, List<String>> {
            val plain = sentence to emptyList<String>()
            if (sentence.startsWith("¿") || sentence.endsWith("?")) return plain

            val match = listPatternRegex.find(sentence) ?: return plain
            val listGroup = match.groups[2] ?: return plain

            val items =
                listGroup.value
                    .split(listSeparatorRegex)
                    .map { it.trim().removeSuffix(".") }
                    .filterNot { it.equals("etc", ignoreCase = true) }
            if (items.size < 3 || items.any { it.length !in 2..25 }) return plain

            val head = sentence.substring(0, listGroup.range.first).trimEnd().removeSuffix(",")
            if (!head.contains(' ') && !head.endsWith(":")) return plain

            val cleaned = if (head.endsWith(":")) head else "$head:"
            return cleaned to items.map { capitalizeFirstLetter(it) }
        }

        // ---------- Utilidades ----------

        private fun capitalizeFirstLetter(text: String): String =
            firstLetterRegex.replace(text) { it.groupValues[1] + it.groupValues[2].uppercase() }

        private fun String.stripAccents(): String = Normalizer.normalize(this, Normalizer.Form.NFD).replace(combiningMarksRegex, "")

        private companion object {
            // Piezas del regex de listas (sin grupos de captura)
            const val CONJ = """(?:y|e|o|u|ni)"""
            const val WORD = """(?!$CONJ(?![\p{L}\p{N}]))[\p{L}\p{N}][\p{L}\p{N}\-]*"""
            const val ITEM = """$WORD(?:\s+$WORD)?""" // ítem de 1 a 2 palabras
            const val LEAD_IN =
                """(?:a|de|con|sin|como|tipo|ni|darle|dar|come|comer|tomar|usar|evitar|incluye|incluyen)"""

            val markdownLinkRegex = Regex("""\[([^\]]+)]\([^)]*\)""")
            val spacesRegex = Regex("""[ \t\u00A0]+""")
            val combiningMarksRegex = Regex("""\p{Mn}+""")
            val firstLetterRegex = Regex("""^([^\p{L}\p{N}]*)(\p{Ll})""")

            // Terminador + cierres opcionales (comillas, paréntesis) + espacio
            val sentenceEndRegex = Regex("""([.!?…]+)["”»)']*\s+""")

            // Nunca terminan oración (títulos, referencias, direcciones). Sin acentos.
            val alwaysContinue =
                setOf(
                    "dr",
                    "dra",
                    "sr",
                    "sra",
                    "srta",
                    "lic",
                    "ing",
                    "arq",
                    "prof",
                    "mvz",
                    "m.v.z",
                    "mtro",
                    "mtra",
                    "num",
                    "tel",
                    "av",
                    "blvd",
                    "col",
                    "cd",
                    "esq",
                    "art",
                    "pag",
                    "sec",
                    "cap",
                    "inc",
                    "mod",
                    "fig",
                    "vol",
                    "ref",
                    "ced",
                    "hosp",
                    "vet",
                    "int",
                    "depto",
                    "nte",
                    "dir",
                    "vs",
                )

            // Terminan oración solo si lo siguiente NO es minúscula ni dígito
            val softAbbreviations =
                setOf(
                    "etc",
                    "ej",
                    "sep",
                    "oct",
                    "nov",
                    "dic",
                    "ene",
                    "feb",
                    "abr",
                    "jun",
                    "jul",
                    "ago",
                    "kg",
                    "mg",
                    "ml",
                    "cm",
                    "hr",
                    "hrs",
                    "min",
                    "seg",
                    "aprox",
                    "max",
                    "a.m",
                    "p.m",
                )

            // Se aplica sobre texto SIN acentos
            val alertRegex =
                Regex(
                    pattern =
                        """\b(?:miedo|fobia|asust\w*|trueno\w*|tormenta\w*|pirotecnia|alerg\w*|medicament\w*|""" +
                            """pastilla\w*|sedante\w*|dosis|toxic\w*|veneno\w*|peligro\w*|cuidado|nunca|jamas|""" +
                            """prohib\w*|evit\w*|urgenc\w*|emergenc\w*|fiebre|vomit\w*|convuls\w*|letargo|""" +
                            """diarrea|no\s+(?:darle|dar|administr\w*|mezcl\w*|suelt\w*|exponer))\b""",
                    option = RegexOption.IGNORE_CASE,
                )

            // [ancla: preposición o ":"] [ítem, ítem y/o ítem  |  ítem, ítem, etc.] [fin de oración]
            val listPatternRegex =
                Regex(
                    pattern =
                        """(\b$LEAD_IN\s+|:\s*)""" +
                            """($ITEM(?:\s*,\s*$ITEM)+?(?:,?\s+$CONJ\s+$ITEM|\s*,\s*etc\.?))""" +
                            """\s*[.!…]*\z""",
                    option = RegexOption.IGNORE_CASE,
                )

            val listSeparatorRegex =
                Regex(
                    pattern = """\s*,\s*(?:$CONJ\s+)?|\s+$CONJ\s+""",
                    option = RegexOption.IGNORE_CASE,
                )
        }
    }
