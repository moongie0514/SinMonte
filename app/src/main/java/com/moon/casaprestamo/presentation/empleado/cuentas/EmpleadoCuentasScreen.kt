package com.moon.casaprestamo.presentation.empleado.cuentas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.moon.casaprestamo.presentation.admin.cuentas.CuentasViewModel

@Composable
fun EmpleadoCuentasScreen(
    viewModel: CuentasViewModel = hiltViewModel(),
    modifier:  Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    EmpleadoCuentasContent(
        uiState         = uiState,
        onLoad          = viewModel::cargar,
        onCrearCliente  = viewModel::crearCliente,
        onEditarUsuario = viewModel::editarUsuario,
        onToggleEstado  = viewModel::cambiarEstado
    )
}