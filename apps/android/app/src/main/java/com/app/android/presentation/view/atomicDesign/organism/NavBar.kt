package com.app.android.presentation.view.atomicDesign.organism

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.presentation.view.atomicDesign.atomic.ButtonNavigation
import com.app.android.presentation.view.ui.theme.SurfaceContainer

data class OptionNav(
    val label: String,
    val icon: ImageVector,
)

val navOptions =
    listOf(
        OptionNav("Catálogo", Icons.Filled.Pets),
        OptionNav("Ayudar", Icons.Outlined.VolunteerActivism),
        OptionNav("Donar", Icons.Outlined.FavoriteBorder),
        OptionNav("Solicitudes", Icons.AutoMirrored.Outlined.Assignment),
        OptionNav("Ajustes", Icons.Outlined.Settings),
    )

@Suppress("ktlint:standard:function-naming")
@Composable
fun NavBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(SurfaceContainer)
                .padding(vertical = 4.dp),
    ) {
        navOptions.forEachIndexed { index, option ->
            ButtonNavigation(
                label = option.label,
                icon = option.icon,
                isSelected = index == selected,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Suppress("ktlint:standard:function-naming")
@Preview(showBackground = true, name = "Navigation Bar")
@Composable
fun NavBarPreview() {
    var selected by remember { mutableIntStateOf(0) }
    NavBar(
        selected = selected,
        onSelect = { selected = it },
    )
}
