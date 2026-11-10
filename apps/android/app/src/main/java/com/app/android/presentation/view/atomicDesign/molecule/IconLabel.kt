package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Secondary

@Suppress("ktlint:standard:function-naming")
@Composable
fun IconLabel(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color = Secondary,
    textColor: Color = InverseSurface,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview
@Composable
fun IconLabelPreview() {
    IconLabel(
        text = "Aceptaste los requisitos de adopción y el compromiso de cuidado y seguimiento",
        icon = Icons.Outlined.Check,
    )
}
