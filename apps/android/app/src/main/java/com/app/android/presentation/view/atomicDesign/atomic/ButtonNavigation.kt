package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Primary

@Suppress("ktlint:standard:function-naming")
@Composable
fun ButtonNavigation(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(width = 64.dp, height = 32.dp)
                    .background(
                        color = if (isSelected) Primary else Color.Transparent,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = InverseSurface,
            )
        }

        Text(
            text = label,
            color = InverseSurface,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "Active Button")
@Composable
fun ButtonNavigationActivePreview() {
    ButtonNavigation(
        label = "Catálogo",
        icon = Icons.Filled.Pets,
        isSelected = true,
        onClick = {},
    )
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "Inactive Button")
@Composable
fun ButtonNavigationInactivePreview() {
    ButtonNavigation(
        label = "Ajustes",
        icon = Icons.Outlined.Settings,
        isSelected = false,
        onClick = {},
    )
}