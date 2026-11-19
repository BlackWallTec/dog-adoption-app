package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.ReadOnlyGrey
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh
import com.app.android.presentation.view.ui.theme.SurfaceContainerLow

// Átomo: tarjeta de escritura
// No guarda el texto, lo recibe y avisa si cambia el string
// Esto se utilizara luego para conectarlo en el viewmodel
@Suppress("ktlint:standard:function-naming")
@Composable
fun WritableCard(
    value: String,
    onValueChange: (String) -> Unit, // avisa si cambio el texto
    placeholder: String, // texto que se va a borrar cuando empiezas a escribir
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null, // El icono es opcional ?
    singleLine: Boolean = false, // true se usara para campos de una linea (ex. edad)
    minLines: Int = 1, // que tan alta empieza la tarjeta
    readOnly: Boolean = false, // true = se ve pero no se puede escribir
    maxLength: Int? = null,
) {
    // Style to grey out read only fields
    val containerColor = if (readOnly) ReadOnlyGrey else SurfaceContainerLow
    val textAlpha = if (readOnly) 0.6f else 1.0f
    val iconTint = if (readOnly) Secondary.copy(alpha = 0.5f) else Secondary
    TextField(
        value = value,
        onValueChange = { input ->
            if (!readOnly) {
                val capped = if (maxLength != null) input.take(maxLength) else input
                onValueChange(capped)
            }
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon =
            if (leadingIcon != null) {
                { Icon(imageVector = leadingIcon, contentDescription = null, tint = Secondary) }
            } else {
                null
            },
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly,
        shape = RoundedCornerShape(16.dp), // esquinas iguales que la infocard
        // Los colores se dividen por los diferentes estados de escritura
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = containerColor, // fondo cuando escribes
                unfocusedContainerColor = containerColor, // fondo en reposo
                disabledContainerColor = containerColor,
                focusedTextColor = InverseSurface.copy(alpha = textAlpha),
                unfocusedTextColor = InverseSurface.copy(alpha = textAlpha),
                disabledTextColor = InverseSurface.copy(alpha = textAlpha),
                focusedIndicatorColor = Color.Transparent, // la linea de abajo escribiendo: invisible
                unfocusedIndicatorColor = Color.Transparent, // linea de abajo en reposo: invisible
                disabledIndicatorColor = Color.Transparent, // linea de abajo deshabilitado: invisible
                cursorColor = InverseSurface, // color del cursor
            ),
    )
}

// Preview: utilizó un mutablestate para recibir los datos en la preview
// pero la implementación final sera en viewmodel

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "WritableCard")
@Composable
private fun WritableCardPreview() {
    AndroidTheme {
        var ocupacion by remember { mutableStateOf("") }
        var nombre by remember { mutableStateOf("Sofia Garcia López") }
        var motivo by remember { mutableStateOf("") }

        Column(
            modifier =
                Modifier
                    .background(Surface)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // espacio entre tarjetas
        ) {
            // 1. Con icono y una sola linea (datos generales
            WritableCard(
                value = ocupacion,
                onValueChange = { ocupacion = it }, // "it" es lo que el usuario acaba de escribir
                placeholder = "Escribe tu ocupación",
                leadingIcon = Icons.Outlined.Person,
                singleLine = true,
                readOnly = true,
            )

            // 2. Sin icono y más alta (como las preguntas largas)
            WritableCard(
                value = motivo,
                onValueChange = { motivo = it },
                placeholder = "Cuéntanos tus motivos",
                minLines = 3,
            )
        }
    }
}
