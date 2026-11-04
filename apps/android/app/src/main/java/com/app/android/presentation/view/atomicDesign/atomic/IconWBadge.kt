package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. EL ÁTOMO: badge con ícono
@Composable
fun IconWithBadge(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Notifications,
    number: Int = 0,
    textSize: TextUnit = 12.sp,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    val buttonModifier = modifier.heightIn(min = 48.dp).widthIn(min = 48.dp)

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonModifier
    ) {
        BadgedBox(
            badge = {
                if (number>0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ) {
                        Text(
                            fontSize = textSize,
                            text = if (number>9) "9+" else number.toString()
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }
    }
}

// 2. PREVISUALIZACIÓN: vacía, a la mitad y llena
@Preview
@Composable
fun IconWithBadgePreview() {
    Column(
        modifier = Modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconWithBadge(number = 24)
        IconWithBadge(number = 6)
        IconWithBadge(number = 4)
        IconWithBadge(number = 0)
    }
}