package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorPrimary = Color(0xFFFDD254)
private val ColorSecondary = Color(0xFF2F6FB5)
private val ColorSecondaryContainer = Color(0xFFD8E8FF)
private val ColorSurface = Color(0xFFFFFBF2)
private val ColorSurfaceContainer = Color(0xFFF1EBDD)
private val ColorTextOnPrimary = Color(0xFF302600)
private val ColorTextOnSecondaryContainer = Color(0xFF12365D)
private val ColorTextMain = Color(0xFF3A3A3A)
private val ColorOutlineVariant = Color(0xFFD2CABB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showCheck: Boolean = false,
    enabled: Boolean = true,
    role: Role = Role.RadioButton
) {
    val backgroundColor = if (selected) ColorSecondaryContainer else ColorSurfaceContainer
    val borderColor = if (selected) ColorSecondary else ColorOutlineVariant
    val textColor = if (selected) ColorTextOnSecondaryContainer else ColorTextMain
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal

    val alpha = if (enabled) 1f else 0.4f

    CompositionLocalProvider(
        LocalMinimumInteractiveComponentEnforcement provides false
    ) {
        Row(
            modifier = modifier
                .height(40.dp)
                .clip(RoundedCornerShape(50))
                .background(backgroundColor.copy(alpha = alpha))
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = alpha),
                    shape = RoundedCornerShape(50)
                )
                .selectable(
                    selected = selected,
                    role = role,
                    enabled = enabled,
                    onClick = onClick
                )
                .semantics {
                    contentDescription = text
                }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showCheck && selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "",
                    tint = textColor.copy(alpha = alpha),
                    modifier = Modifier.height(16.dp)
                )
            }
            Text(
                text = text,
                color = textColor.copy(alpha = alpha),
                fontSize = 14.sp,
                fontWeight = fontWeight
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterButtonPreviewUnselected() {
    FilterButton(
        text = "Chico",
        selected = false,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterButtonPreviewSelected() {
    FilterButton(
        text = "Mediano",
        selected = true,
        onClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterButtonPreviewSelectedWithCheck() {
    FilterButton(
        text = "Convive con gatos",
        selected = true,
        onClick = {},
        showCheck = true
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterButtonPreviewDisabled() {
    FilterButton(
        text = "Grande",
        selected = true,
        onClick = {},
        enabled = false
    )
}
