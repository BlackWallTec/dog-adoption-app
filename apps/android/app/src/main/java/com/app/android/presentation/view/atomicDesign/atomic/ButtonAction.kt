package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.Primary
import com.app.android.presentation.view.ui.theme.PrimaryContainer

@Suppress("ktlint:standard:function-naming")
@Composable
fun WelcomeCard(
    name: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(16.dp).background(PrimaryContainer),
    ) {
        Text(text = "¡Hola, $name!")
        Text(text = "Bienvenido a Jetpack Compose.")
        Button(
            onClick = onClick,
            modifier = Modifier.padding(top = 8.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    contentColor = Color.Black,
                ),
        ) {
            Text("Continuar")
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "Welcome Card View")
@Composable
fun WelcomeCardPreview() {
    WelcomeCard(name = "Carlos", onClick = {})
}