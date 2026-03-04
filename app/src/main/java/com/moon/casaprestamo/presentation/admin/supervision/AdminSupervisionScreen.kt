package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminSupervisionScreen(
    idAprobador: Int,
    esAdmin: Boolean = true,
    viewModel: SupervisionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminSupervisionContent(
        uiState              = uiState,
        idAprobador          = idAprobador,
        esAdmin             = esAdmin,
        onSetTab             = viewModel::setTab,
        onSetFechas          = viewModel::setFechas,
        onCargarFolios       = viewModel::cargarFolios,
        onAbrirEstadoCuenta  = viewModel::abrirEstadoCuenta,
        onCerrarEstadoCuenta = viewModel::cerrarEstadoCuenta,
        onAbrirTicketPago    = viewModel::abrirTicketPago,   // ← nuevo
        onCerrarTicketPago   = viewModel::cerrarTicketPago,  // ← nuevo
        onAbrirSolicitud     = viewModel::abrirDetalleSolicitud,
        onCerrarSolicitud    = viewModel::cerrarDetalleSolicitud,
        onAprobar  = { idPrestamo -> viewModel.aprobarPrestamo(idPrestamo, idAprobador) },
        onRechazar = { idPrestamo -> viewModel.rechazarPrestamo(idPrestamo, idAprobador) },
        onLimpiarMensaje     = viewModel::limpiarMensaje
    )
}