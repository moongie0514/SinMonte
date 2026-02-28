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

    fun cargarCartera(idCliente: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                Log.d(TAG, "GET /cliente/$idCliente/prestamos")
                val response = apiService.obtenerPrestamosCliente(idCliente)

                Log.d(TAG, "HTTP ${response.code()}")

                if (response.isSuccessful) {
                    val prestamosOriginal = response.body() ?: emptyList()

                    // Excluir RECHAZADO del resumen de cartera del cliente
                    val prestamos = prestamosOriginal.filterNot { it.estado.equals("RECHAZADO", ignoreCase = true) }

                    val estadosCapital = setOf("ACTIVO", "MORA", "MOROSO", "LIQUIDADO")
                    val estadosSaldo = setOf("ACTIVO", "MORA", "MOROSO")

                    val capitalOtorgado = prestamos
                        .filter { it.estado.uppercase() in estadosCapital }
                        .sumOf { it.montoTotal }
                        .coerceAtLeast(0.0)

                    val saldoPendiente = prestamos
                        .filter { it.estado.uppercase() in estadosSaldo }
                        .sumOf { it.saldoPendiente }
                        .coerceAtLeast(0.0)

                    val montoLiquidado = (capitalOtorgado - saldoPendiente).coerceAtLeast(0.0)
                    val mesesTotales = prestamos.sumOf { it.plazoMeses }

                    val prestamosConPagos = prestamos.map { PrestamoConPagos(prestamo = it, pagos = emptyList()) }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            prestamos = prestamos,
                            prestamosConPagos = prestamosConPagos,
                            capitalOtorgado = capitalOtorgado,
                            saldoPendiente = saldoPendiente,
                            totalRestante = saldoPendiente,
                            saldoActual = saldoPendiente,
                            montoLiquidado = montoLiquidado,
                            mesesTotales = mesesTotales,
                            error = null
                        )
                    }

                    // Cargar calendario del préstamo vigente para mostrar tabla inmediatamente
                    val prestamoActual = prestamos.firstOrNull {
                        it.estado.equals("ACTIVO", true) || it.estado.equals("MORA", true) || it.estado.equals("MOROSO", true)
                    } ?: prestamos.firstOrNull { it.estado.equals("LIQUIDADO", true) }

                    prestamoActual?.let { cargarPagos(idCliente, it.idPrestamo) }

                } else {
                    val error = try {
                        JSONObject(response.errorBody()?.string() ?: "").optString("detail", "Error del servidor")
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
                    _uiState.update { state ->
                        state.copy(
                            pagos = pagos,
                            prestamosConPagos = state.prestamosConPagos.map { pcp ->
                                if (pcp.prestamo.idPrestamo == idPrestamo) pcp.copy(pagos = pagos) else pcp
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error cargando pagos", e)
            }
        }
    }
}
