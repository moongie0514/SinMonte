package com.moon.casaprestamo.ui.components.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.moon.casaprestamo.presentation.cliente.cartera.ClienteCarteraContent
import com.moon.casaprestamo.presentation.cliente.cartera.ClienteCarteraScreen
import com.moon.casaprestamo.presentation.cliente.perfil.ClientePerfilScreen
import com.moon.casaprestamo.presentation.cliente.prestamo.SolicitarPrestamoScreen

@Composable
fun ClienteNavGraph(
    navController: NavHostController,
    nombreUsuario: String,
    idCliente: Int
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CLIENTE_CARTERA
    ) {
        composable(Routes.CLIENTE_CARTERA) {
            ClienteCarteraScreen(idCliente = idCliente)
        }
        composable(Routes.CLIENTE_SOLICITAR) {
            SolicitarPrestamoScreen(idCliente = idCliente)
        }
        composable(Routes.CLIENTE_PERFIL) {
            ClientePerfilScreen(idCliente = idCliente)
        }
    }
}