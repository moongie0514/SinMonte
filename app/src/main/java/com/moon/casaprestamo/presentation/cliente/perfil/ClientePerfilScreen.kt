package com.moon.casaprestamo.presentation.cliente.perfil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ClientePerfilScreen(
    idCliente: Int,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(idCliente) {
        viewModel.cargarPerfil(idCliente)
    }

    ClientePerfilContent(
        uiState                = uiState,
        onNombreChange         = viewModel::onNombreChange,
        onApellidoPaternoChange = viewModel::onApellidoPaternoChange,
        onApellidoMaternoChange = viewModel::onApellidoMaternoChange,
        onTelefonoChange       = viewModel::onTelefonoChange,
        onDireccionChange      = viewModel::onDireccionChange,
        onGuardar              = viewModel::guardarPerfil,
        onLimpiarMensaje       = viewModel::limpiarMensaje
    )
}