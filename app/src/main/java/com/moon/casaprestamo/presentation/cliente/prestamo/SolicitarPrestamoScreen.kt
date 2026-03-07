package com.moon.casaprestamo.presentation.cliente.prestamo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

@Composable
fun SolicitarPrestamoScreen(
    idCliente: Int,
    viewModel: SolicitarPrestamoViewModel = hiltViewModel()
) {
    val uiState       = viewModel.uiState
    val lifecycleOwner = LocalLifecycleOwner.current

    // Carga configuración una sola vez
    LaunchedEffect(Unit) {
        viewModel.cargarConfiguracion()
    }

    // ── CLAVE DEL BLOQUEO ────────────────────────────────────
    // Re-verifica elegibilidad cada vez que la pantalla vuelve al
    // primer plano (RESUMED), incluso si el usuario cambia de pestaña
    // y regresa. No hay manera de saltarse esto.
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            viewModel.verificarElegibilidad(idCliente)
        }
    }

    when {
        // ── Cargando elegibilidad ────────────────────────────
        uiState.elegibilidadCargando -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // ── BLOQUEADO ────────────────────────────────────────
        !uiState.puedeSolicitar -> {
            BloqueadoContent(motivo = uiState.motivoBloqueo)
        }

        // ── Formulario habilitado ────────────────────────────
        else -> {
            SolicitarPrestamoContent(
                state         = uiState,
                onMontoChange = viewModel::onMontoChange,
                onPlazoChange = viewModel::onPlazoChange,
                onEnviar      = { viewModel.enviarSolicitud(idCliente) },
                onBack        = {}
            )
        }
    }
}

// ── Pantalla de bloqueo — no hay formulario, no hay manera de acceder ──
@Composable
private fun BloqueadoContent(motivo: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier            = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape    = RoundedCornerShape(20.dp),
                color    = MaterialTheme.colorScheme.errorContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint     = MaterialTheme.colorScheme.error
                    )
                }
            }

            Text(
                "SOLICITUD NO DISPONIBLE",
                fontWeight    = FontWeight.Black,
                fontSize      = 16.sp,
                letterSpacing = 1.sp,
                color         = MaterialTheme.colorScheme.onBackground
            )

            Surface(
                color    = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                shape    = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text       = motivo ?: "No cumples los requisitos para solicitar un préstamo en este momento.",
                    modifier   = Modifier.padding(20.dp),
                    textAlign  = TextAlign.Center,
                    fontSize   = 14.sp,
                    lineHeight = 20.sp,
                    color      = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                "Cuando cumplas los requisitos, esta pantalla se desbloqueará automáticamente.",
                textAlign  = TextAlign.Center,
                fontSize   = 12.sp,
                color      = MaterialTheme.colorScheme.outline,
                lineHeight = 18.sp
            )
        }
    }
}