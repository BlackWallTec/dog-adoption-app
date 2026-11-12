package com.app.android.presentation.view.atomicDesign.organism.basicOrganisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.android.R
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.DetalleSolicitud
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.EstadoTarjeta
import com.app.android.presentation.view.atomicDesign.molecule.basicMolecules.TarjetaSolicitud
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.Surface

// 1. EL ORGANISMO: TarjetaSolicitud + botón "Ver detalles" + DetalleSolicitud
// Stateless: si está expandida o no lo guarda quien lo usa (la page)
@Composable
fun TarjetaSolicitudExpandible(
    foto: Painter,
    nombre: String,
    folio: String,
    fecha: String,
    estado: EstadoTarjeta,
    comentarioAdmin: String?,
    puedeEditar: Boolean,
    expandida: Boolean,
    onToggleDetalles: () -> Unit,
    modifier: Modifier = Modifier,
    onEditar: () -> Unit = {},
) {
    TarjetaSolicitud(
        foto = foto,
        nombre = nombre,
        folio = folio,
        fecha = fecha,
        estado = estado,
        puedeEditar = puedeEditar,
        onEditar = onEditar,
        modifier = modifier,
    ) {
        // Esto entra en el "pie" de la tarjeta
        TextButton(
            onClick = onToggleDetalles,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.textButtonColors(contentColor = Secondary),
        ) {
            Text(text = if (expandida) "Ocultar detalles" else "Ver detalles", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (expandida) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
        }

        AnimatedVisibility(visible = expandida) {
            DetalleSolicitud(
                mensaje = estado.mensaje,
                comentarioAdmin = comentarioAdmin,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// 2. PREVISUALIZACION: todos los estados (el de documento rechazado, abierto)
@Preview(showBackground = true, name = "Tarjeta solicitud expandible")
@Composable
private fun TarjetaSolicitudExpandiblePreview() {
    AndroidTheme {
        Column(
            modifier = Modifier.background(Surface).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EstadoTarjeta.entries.forEach { estado ->
                TarjetaSolicitudExpandible(
                    foto = painterResource(R.drawable.perro_prueba),
                    nombre = "Bruno",
                    folio = "QA-0248",
                    fecha = "12 oct 2026",
                    estado = estado,
                    comentarioAdmin =
                        if (estado == EstadoTarjeta.DOCUMENTO_RECHAZADO) {
                            "La foto de tu INE sale borrosa, súbela de nuevo por favor."
                        } else {
                            null
                        },
                    puedeEditar = estado == EstadoTarjeta.DOCUMENTO_RECHAZADO,
                    expandida = estado == EstadoTarjeta.DOCUMENTO_RECHAZADO,
                    onToggleDetalles = {},
                )
            }
        }
    }
}