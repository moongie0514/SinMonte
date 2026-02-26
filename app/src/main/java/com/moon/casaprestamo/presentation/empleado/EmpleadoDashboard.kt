package com.moon.casaprestamo.presentation.empleado

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moon.casaprestamo.ui.components.navigation.*
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

@Composable
fun EmpleadoDashboard(
    idEmpleado: Int,
    userName: String = "EMPLEADO",
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route
            ?: Routes.EMPLEADO_COBRANZA

    EmpleadoLayout(
        userName = userName,
        vistaActiva = currentRoute,
        onVistaChange = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
                restoreState = true
            }
        },
        onLogout = onLogout,
        titleProvider = { route ->
            when (route) {
                Routes.EMPLEADO_COBRANZA -> "COBRANZA"
                Routes.EMPLEADO_CLIENTES -> "CLIENTES"
                Routes.EMPLEADO_PRESTAMOS -> "PRÉSTAMOS"
                else -> ""
            }
        }
    ) {
        EmpleadoNavGraph(
            navController = navController,
            idEmpleado = idEmpleado
        )

    }
}
