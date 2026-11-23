package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesCatalogo.CareParagraph
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoCuerpo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CareParagraphItem(
    paragraph: CareParagraph,
    modifier: Modifier = Modifier,
) {
    // Paleta de colores según si es una alerta (ej. truenos, alergia) o nota general
    val backgroundColor = if (paragraph.isUrgentOrAlert) Color(0xFFFFF8E1) else Color(0xFFF8F9FA)
    val borderColor = if (paragraph.isUrgentOrAlert) Color(0xFFFFE082) else Color(0xFFE9ECEF)
    val iconTint = if (paragraph.isUrgentOrAlert) Color(0xFFF57C00) else Color(0xFF1976D2)
    val iconVector = if (paragraph.isUrgentOrAlert) Icons.Outlined.Info else Icons.Outlined.FavoriteBorder

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Fila principal: Ícono + Oración formateada
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = iconTint,
                    modifier =
                        Modifier
                            .size(20.dp)
                            .padding(top = 2.dp),
                )
                TextoCuerpo(texto = paragraph.text)
            }

            // Si el UseCase detectó una lista escrita por comas (ej. "Juguetes, Comida, Una pelota")
            if (paragraph.extractedItems.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 30.dp), // Alineado con el texto
                ) {
                    paragraph.extractedItems.forEach { item ->
                        ChipTag(
                            texto = item,
                            colorFondo = if (paragraph.isUrgentOrAlert) Color(0xFFFFECB3) else Color(0xFFE3F2FD),
                            colorTexto = if (paragraph.isUrgentOrAlert) Color(0xFFE65100) else Color(0xFF1565C0),
                        )
                    }
                }
            }
        }
    }
}
