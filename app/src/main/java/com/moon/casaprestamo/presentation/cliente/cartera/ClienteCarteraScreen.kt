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
    // ✅ StateFlow requiere collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ✅ Recarga cada vez que la pantalla vuelve a primer plano
    LaunchedEffect(idCliente, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.cargarCartera(idCliente)
        }
    }

    ClienteCarteraContent(
        uiState  = uiState,
        onRetry  = { viewModel.cargarCartera(idCliente) }
    )
}