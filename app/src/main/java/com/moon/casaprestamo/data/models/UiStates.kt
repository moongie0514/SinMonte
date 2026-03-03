package com.moon.casaprestamo.data.models

// ==================== UI STATES ====================

// --- AUTH ---
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginResult: LoginResult? = null
)

sealed class LoginResult {
    data class Success(val usuario: Usuario) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

data class RegistroUiState(
    val nombre: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val curp: String = "",
    val ine: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val fechaNacimiento: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false,
    val emailParaVerificar: String = "",
    val errorMessage: String? = null
)

data class VerificarEmailUiState(
    val email: String = "",
    val codigo: String = "",
    val isLoading: Boolean = false,
    val verificado: Boolean = false,
    val errorMessage: String? = null
)

data class CarteraUiState(
    val nombreCliente: String = "",
    val capitalOtorgado: Double = 0.0,
    val totalPagado: Double = 0.0,
    val totalRestante: Double = 0.0,
    val saldoActual: Double = 0.0,
    val mesesTotales: Int = 0,
    val prestamosConPagos: List<PrestamoConPagos> = emptyList(),
    val pagos: List<PagoData> = emptyList(),
    val montoLiquidado: Double = 0.0,
    val saldoPendiente: Double = 0.0,
    val mensajeAviso: String? = null,
    val prestamos: List<PrestamoData> = emptyList(),
    val pagoEnProceso: Int? = null,
    val mensajePago: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ClientePerfilUiState(
    val idCliente: Int = 0,
    val nombreCompleto: String = "",
    val curp: String = "",
    val numeroIne: String = "",
    val fechaNacimiento: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val fechaRegistro: String = "",
    val isLoading: Boolean = false
)

// --- ADMIN ---
data class ConfigAdminUiState(
    val tasaInteres: String = "",
    val plazoMaximo: String = "",
    val montoMinimo: String = "",
    val montoMaximo: String = "",
    val isLoading: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false
)
data class AdminReportesUiState(
    val capitalActivo: Double = 0.0,
    val capitalRecuperado: Double = 0.0,
    val prestamosEnMora: Int = 0,
    val tasaPromedio: Double = 0.0,
    val saldoPendiente: Double = 0.0,
    val totalClientes: Int = 0,
    val prestamosActivos: Int = 0,
    val prestamosPendientes: Int = 0,
    val prestamosLiquidados: Int = 0,
    val totalPrestamos: Int = 0,
    val isLoading: Boolean = false
)