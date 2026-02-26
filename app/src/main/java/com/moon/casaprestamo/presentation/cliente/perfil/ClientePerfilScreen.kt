package com.moon.casaprestamo.presentation.cliente.perfil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ClientePerfilScreen(
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ClientePerfilContent(
        uiState = uiState,
        onEditarClick = { /* TODO: Implementar edición */ }
    )
}