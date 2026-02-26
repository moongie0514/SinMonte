package com.moon.casaprestamo.ui.components.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.moon.casaprestamo.presentation.empleado.*
import com.moon.casaprestamo.presentation.empleado.cobranza.EmpleadoCobranzaScreen

@Composable
fun EmpleadoNavGraph(
    navController: NavHostController,
    idEmpleado: Int
) {
    NavHost(
        navController = navController,
        startDestination = Routes.EMPLEADO_COBRANZA,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Routes.EMPLEADO_COBRANZA) {
            EmpleadoCobranzaScreen(idEmpleado = idEmpleado)
        }
        composable(Routes.EMPLEADO_CLIENTES) {
            EmpleadoClientesContent()
        }
        composable(Routes.EMPLEADO_PRESTAMOS) {
            EmpleadoPrestamosContent()
        }
    }
}
