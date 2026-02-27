package com.moon.casaprestamo.ui.components.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.moon.casaprestamo.presentation.admin.*
import com.moon.casaprestamo.presentation.admin.configuracion.AdminConfiguracionContent
import com.moon.casaprestamo.presentation.admin.configuracion.AdminConfiguracionScreen
import com.moon.casaprestamo.presentation.admin.reportes.AdminReportesScreen
import com.moon.casaprestamo.presentation.admin.supervision.AdminSupervisionScreen

@Composable
fun AdminNavGraph(navController: NavHostController, idAdmin: Int) {
    NavHost(
        navController = navController,
        startDestination = Routes.ADMIN_REPORTES
    ) {
        composable(Routes.ADMIN_REPORTES) {
            AdminReportesScreen()
        }

        composable(Routes.ADMIN_SUPERVISION) {
            AdminSupervisionScreen(idAprobador = idAdmin)
        }

        composable(Routes.ADMIN_CUENTAS) {
            AdminCuentasContent()
        }

        composable(Routes.ADMIN_CONFIGURACION) {
            AdminConfiguracionScreen()
        }
    }
}