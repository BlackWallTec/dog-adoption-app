package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
private val ColorSurface = Color(0xFFFFFBF2)
private val ColorTextOnPrimary = Color(0xFF302600)

@Composable
fun FilterActionBar(
    onClear: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    clearText: String = "Limpiar",
    applyText: String = "Aplicar filtros",
    clearEnabled: Boolean = true,
    applyEnabled: Boolean = true
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Limpiar button
        val clearAlpha = if (clearEnabled) 1f else 0.4f
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(shape)
                .background(ColorSurface.copy(alpha = clearAlpha))
                .border(
                    width = 1.5.dp,
                    color = ColorSecondary.copy(alpha = clearAlpha),
                    shape = shape
                )
                .clickable(enabled = clearEnabled, role = Role.Button) { onClear() }
                .semantics { contentDescription = clearText },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = clearText,
                color = ColorSecondary.copy(alpha = clearAlpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Aplicar button
        val applyAlpha = if (applyEnabled) 1f else 0.4f
        Row(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(shape)
                .background(ColorPrimary.copy(alpha = applyAlpha))
                .clickable(enabled = applyEnabled, role = Role.Button) { onApply() }
                .semantics { contentDescription = applyText },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = applyText,
                color = ColorTextOnPrimary.copy(alpha = applyAlpha),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterActionBarPreviewNormal() {
    FilterActionBar(
        onClear = {},
        onApply = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFBF2)
@Composable
fun FilterActionBarPreviewClearDisabled() {
    FilterActionBar(
        onClear = {},
        onApply = {},
        clearEnabled = false
    )
}