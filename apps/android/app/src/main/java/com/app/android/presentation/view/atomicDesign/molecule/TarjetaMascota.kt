package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.R
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh

// 1. LA MOLECULA: foto de la mascota + sus datos básicos
@Composable
fun TarjetaMascota(
    foto: Painter, // La imagen ya lista para composearse o dibujarse o aja
    nombre: String,
    edad: String,
    sexo: String,
    tamano: String,
    raza: String,
    ciudad: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = SurfaceContainerHigh, shape = RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
    ) {
        Image (
            painter = foto,
            contentDescription = "Foto de $nombre",
            contentScale = ContentScale.Crop, // Recorta la foto para llenar el cuadrado sin deformarla
            modifier =
                Modifier
                    .size(width = 64.dp, height = 72.dp)
                    .clip(RoundedCornerShape(12.dp)) // Esquinas redondas para la pic
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = nombre,
                color = InverseSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "$edad · $sexo · $tamano", // une los textos con puntitos
                color = InverseSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "$raza · $ciudad",
                color = InverseSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

// 2. PREVISUALIZACIÓN
@Preview(showBackground = true, name = "Tarjeta mascota")
@Composable
fun TarjetaMascotaPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
    ) {
        TarjetaMascota(
            foto = painterResource(id = R.drawable.perro_prueba),
            nombre = "Bruno",
            edad = "2 años",
            sexo = "Macho",
            tamano = "Mediano",
            raza = "Mestizo",
            ciudad = "Querétaro",
        )
    }
}