package com.app.android.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme

private const val DISABLED_CONTAINER_ALPHA = 0.4f
private const val DISABLED_CONTENT_ALPHA = 0.8f

enum class ButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Clear,
}

// Botón base para todos los variantes
@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary,
    isLoading: Boolean = false,
    loadingDescription: String = "Cargando",
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(48.dp)
    val buttonModifier = modifier.heightIn(min = 50.dp)
    val isEnabled = enabled && !isLoading

    when (variant) {
        ButtonVariant.Primary -> {
            // Amarillo primario para confirmar/adoptar
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = shape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.inverseSurface,
                        disabledContainerColor = colors.primary.copy(alpha = DISABLED_CONTAINER_ALPHA),
                        disabledContentColor = colors.inverseSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
                    ),
            ) {
                ButtonContent(text, isLoading, loadingDescription)
            }
        }

        ButtonVariant.Secondary -> {
            // Secundario azul claro
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = shape,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = colors.secondaryContainer,
                        contentColor = colors.secondary,
                        disabledContainerColor = colors.secondaryContainer.copy(alpha = DISABLED_CONTAINER_ALPHA),
                        disabledContentColor = colors.secondary.copy(alpha = DISABLED_CONTENT_ALPHA),
                    ),
            ) {
                ButtonContent(text, isLoading, loadingDescription)
            }
        }

        ButtonVariant.Outlined -> {
            // Con outline para filtros, etc
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = shape,
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = colors.surface,
                        contentColor = colors.secondary,
                        disabledContainerColor = colors.surface,
                        disabledContentColor = colors.secondary.copy(alpha = DISABLED_CONTENT_ALPHA),
                    ),
                border =
                    BorderStroke(
                        width = 1.dp,
                        color =
                            if (isEnabled) {
                                colors.inverseSurface
                            } else {
                                colors.inverseSurface.copy(alpha = DISABLED_CONTAINER_ALPHA)
                            },
                    ),
            ) {
                ButtonContent(text, isLoading, loadingDescription)
            }
        }

        ButtonVariant.Clear -> {
            // Transparente con solo texto azul claro
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled,
                shape = shape,
                colors =
                    ButtonDefaults.textButtonColors(
                        contentColor = colors.secondary,
                        disabledContentColor = colors.secondary.copy(alpha = DISABLED_CONTENT_ALPHA),
                    ),
            ) {
                ButtonContent(text, isLoading, loadingDescription)
            }
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    isLoading: Boolean,
    loadingDescription: String,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier.semantics {
                if (isLoading) stateDescription = loadingDescription
            },
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.alpha(if (isLoading) 0f else 1f),
        )
        if (isLoading) {
            CircularProgressIndicator(
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(showBackground = true, name = "App Button Variants")
@Composable
private fun AppButtonVariantsPreview() {
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
            AppButton(text = "Deshabilitado (Primary)", enabled = false, variant = ButtonVariant.Primary, onClick = {})
            AppButton(text = "Deshabilitado (Outlined)", enabled = false, variant = ButtonVariant.Outlined, onClick = {})
        }
    }
}
