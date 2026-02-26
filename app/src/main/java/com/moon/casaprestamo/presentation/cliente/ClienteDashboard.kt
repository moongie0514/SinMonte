package com.moon.casaprestamo.presentation.cliente

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.moon.casaprestamo.ui.components.navigation.ClienteNavGraph
import com.moon.casaprestamo.ui.components.navigation.Routes

@Composable
fun ClienteDashboard(
    idCliente: Int,
    userName: String,
    onLogout: () -> Unit = {}
) {
    Log.d("CLIENT_DASHBOARD", "=== INICIANDO DASHBOARD ===")
    Log.d("CLIENT_DASHBOARD", "ID Cliente: $idCliente")

    // ⚠️ VALIDACIÓN CRÍTICA
    if (idCliente <= 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Error: ID de cliente inválido",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        return
    }
    // 1. Usar un navController dedicado para la navegación interna del cliente
    val internalNavController = rememberNavController()

    // 2. Estado para el título de la TopBar
    var currentTitle by remember { mutableStateOf("Mi Cartera") }

    ClienteLayout(
        userName = userName,
        vistaActiva = "", // Esto lo puedes vincular al backstack después
        onVistaChange = { route ->
            // Actualizamos el título según la ruta
            currentTitle = when (route) {
                Routes.CLIENTE_CARTERA -> "Mi Cartera"
                Routes.CLIENTE_SOLICITAR -> "Solicitar Crédito"
                Routes.CLIENTE_PERFIL -> "Mi Perfil"
                else -> "Dashboard"
            }

            // Navegación interna segura
            internalNavController.navigate(route) {
                // Evita acumular pantallas en el stack
                popUpTo(internalNavController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        },
        onLogout = onLogout,
        titleProvider = { currentTitle } // Usamos el estado local
    ) {
        // 3. El Grafo interno que maneja las pantallas de Cartera, Perfil, etc.
        ClienteNavGraph(
            navController = internalNavController,
            idCliente = idCliente,
            nombreUsuario = userName
        )
    }
}
