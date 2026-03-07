package com.moon.casaprestamo.presentation.empleado.supervision

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.moon.casaprestamo.presentation.admin.supervision.AdminSupervisionContent
import com.moon.casaprestamo.presentation.admin.supervision.SupervisionViewModel

@Composable
fun EmpleadoSupervisionScreen(
    idEmpleado: Int,
    viewModel:  SupervisionViewModel = hiltViewModel(),
    modifier:   Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminSupervisionContent(
        uiState              = uiState,
        idAprobador          = idEmpleado,
        esAdmin              = false,                           // ← clave: restringe recaudación y solicitudes
        onSetTab             = viewModel::setTab,
        onSetFechas          = viewModel::setFechas,
        onCargarFolios       = viewModel::cargarFolios,
        onAbrirEstadoCuenta  = viewModel::abrirEstadoCuenta,
        onCerrarEstadoCuenta = viewModel::cerrarEstadoCuenta,
        onAbrirTicketPago    = viewModel::abrirTicketPago,
        onCerrarTicketPago   = viewModel::cerrarTicketPago,
        onAbrirSolicitud     = { },                            // empleado nunca aprueba — no-op
        onCerrarSolicitud    = viewModel::cerrarDetalleSolicitud,
        onAprobar            = { },                            // bloqueado por esAdmin = false
        onRechazar           = { },                            // bloqueado por esAdmin = false
        onLimpiarMensaje     = viewModel::limpiarMensaje,
        modifier             = modifier
    )
}