package com.moon.casaprestamo.presentation.cliente.prestamo

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SolicitarPrestamoScreen(
    idCliente: Int,
    viewModel: SolicitarPrestamoViewModel = hiltViewModel()
) {
    // ✅ mutableStateOf — no necesita collectAsState()
    val uiState = viewModel.uiState

    SolicitarPrestamoContent(
        state         = uiState,
        onMontoChange = viewModel::onMontoChange,
        onPlazoChange = viewModel::onPlazoChange,   // ✅ antes: onMesesChange
        onEnviar      = { viewModel.enviarSolicitud(idCliente) },
        onBack        = {}
    )
}