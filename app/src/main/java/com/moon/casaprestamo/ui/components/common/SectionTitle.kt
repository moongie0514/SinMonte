package com.moon.casaprestamo.ui.components.common

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall
    )
}
