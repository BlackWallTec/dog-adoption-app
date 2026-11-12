package com.app.android.presentation.view.atomicDesign.molecule.basicMolecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.basicAtomics.TextoCuerpo
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerLow

// 1. LA MOLECULA: lo que se despliega en la tarjeta al darle "Ver detalles"
// mensaje = texto fijo que explica el estado (viene de EstadoTarjeta)
// comentarioAdmin = lo que escribió la administradora (null o vacío = sin comentarios)
@Composable
fun DetalleSolicitud(
    mensaje: String,
    comentarioAdmin: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = Surface, shape = RoundedCornerShape(12.dp))
                .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextoCuerpo(texto = mensaje, color = InverseSurface, fontSize = 15.sp)

        // El título resalta (negritas) y el comentario queda en gris, para que se note quién lo dice
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            TextoCuerpo(
                texto = "Comentarios del administrador",
                color = InverseSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            TextoCuerpo(
                texto = comentarioAdmin?.takeIf { it.isNotBlank() } ?: "Sin comentarios por ahora.",
                color = InverseSurface.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
        }
    }
}

// 2. PREVISUALIZACION: sin comentarios y con comentarios
@Preview(showBackground = true, name = "Detalle solicitud")
@Composable
private fun DetalleSolicitudPreview() {
    AndroidTheme {
        Column(
            modifier = Modifier.background(SurfaceContainerLow).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetalleSolicitud(
                mensaje = EstadoTarjeta.EN_REVISION.mensaje,
                comentarioAdmin = null,
            )
            DetalleSolicitud(
                mensaje = EstadoTarjeta.DOCUMENTO_RECHAZADO.mensaje,
                comentarioAdmin = "La foto de tu INE sale borrosa, súbela de nuevo por favor.",
            )
        }
    }
}