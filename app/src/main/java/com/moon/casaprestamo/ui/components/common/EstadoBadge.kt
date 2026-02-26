package com.moon.casaprestamo.ui.components.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun EstadoBadge(estado: String) {
    val color = when (estado) {
        "PAGADO" -> Color(0xFF2E7D32)
        "ATRASADO" -> Color(0xFFC62828)
        else -> Color(0xFFF9A825)
    }

    AssistChip(
        onClick = {},
        label = { Text(estado) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = color.copy(alpha = 0.15f),
            labelColor = color
        )
    )
}
