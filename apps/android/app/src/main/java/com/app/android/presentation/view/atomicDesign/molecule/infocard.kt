package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
<<<<<<< HEAD
=======
import androidx.compose.ui.Alignment
>>>>>>> 37eab7179fd3454dcc4d5d18cfd34eadd87444ac
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh

// 1. LA MOLÉCULA: tarjeta con icono, titulo y descripción
@Composable
fun InfoCard(
<<<<<<< HEAD
    titulo: String,
=======
    titulo: String? = null,
>>>>>>> 37eab7179fd3454dcc4d5d18cfd34eadd87444ac
    descripcion: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(16.dp) // esquinas redondas (de material design)

    // Row = icono a la izquierda, textos a la derecha
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = SurfaceContainerHigh, shape = forma)
                .padding(16.dp),
<<<<<<< HEAD
=======
        verticalAlignment = if (titulo.isNullOrBlank()) Alignment.CenterVertically else Alignment.Top,
>>>>>>> 37eab7179fd3454dcc4d5d18cfd34eadd87444ac
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null, // el titulo ya explica
            tint = Secondary,
        )

        Spacer(modifier = Modifier.width(16.dp)) // espacio entre icono y texto

        // Column = titulo arriba, descripción abajo
        Column {
<<<<<<< HEAD
            Text(
                text = titulo,
                color = InverseSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = descripcion,
                color = InverseSurface.copy(alpha = 0.7f), // mismo color pero mas claro
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
=======
            if (!titulo.isNullOrBlank()) {
                Text(
                    text = titulo,
                    color = InverseSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
                Text(
                    text = descripcion,
                    color = InverseSurface.copy(alpha = 0.7f), // mismo color pero mas claro
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = if (titulo.isNullOrBlank()) 0.dp else 6.dp),
                )
>>>>>>> 37eab7179fd3454dcc4d5d18cfd34eadd87444ac
        }
    }
}

// 2. PREVISUALIZACIÓN: las tarjetas como en el diseño de Requisitos
@Preview(showBackground = true, name = "Tarjetas de requisitos")
@Composable
fun InfoCardPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), // espacio entre las tarjetas
    ) {
        InfoCard(
            titulo = "Ser mayor de edad",
            descripcion = "Tener 18 años o más para iniciar el proceso.",
            icono = Icons.Outlined.Person,
        )
        InfoCard(
            titulo = "Identificación oficial",
            descripcion = "INE o pasaporte vigente y legible.",
            icono = Icons.Outlined.Verified,
        )
        InfoCard(
            titulo = "Comprobante de domicilio",
            descripcion = "Recibo reciente, no mayor a 3 meses.",
            icono = Icons.Outlined.Home,
        )
        InfoCard(
            titulo = "Compromiso de cuidado y seguimiento",
            descripcion = "Brindar alimento, atención veterinaria y un hogar seguro; aceptar el seguimiento del refugio.",
            icono = Icons.Outlined.FavoriteBorder,
        )
    }
}
