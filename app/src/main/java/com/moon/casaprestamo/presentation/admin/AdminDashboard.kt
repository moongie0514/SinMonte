package com.moon.casaprestamo.presentation.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moon.casaprestamo.ui.components.navigation.AdminNavGraph
import com.moon.casaprestamo.ui.components.navigation.Routes
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme

@Composable
fun AdminDashboard(
    idAdmin: Int,
    userName: String = "ADMIN",
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route
            ?: Routes.ADMIN_REPORTES

    AdminLayout(
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
                Routes.ADMIN_REPORTES -> "Analítica de Cartera"
                Routes.ADMIN_SUPERVISION -> "Supervisión"
                Routes.ADMIN_CUENTAS -> ""
                Routes.ADMIN_CONFIGURACION -> "Configuración"
                else -> ""
            }
        }
    ) {
        AdminNavGraph(navController, idAdmin = idAdmin)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminDashboardPreview() {
    CasaPrestamoTheme (darkTheme = false) {
        AdminDashboard(idAdmin = 1)
    }
}