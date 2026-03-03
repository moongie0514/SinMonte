package com.moon.casaprestamo.presentation.cliente.cartera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun ClienteCarteraScreen(
    idCliente: Int,
    viewModel: ClienteCarteraViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(idCliente, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.cargarCartera(idCliente)
        }
    }

    ClienteCarteraContent(
        uiState  = uiState,
        onRetry  = { viewModel.cargarCartera(idCliente) },
        onPagar  = { idPago -> viewModel.registrarPago(idPago) },          // ← NUEVO
        onLimpiarMensaje = viewModel::limpiarMensajePago                   // ← NUEVO
    )
}