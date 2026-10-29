package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.R
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.DatoResumen
import com.app.android.presentation.view.atomicDesign.atomic.DocumentoItem
import com.app.android.presentation.view.atomicDesign.molecule.IconLabel
import com.app.android.presentation.view.atomicDesign.molecule.TarjetaMascota
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.atomicDesign.organism.TarjetaResumen
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

@Suppress("ktlint:standard:function-naming")
@Composable
fun ConfirmarScreen(
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 3, // 3 = "Solicitudes
    onNavSelect: (Int) -> Unit = {},
) {
    // Scaffold = el "esqueleto" de una pantalla: arriba, abajo
    // y contenido.
    Scaffold(
        modifier = modifier,
        containerColor = Surface, // fondo de toda la pantalla
        topBar = {
            AppTopBar(
                titulo = "Solicitud de adopción",
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            NavBar(
                selected = navSeleccionado,
                onSelect = onNavSelect,
            )
        },
    ) { paddingInterno ->
        // paddingInterno = el espacio que ocupan las barras, para que nada quede tapado
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState()) // permite deslizar si no cabe
                    .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Confirmar",
                color = InverseSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = "Revisa tus datos antes de enviar. ¡Estás a un paso de conocer a Bruno.",
                color = InverseSurface,
                fontSize = 16.sp,
            )
            TarjetaMascota(
                foto = painterResource(id = R.drawable.perro_prueba),
                nombre = "Bruno",
                edad = "2 años",
                sexo = "Macho",
                tamano = "Mediano",
                raza = "Mestizo",
                ciudad = "Querétaro",
            )
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
            TarjetaResumen(
                titulo = "Tu Hogar",
                alEditar = {},
            ) {
                DatoResumen(etiqueta = "¿Has tenido mascotas antes?", valor = "Si")
                DatoResumen(etiqueta = "¿Tienes otros animales en casa?", valor = "Mia, gata de 3 años, esteralizada y vacunada. Tranquila; Convive con perros")
                DatoResumen(etiqueta = "¿Por qué deseas adoptar?", valor = "Queremos darle a Bruno un hogar lleno de cariño. Tenemos tiempo para pasearlo cada día y acompañarlo en su adaptación.")
                DatoResumen(etiqueta = "¿Dondé vivirá y dormira el perrito?", valor = "Casa con patio cerrado y espacios seguros para Bruno. Lugar para dormir: pendiente.")
                DatoResumen(etiqueta = "¿Cuantas personas viven contigo?", valor = "3 adultos")
                DatoResumen(etiqueta = "¿Todos estan de acuerdo?", valor = "Si, todos estamso de acuerdo en adoptar")
                DatoResumen(etiqueta = "Haus o depa", valor = "Casa")
                DatoResumen(etiqueta = "¿Tienes jardin o Patio?", valor = "Patio cerrado")
            }
            TarjetaResumen(
                titulo = "Compromiso",
                alEditar = {},
            ) {
                DatoResumen(etiqueta = "Menciona los cuidados basicos de una mascota", valor = null)
                DatoResumen(etiqueta = "¿Que opinas de la esterilización?", valor = null)
                DatoResumen(etiqueta = "¿Nos perimtes dar seguimiento (visitas o fotos)?", valor = null)
            }
            TarjetaResumen(
                titulo = "Documentos",
                alEditar = {},
            ) {
                DocumentoItem(nombreArchivo = "INE_Sofia_Garcia.pdf")
                DocumentoItem(nombreArchivo = "Domicilio_Sofia_Garcia.pdf")
            }
            IconLabel(
                text = "Aceptaste los requisitos de adopción y el compromiso de cuidado y seguimiento",
                icon = Icons.Outlined.Check,
            )
            AppButton(
                text = "Enviar solicitud",
                onClick = onContinuarClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
            )
        }
    }
}

@Preview
@Composable
fun ConfirmarScreenPreview() {
    AndroidTheme {
        ConfirmarScreen(
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}
