// Archivo generado para el rediseño Qariño Animal. Ver CONTRATO.md.
package com.app.android.presentation.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Estilos de texto. NUNCA escribas fontSize = ... fuera de este archivo: usa QText(style = ...).
 *
 * Dueño: P1 (Átomos).
 */
enum class QTextStyle {
    TopBarTitle, // 21sp Bold: título de la top bar
    Title, // 18sp Bold: nombres (perro, solicitud), títulos de tarjeta
    SectionTitle, // 17sp Bold: "Detalles de Raza y Tamaño", "Datos bancarios"
    Body, // 15sp Normal: texto normal
    BodyStrong, // 15sp SemiBold: valores, etiquetas de campo
    Button, // 15sp SemiBold: texto de botones
    Label, // 13sp Medium: etiquetas chicas, chips
    Caption, // 12sp Normal: ayudas, nav bar
}

object QTypography {
    fun of(style: QTextStyle): TextStyle =
        when (style) {
            QTextStyle.TopBarTitle -> TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
            QTextStyle.Title -> TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
            QTextStyle.SectionTitle -> TextStyle(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
            QTextStyle.Body -> TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
            QTextStyle.BodyStrong -> TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold)
            QTextStyle.Button -> TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
            QTextStyle.Label -> TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
            QTextStyle.Caption -> TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal)
        }
}
