package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.app.android.presentation.view.ui.theme.AndroidTheme

@Composable
fun FormHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FormHeaderPreview() {
    AndroidTheme {
        FormHeader(
            title = "Formulario",
            subtitle = "Ingresa para continuar",
        )
    }
}
