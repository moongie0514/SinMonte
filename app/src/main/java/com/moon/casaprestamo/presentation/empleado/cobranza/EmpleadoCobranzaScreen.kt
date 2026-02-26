package com.moon.casaprestamo.presentation.empleado.cobranza

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun EmpleadoCobranzaScreen(
    idEmpleado: Int,
    viewModel: EmpleadoCobranzaViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    EmpleadoCobranzaContent(
        uiState = uiState,
        idEmpleado = idEmpleado,
        onRegistrarPago = viewModel::registrarPago,
        onLiquidarTodo = viewModel::liquidarTodo,     // ✅ PUNTO 3
        onLimpiarMensaje = viewModel::limpiarMensaje,
        modifier = modifier
    )
}