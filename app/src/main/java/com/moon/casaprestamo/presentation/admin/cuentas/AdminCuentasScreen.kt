package com.moon.casaprestamo.presentation.admin.cuentas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminCuentasScreen(
    viewModel: CuentasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AdminCuentasContent(
            uiState = uiState,
            onLoad = viewModel::cargar,
            onCrearEmpleado = viewModel::crearEmpleado,
            onCrearCliente = viewModel::crearCliente,
            onEditarUsuario = viewModel::editarUsuario,
            onToggleEstado = viewModel::cambiarEstado
        )
    }
}
