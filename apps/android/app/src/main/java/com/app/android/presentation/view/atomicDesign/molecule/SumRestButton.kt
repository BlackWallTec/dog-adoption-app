package com.app.android.presentation.view.atomicDesign.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.CounterIconButton
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.SurfaceContainerHigh

@Suppress("ktlint:standard:function-naming")
@Composable
fun SumRestButton(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .background(SurfaceContainerHigh, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CounterIconButton(Icons.Default.Remove, "Disminuir", onClick = onDecrease)
        Text(
            text = value.toString(),
            fontWeight = FontWeight.Bold,
            color = InverseSurface,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        CounterIconButton(Icons.Default.Add, "Aumentar", onClick = onIncrease)
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true)
@Composable
private fun SumRestButtonPreview() {
    AndroidTheme {
        SumRestButton(value = 3, onDecrease = {}, onIncrease = {})
    }
}
