package com.app.android.presentation.designsystem.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.app.android.presentation.designsystem.theme.QRadius
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme

private val PREVIEW_SIZE = 120.dp

/**
 * Foto con esquinas redondeadas, ContentScale.Crop. model = URL/Uri/Painter/resId (coil3 AsyncImage;
 * si es Painter usa Image). Mientras carga: fondo surfaceMuted.
 *
 * Dueño: P1 (Átomos).
 * Reemplaza a: Multimedia (imagen).
 */
@Composable
fun QPhoto(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = QRadius.medium,
) {
    val photoModifier =
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(QTheme.colors.surfaceMuted)
    if (model is Painter) {
        Image(
            painter = model,
            contentDescription = contentDescription,
            modifier = photoModifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = photoModifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@Preview(showBackground = true, name = "QPhoto")
@Composable
private fun QPhotoPreview() {
    QarinoTheme {
        Row(
            modifier = Modifier.padding(QSpacing.l),
            horizontalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QPhoto(
                model = ColorPainter(QTheme.colors.brandStrong),
                contentDescription = null,
                modifier = Modifier.size(PREVIEW_SIZE),
            )
            QPhoto(model = null, contentDescription = null, modifier = Modifier.size(PREVIEW_SIZE))
        }
    }
}