package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminSupervisionScreen(
    idAprobador: Int,
    viewModel: SupervisionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminSupervisionContent(
        uiState = uiState,
        onAprobar = { idPrestamo -> viewModel.aprobarPrestamo(idPrestamo, idAprobador) },
        onRechazar = { idPrestamo -> viewModel.rechazarPrestamo(idPrestamo, idAprobador) },
        modifier = modifier
    )
}