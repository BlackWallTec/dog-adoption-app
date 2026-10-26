package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import com.app.android.presentation.view.ui.theme.Secondary

@Suppress("ktlint:standard:function-naming")
@Composable
fun CounterIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.focusProperties { canFocus = false },
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) Secondary else Secondary.copy(alpha = 0.38f),
        )
    }
}
