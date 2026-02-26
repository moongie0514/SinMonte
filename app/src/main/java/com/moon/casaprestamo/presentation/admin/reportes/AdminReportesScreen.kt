package com.moon.casaprestamo.presentation.admin.reportes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminReportesScreen(
    viewModel: ReportesViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminReportesContent(
        uiState = uiState,
        modifier = modifier
    )
}