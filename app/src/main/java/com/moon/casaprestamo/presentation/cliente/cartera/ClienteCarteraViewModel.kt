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
                    val prestamos = response.body() ?: emptyList()
                    val estadosVigentes = setOf("ACTIVO", "MORA", "MOROSO")
                    val prestamosVigentes = prestamos.filter { it.estado.uppercase() in estadosVigentes }

                    val pagosPorPrestamo = mutableMapOf<Int, List<PagoData>>()
                    for (prestamo in prestamos) {
                        try {
                            val pagosResp = apiService.obtenerCalendarioPagos(idCliente, prestamo.idPrestamo)
                            pagosPorPrestamo[prestamo.idPrestamo] = if (pagosResp.isSuccessful) pagosResp.body().orEmpty() else emptyList()
                        } catch (_: Exception) {
                            pagosPorPrestamo[prestamo.idPrestamo] = emptyList()
                        }
                    }

                    val prestamosConPagos = prestamos.map { p ->
                        PrestamoConPagos(prestamo = p, pagos = pagosPorPrestamo[p.idPrestamo].orEmpty())
                    }

                    val capitalOtorgado = prestamosVigentes.sumOf { it.montoTotal }.coerceAtLeast(0.0)
                    val saldoPendiente = prestamosVigentes.sumOf { it.saldoPendiente }.coerceAtLeast(0.0)
                    val montoLiquidado = prestamosConPagos
                        .filter { it.prestamo.estado.uppercase() in estadosVigentes }
                        .sumOf { pcp ->
                            pcp.pagos.filter { it.estado.equals("pagado", ignoreCase = true) }.sumOf { it.monto }
                        }
                        .coerceAtLeast(0.0)

                    val mesesTotales = prestamosVigentes.sumOf { it.plazoMeses }

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
