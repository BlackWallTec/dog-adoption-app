package com.app.android.presentation.view.atomicDesign.atomic

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.ui.theme.AndroidTheme
import com.app.android.presentation.view.ui.theme.Secondary

@Composable
fun SeleccionUnicaButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val interactiveModifier =
        if (onClick != null) {
            Modifier.selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
        } else {
            Modifier
        }

    val dotSize by animateDpAsState(
        targetValue = if (selected) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "DotAnimation",
    )

    val color = if (enabled) Secondary else Secondary.copy(alpha = 0.38f)

    Box(
        modifier =
            modifier
                .size(48.dp)
                .then(interactiveModifier),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .border(
                        width = 2.dp,
                        color = color,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (dotSize > 0.dp) {
                Box(
                    modifier =
                        Modifier
                            .size(dotSize)
                            .clip(CircleShape)
                            .background(color),
                )
            }
        }
    }
}

@Composable
fun StatefulSeleccionUnicaButton(
    modifier: Modifier = Modifier,
    initialSelected: Boolean = false,
    onSelectionChange: ((Boolean) -> Unit)? = null,
) {
    var isSelected by remember { mutableStateOf(initialSelected) }

    SeleccionUnicaButton(
        selected = isSelected,
        onClick = {
            isSelected = !isSelected
            onSelectionChange?.invoke(isSelected)
        },
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "Selección Única Interactive Preview")
@Composable
private fun SeleccionUnicaButtonPreview() {
    var selectedOption by remember { mutableIntStateOf(1) }

    AndroidTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Grupo de Selección (Stateless):")
            Row(verticalAlignment = Alignment.CenterVertically) {
                SeleccionUnicaButton(
                    selected = selectedOption == 1,
                    onClick = { selectedOption = 1 },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Opción 1")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                SeleccionUnicaButton(
                    selected = selectedOption == 2,
                    onClick = { selectedOption = 2 },
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Opción 2")
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text("Botón Independiente (Stateful):")
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatefulSeleccionUnicaButton(initialSelected = false)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tocar para alternar")
            }
        }
    }
}