package com.app.android.presentation.view.atomicDesign.atomic
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.InverseSurface
import com.app.android.presentation.view.ui.theme.Primary
import com.app.android.presentation.view.ui.theme.Secondary
import com.app.android.presentation.view.ui.theme.SecondaryContainer
import com.app.android.presentation.view.ui.theme.Surface

enum class ButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Clear,
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    isLoading: Boolean = false,
) {
    val shape = RoundedCornerShape(16.dp)
    val heightModifier = modifier.height(50.dp)

    when (variant) {
        ButtonVariant.Primary -> {
            // Amarillo primario para confirmar/adoptar
            Button(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = InverseSurface,
                        disabledContainerColor = Primary.copy(alpha = 0.4f),
                        disabledContentColor = InverseSurface.copy(alpha = 0.8f),
                    ),
            ) {
                ButtonContent(text, isLoading, InverseSurface)
            }
        }

        ButtonVariant.Secondary -> {
            // Secundario azul claro
            Button(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SecondaryContainer,
                        contentColor = Secondary,
                        disabledContainerColor = SecondaryContainer.copy(alpha = 0.4f),
                        disabledContentColor = Secondary.copy(alpha = 0.8f),
                    ),
            ) {
                ButtonContent(text, isLoading, Secondary)
            }
        }

        ButtonVariant.Outlined -> {
            // Con outline para filtros, etc
            OutlinedButton(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Surface,
                        contentColor = Secondary,
                        disabledContentColor = Secondary.copy(alpha = 0.8f),
                    ),
                border =
                    BorderStroke(
                        width = 1.dp,
                        color = if (enabled && !isLoading) InverseSurface else InverseSurface.copy(alpha = 0.4f),
                    ),
            ) {
                ButtonContent(text, isLoading, Secondary)
            }
        }

        ButtonVariant.Clear -> {
            // Transparente con solo texto azul claro
            TextButton(
                onClick = onClick,
                modifier = heightModifier,
                enabled = enabled && !isLoading,
                shape = shape,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = Secondary,
                        disabledContentColor = Secondary.copy(alpha = 0.8f),
                    ),
            ) {
                ButtonContent(text, isLoading, Secondary)
            }
        }
    }
}

// Extracted helper to avoid repeating the loading/text logic 4 times
@Suppress("ktlint:standard:function-naming")
@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    color: Color,
) {
    if (isLoading) {
        CircularProgressIndicator(
            color = color,
            modifier = Modifier.size(24.dp),
        )
    } else {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "App Button Variants")
@Composable
fun AppButtonVariantsPreview() {
    AndroidTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppButton(text = "Adoptar (Primary)", variant = ButtonVariant.Primary, onClick = {})
            AppButton(text = "Ver Perfil (Secondary)", variant = ButtonVariant.Secondary, onClick = {})
            AppButton(text = "Filtros (Outlined)", variant = ButtonVariant.Outlined, onClick = {})
            AppButton(text = "Cancelar (Clear)", variant = ButtonVariant.Clear, onClick = {})
            AppButton(text = "Cargando", isLoading = true, variant = ButtonVariant.Primary, onClick = {})
        }
    }
}
