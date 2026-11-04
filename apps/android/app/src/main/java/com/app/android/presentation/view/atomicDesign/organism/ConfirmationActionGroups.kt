package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.AppButton
import com.app.android.presentation.view.atomicDesign.atomic.ButtonVariant
import com.app.android.presentation.view.ui.theme.AndroidTheme

@Composable
fun ConfirmationActionsGroup(
    onVerMisProgramasClick: () -> Unit,
    onExplorarProgramasClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppButton(
            text = "Ver mis programas",
            onClick = onVerMisProgramasClick,
            variant = ButtonVariant.Primary,
            modifier = Modifier.fillMaxWidth(),
        )
        AppButton(
            text = "Explorar programas",
            onClick = onExplorarProgramasClick,
            variant = ButtonVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmationActionsGroupPreview() {
    AndroidTheme {
        ConfirmationActionsGroup(
            onVerMisProgramasClick = {},
            onExplorarProgramasClick = {},
        )
    }
}
