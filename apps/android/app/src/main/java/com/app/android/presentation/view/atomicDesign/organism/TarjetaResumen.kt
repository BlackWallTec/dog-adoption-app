package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.DatoResumen
import com.app.android.presentation.view.atomicDesign.atomic.DocumentoItem
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh

// DetailBox: Organismo para el despliegue de información.

// 1. El ORGANISMO: tarjeta con titulo, boton "Editar" y un espacio para el contenido
@Composable
fun TarjetaResumen(
    titulo: String,
    alEditar: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable ColumnScope.() -> Unit, // el SLOT: aquí va lo de adentro
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(color = SurfaceContainerHigh, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), // espacio enttre los datos
    ) {
        // Header: Titulo a la izq, editar a la derecha
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titulo,
                color = InverseSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.weight(1f)) // Empuja el editar hasta la derecha

            Text(
                text = "Editar",
                color = InverseSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = alEditar),
            )
        }

        // Se dibuja la info mandada aca
        contenido()
    }
}

// 2. PREVISUALIZACIONES: la misma tarjeta con contenidos distintos
@Preview(showBackground = true, name = "Tarjeta con datos")
@Composable
fun TarjetaResumenDatosPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
    ) {
        TarjetaResumen(
            titulo = "Datos generales",
            alEditar = {},
        ) {
            DatoResumen(etiqueta = "Nombre completo", valor = "Sofía García López")
            DatoResumen(etiqueta = "Edad", valor = "31 años")
            DatoResumen(etiqueta = "Ocupación", valor = null)
            DatoResumen(etiqueta = "Domicilio", valor = "Epigmenio Gonzales 19284900 pipiopi")
            DatoResumen(etiqueta = "Delegación / Municipio", valor = "Queretaro")
            DatoResumen(etiqueta = "Teléfono", valor = "442 433 9258")
            DatoResumen(etiqueta = "Correo", valor = "sofia.garcia@gmail.com")
        }
    }
}

@Preview(showBackground = true, name = "Tarjeta con datos de hogar")
@Composable
fun TarjetaResumenHogarPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
    ) {
        TarjetaResumen(
            titulo = "Tu Hogar",
            alEditar = {},
        ) {
            DatoResumen(etiqueta = "¿Has tenido mascotas antes?", valor = "Si")
            DatoResumen(
                etiqueta = "¿Tienes otros animales en casa?",
                valor = "Mia, gata de 3 años, esteralizada y vacunada. Tranquila; Convive con perros",
            )
            DatoResumen(
                etiqueta = "¿Por qué deseas adoptar?",
                valor = "Queremos darle a Bruno un hogar lleno de cariño. Tenemos tiempo para pasearlo cada día y acompañarlo en su adaptación.",
            )
            DatoResumen(
                etiqueta = "¿Dondé vivirá y dormira el perrito?",
                valor = "Casa con patio cerrado y espacios seguros para Bruno. Lugar para dormir: pendiente.",
            )
            DatoResumen(etiqueta = "¿Cuantas personas viven contigo?", valor = "3 adultos")
            DatoResumen(etiqueta = "¿Todos estan de acuerdo?", valor = "Si, todos estamso de acuerod en adoptar")
            DatoResumen(etiqueta = "Haus o depa", valor = "Casa")
            DatoResumen(etiqueta = "¿Tienes jardin o Patio?", valor = "Patio cerrado")
            DatoResumen(etiqueta = "Si rentas ¿Tu landlord acepta mascotas?", valor = null)
        }
    }
}

@Preview(showBackground = true, name = "Tarjeta con Compromiso")
@Composable
fun TarjetaResumenCompromisoPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
    ) {
        TarjetaResumen(
            titulo = "Compromiso",
            alEditar = {},
        ) {
            DatoResumen(etiqueta = "Menciona los cuidados basicos de una mascota", valor = null)
            DatoResumen(etiqueta = "¿Que opinas de la esterilización?", valor = null)
            DatoResumen(etiqueta = "¿Nos perimtes dar seguimiento (visitas o fotos)?", valor = null)
        }
    }
}

@Preview(showBackground = true, name = "Tarjeta con documentos")
@Composable
fun TarjetaResumenDocumentosPreview() {
    Column(
        modifier =
            Modifier
                .background(Surface)
                .padding(16.dp),
    ) {
        TarjetaResumen(
            titulo = "Documentos",
            alEditar = {},
        ) {
            DocumentoItem(nombreArchivo = "INE_Sofia_Garcia.pdf")
            DocumentoItem(nombreArchivo = "Domicilio_Sofia_Garcia.pdf")
        }
    }
}
