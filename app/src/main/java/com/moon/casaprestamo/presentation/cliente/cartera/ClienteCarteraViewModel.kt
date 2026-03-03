package com.moon.casaprestamo.presentation.cliente.cartera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.*
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "CARTERA_VM"

@HiltViewModel
class ClienteCarteraViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarteraUiState())
    val uiState: StateFlow<CarteraUiState> = _uiState.asStateFlow()

    private var idClienteActual: Int = 0

    fun cargarCartera(idCliente: Int) {
        idClienteActual = idCliente
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d(TAG, "GET /cliente/$idCliente/prestamos")
                val response = apiService.obtenerPrestamosCliente(idCliente)
                Log.d(TAG, "HTTP ${response.code()}")

                if (response.isSuccessful) {
                    val prestamosOriginal = response.body() ?: emptyList()
                    val prestamos = prestamosOriginal.filterNot {
                        it.estado.equals("RECHAZADO", ignoreCase = true)
                    }

                    prestamos.forEach { p ->
                        Log.d(TAG, "préstamo ${p.idPrestamo} estado=${p.estado} montoTotal=${p.montoTotal} saldoPendiente=${p.saldoPendiente}")
                    }

                    val estadosCapital = setOf("ACTIVO", "MORA", "MOROSO", "LIQUIDADO")
                    val estadosSaldo   = setOf("ACTIVO", "MORA", "MOROSO")

                    val capitalOtorgado = prestamos
                        .filter { it.estado.uppercase() in estadosCapital }
                        .sumOf { it.montoTotal }
                        .coerceAtLeast(0.0)

                    val saldoPendiente = prestamos
                        .filter { it.estado.uppercase() in estadosSaldo }
                        .sumOf { it.saldoPendiente }
                        .coerceAtLeast(0.0)

                    val mesesTotales      = prestamos.sumOf { it.plazoMeses }
                    val prestamosConPagos = prestamos.map { PrestamoConPagos(prestamo = it, pagos = emptyList()) }

                    // montoLiquidado se calculará correctamente en cargarPagos
                    // desde los pagos reales — aquí lo dejamos en 0 temporalmente
                    _uiState.update {
                        it.copy(
                            isLoading         = false,
                            prestamos         = prestamos,
                            prestamosConPagos = prestamosConPagos,
                            capitalOtorgado   = capitalOtorgado,
                            saldoPendiente    = saldoPendiente,
                            totalRestante     = saldoPendiente,
                            saldoActual       = saldoPendiente,
                            montoLiquidado    = 0.0,   // se actualiza en cargarPagos
                            mesesTotales      = mesesTotales,
                            error             = null
                        )
                    }

                    val prestamoActual = prestamos.firstOrNull {
                        it.estado.equals("ACTIVO",   true) ||
                                it.estado.equals("MORA",     true) ||
                                it.estado.equals("MOROSO",   true)
                    } ?: prestamos.firstOrNull { it.estado.equals("LIQUIDADO", true) }

                    prestamoActual?.let { cargarPagos(idCliente, it.idPrestamo) }

                } else {
                    val error = try {
                        JSONObject(response.errorBody()?.string() ?: "")
                            .optString("detail", "Error del servidor")
                    } catch (e: Exception) { "Error del servidor" }
                    Log.e(TAG, "❌ HTTP ${response.code()}: $error")
                    _uiState.update { it.copy(isLoading = false, error = error) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error de red", e)
                _uiState.update { it.copy(isLoading = false, error = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    fun cargarPagos(idCliente: Int, idPrestamo: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "GET /cliente/$idCliente/prestamos/$idPrestamo/pagos")
                val response = apiService.obtenerCalendarioPagos(idCliente, idPrestamo)

                if (response.isSuccessful) {
                    val pagos = response.body() ?: emptyList()

                    // ✅ montoLiquidado = suma real de pagos con estado "pagado"
                    // Correcto con intereses: no depende de montoTotal vs saldoPendiente
                    val montoLiquidado = pagos
                        .filter { it.estado.equals("pagado", ignoreCase = true) }
                        .sumOf { it.monto }

                    Log.d(TAG, "pagos cargados: ${pagos.size} — liquidado: $montoLiquidado")

                    _uiState.update { state ->
                        state.copy(
                            prestamosConPagos = state.prestamosConPagos.map { pc ->
                                if (pc.prestamo.idPrestamo == idPrestamo) pc.copy(pagos = pagos)
                                else pc
                            },
                            montoLiquidado = montoLiquidado
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error al cargar pagos: ${e.localizedMessage}")
            }
        }
    }

    fun registrarPago(idPago: Int) {
        val idCliente = idClienteActual
        if (idCliente == 0) {
            Log.e(TAG, "❌ registrarPago: idCliente no inicializado")
            _uiState.update { it.copy(mensajePago = "Error: sesión inválida") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(pagoEnProceso = idPago) }
            try {
                Log.d(TAG, "POST /cliente/registrar_pago → idPago=$idPago idCliente=$idCliente")
                val request  = RegistrarPagoClienteRequest(idPago = idPago, idCliente = idCliente)
                val response = apiService.registrarPagoCliente(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "✅ Pago registrado: ${body?.message}")
                    _uiState.update { it.copy(pagoEnProceso = null, mensajePago = "✅ ${body?.message}") }
                    cargarCartera(idCliente)
                } else {
                    val errorMsg = try {
                        JSONObject(response.errorBody()?.string() ?: "")
                            .optString("detail", "Error al registrar pago")
                    } catch (e: Exception) { "Error al registrar pago" }
                    Log.e(TAG, "❌ Error ${response.code()}: $errorMsg")
                    _uiState.update { it.copy(pagoEnProceso = null, mensajePago = errorMsg) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Excepción al pagar: ${e.localizedMessage}")
                _uiState.update { it.copy(pagoEnProceso = null, mensajePago = "Error de red: ${e.localizedMessage}") }
            }
        }
    }

    fun limpiarMensajePago() = _uiState.update { it.copy(mensajePago = null) }
}