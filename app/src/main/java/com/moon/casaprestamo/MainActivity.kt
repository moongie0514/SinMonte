package com.moon.casaprestamo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moon.casaprestamo.auth.*
import com.moon.casaprestamo.presentation.admin.AdminDashboard
import com.moon.casaprestamo.presentation.cliente.ClienteDashboard
import com.moon.casaprestamo.presentation.empleado.EmpleadoDashboard
import com.moon.casaprestamo.ui.theme.CasaPrestamoTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CasaPrestamoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { usuario ->
                    val nombre = URLEncoder.encode(
                        usuario.nombreCompleto,
                        StandardCharsets.UTF_8.toString()
                    )
                    Log.d("NAV", "Login OK — rol: ${usuario.rol}, id: ${usuario.idUsuario}")

                    // Rol es case-sensitive: "Cliente" | "Empleado" | "Admin"
                    when (usuario.rol) {
                        "Admin" -> navController.navigate("admin/${usuario.idUsuario}/$nombre") {
                            popUpTo("login") { inclusive = true }
                        }
                        "Cliente" -> navController.navigate("cliente/${usuario.idUsuario}/$nombre") {
                            popUpTo("login") { inclusive = true }
                        }
                        "Empleado" -> navController.navigate("empleado/${usuario.idUsuario}/$nombre") {
                            popUpTo("login") { inclusive = true }
                        }
                        else -> Log.e("NAV", "Rol desconocido: ${usuario.rol}")
                    }
                },
                onNavigateToRegister = { navController.navigate("registro_usuario") },
                onNavigateToForgot   = { navController.navigate("forgot_password") }
            )
        }

        // ── Registro → verificación email ──────────────────────────────────────
        composable("registro_usuario") {
            RegistroUsuarioScreen(
                onBack = { navController.popBackStack() },
                onRegistroExitoso = { email ->
                    val emailEnc = URLEncoder.encode(email, StandardCharsets.UTF_8.toString())
                    navController.navigate("verificar_email/$emailEnc") {
                        popUpTo("registro_usuario") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route     = "verificar_email/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStack ->
            val email = URLDecoder.decode(
                backStack.arguments?.getString("email") ?: "",
                StandardCharsets.UTF_8.toString()
            )
            VerificarEmailScreen(
                email         = email,
                onVerificado  = {
                    navController.navigate("login") { popUpTo(0) }
                },
                onBackToLogin = {
                    navController.navigate("login") { popUpTo(0) }
                }
            )
        }

        // ── Recuperación contraseña ────────────────────────────────────────────
        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack("login", false) }
            )
        }

        // ── Admin ──────────────────────────────────────────────────────────────
        composable(
            route     = "admin/{idUsuario}/{userName}",
            arguments = listOf(
                navArgument("idUsuario") { type = NavType.IntType },
                navArgument("userName")  { type = NavType.StringType }
            )
        ) { back ->
            val nombre = URLDecoder.decode(
                back.arguments?.getString("userName") ?: "ADMIN",
                StandardCharsets.UTF_8.toString()
            )
            AdminDashboard(
                idAdmin = back.arguments?.getInt("idUsuario") ?: 0,
                userName = nombre,
                onLogout = { navController.navigate("login") { popUpTo(0) } }
            )
        }

        // ── Empleado ───────────────────────────────────────────────────────────
        composable(
            route     = "empleado/{idUsuario}/{userName}",
            arguments = listOf(
                navArgument("idUsuario") { type = NavType.IntType },
                navArgument("userName")  { type = NavType.StringType }
            )
        ) { back ->
            val idUsuario = back.arguments?.getInt("idUsuario") ?: 0
            val nombre = URLDecoder.decode(
                back.arguments?.getString("userName") ?: "EMPLEADO",
                StandardCharsets.UTF_8.toString()
            )
            EmpleadoDashboard(
                idEmpleado = idUsuario,
                userName   = nombre,
                onLogout   = { navController.navigate("login") { popUpTo(0) } }
            )
        }

        // ── Cliente ────────────────────────────────────────────────────────────
        composable(
            route     = "cliente/{idUsuario}/{userName}",
            arguments = listOf(
                navArgument("idUsuario") { type = NavType.IntType },
                navArgument("userName")  { type = NavType.StringType }
            )
        ) { back ->
            val idUsuario = back.arguments?.getInt("idUsuario") ?: 0
            val nombre = URLDecoder.decode(
                back.arguments?.getString("userName") ?: "CLIENTE",
                StandardCharsets.UTF_8.toString()
            )
            ClienteDashboard(
                idCliente  = idUsuario,     // ahora es id_usuario de la tabla unificada
                userName   = nombre,
                onLogout   = { navController.navigate("login") { popUpTo(0) } }
            )
        }
    }
}