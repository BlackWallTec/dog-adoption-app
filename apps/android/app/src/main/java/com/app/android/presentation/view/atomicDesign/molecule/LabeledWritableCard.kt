package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.WritableCard
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

/*
Molecule:
- Título arriba
- WritableCard
- Notas
 */
@Suppress("ktlint:standard:function-naming")
@Composable
fun LabeledWritableCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit, // avisa si cambio el texto
    placeholder: String, // texto que se va a borrar cuando empiezas a escribir
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null, // El icono es opcional ?
    helperText: String? = null, // texto auxiliar | si es null no se muestra
    singleLine: Boolean = false, // true se usara para campos de una linea (ex. edad)
    minLines: Int = 1, // que tan alta empieza la tarjeta
) {
    // Column = título arriba, tarjeta en medio, ayuda abajo
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = InverseSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp)) // espacio entre título y tarjeta

        WritableCard(
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            singleLine = singleLine,
            minLines = minLines,
        )

        // texto de ayuda aparece si se habilita
        if (helperText != null) {
            Text(
                text = helperText,
                color = InverseSurface.copy(alpha = 0.7f), // 70% de opacidad
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// Preview: utiliza un mutablestate para recibir los datos en la preview

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "LabeledWrittableCard")
@Composable
private fun LabeledWritableCardPreview() {
    AndroidTheme {
        var ocupacion by remember { mutableStateOf("") }
        var motivo by remember { mutableStateOf("") }

        Column(
            modifier =
                Modifier
                    .background(Surface)
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), // espacio entre tarjetas
        ) {
            LabeledWritableCard(
                label = "Ocupación",
                value = ocupacion,
                onValueChange = { ocupacion = it }, // "it" es lo que el usuario acaba de escribir
                placeholder = "Escribe tu ocupación",
                leadingIcon = Icons.Outlined.Person,
                helperText = "Este dato no está guardado en tu perfil.",
                singleLine = true,
            )
            LabeledWritableCard(
                label = "¿Por qué deseas adoptar?",
                value = motivo,
                onValueChange = { motivo = it },
                placeholder = "Cuéntanos tus motivos",
                minLines = 3, // respuesta larga | más lineas
            )
        }
    }
}
