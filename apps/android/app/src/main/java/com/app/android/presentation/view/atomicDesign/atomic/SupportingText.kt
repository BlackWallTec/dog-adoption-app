package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.view.ui.theme.AndroidTheme

@Composable
fun SupportingText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun SupportingTextPreview() {
    AndroidTheme {
        SupportingText(text = "Usaremos este correo para tu solicitud.")
    }
}
