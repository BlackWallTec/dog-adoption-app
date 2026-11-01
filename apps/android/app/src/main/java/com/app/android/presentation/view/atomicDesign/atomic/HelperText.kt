package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelperText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6B655A)
) {
    Text(
        text = text,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = color,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun HelperTextPreview() {
    HelperText(
        text = "PDF, JPG o PNG · Máximo 5 MB por archivo. Asegúrate de que los datos sean legibles.",
        modifier = Modifier.padding(16.dp)
    )
}