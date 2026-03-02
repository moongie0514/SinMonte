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
        onAbrirSolicitud     = viewModel::abrirDetalleSolicitud,
        onCerrarSolicitud    = viewModel::cerrarDetalleSolicitud,
        onAprobar            = { id -> viewModel.aprobarPrestamo(id, idAprobador) },
        onRechazar           = { id -> viewModel.rechazarPrestamo(id, idAprobador) },
        onLimpiarMensaje     = viewModel::limpiarMensaje,
        modifier             = modifier
    )
}