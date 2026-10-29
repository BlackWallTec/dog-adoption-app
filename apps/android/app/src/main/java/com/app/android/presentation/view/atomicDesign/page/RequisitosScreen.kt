package com.app.android.presentation.view.atomicDesign.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.molecule.ImportantInfoCard
import com.app.android.presentation.view.atomicDesign.molecule.InfoCard
import com.app.android.presentation.view.atomicDesign.molecule.LabeledCheckBox
import com.app.android.presentation.view.atomicDesign.organism.AppTopBar
import com.app.android.presentation.view.atomicDesign.organism.NavBar
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Surface

// PAGE: Paso 1 de la solicitud de adopción
@Composable
fun RequisitosScreen(
    onBackClick: () -> Unit,
    onContinuarClick: () -> Unit,
    modifier: Modifier = Modifier,
    navSeleccionado: Int = 3, // 3 = "Solicitudes"
    onNavSelect: (Int) -> Unit = {},
) {
    // Scaffold = el "esqueleto" de una pantalla: arriba, abajo y contenido
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
                text = "Requisitos",
                color = InverseSurface,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = "Adoptar es un compromiso para toda la vida. Antes de seguir, revisa lo que necesitas.",
                color = InverseSurface,
                fontSize = 16.sp,
            )

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

            ImportantInfoCard(
                titulo = "IMPORTANTE: La esterilización es requisito.",
                descripcion = "Los adultos se entregan esterilizados; si es cachorro se firma una carta compromiso para esterilizarlo a los 4 meses.",
            )

            LabeledCheckBox(
                text = "He leído y acepto los requisitos y las responsabilidades de una adopción responsable",
                checked = false,
                onCheckedChange = {},
            )

            AppButton(
                text = "Continuar",
                onClick = onContinuarClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "Paso 1 - Requisitos")
@Composable
fun RequisitosScreenPreview() {
    AndroidTheme {
        RequisitosScreen(
            onBackClick = {},
            onContinuarClick = {},
        )
    }
}
