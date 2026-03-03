package com.moon.casaprestamo.presentation.empleado.cobranza

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.PagoPendiente
import com.moon.casaprestamo.data.models.RegistrarPagoRequest
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CobranzaUiState(
    val pagosPendientes: List<PagoPendiente> = emptyList(),
    val isLoading: Boolean = false,
    val mensaje: String? = null,
    val ultimoFolio: String? = null
)

@HiltViewModel
class EmpleadoCobranzaViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CobranzaUiState())
    val uiState: StateFlow<CobranzaUiState> = _uiState.asStateFlow()

    init {
        cargarPagosPendientes()
    }

    fun cargarPagosPendientes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.obtenerPagosPendientes()
                if (response.isSuccessful) {
                    val pagos = response.body() ?: emptyList()
                    Log.d("COBRANZA_VM", "✅ Pagos pendientes: ${pagos.size}")
                    _uiState.update { it.copy(pagosPendientes = pagos, isLoading = false) }
                } else {
                    Log.e("COBRANZA_VM", "❌ Error: ${response.code()}")
                    _uiState.update { it.copy(isLoading = false, mensaje = "Error al cargar") }
                }
            } catch (e: Exception) {
                Log.e("COBRANZA_VM", "💥 Error de red", e)
                _uiState.update { it.copy(isLoading = false, mensaje = "Error de red") }
            }
        }
    }

    fun registrarPago(idPago: Int, idEmpleado: Int, _metodoPago: String) {
        viewModelScope.launch {
            try {
                Log.d("COBRANZA_VM", "=== REGISTRANDO PAGO === id_pago: $idPago, empleado: $idEmpleado, método: $_metodoPago")

                val request = RegistrarPagoRequest(
                    id_pago = idPago,
                )
                val response = apiService.registrarPago(request)

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Log.d("COBRANZA_VM", "✅ Pago registrado para préstamo ${result.idPrestamo} | Monto: ${result.monto}")
                    _uiState.update {
                        it.copy(
                            mensaje = "✅ ${result.message}",
                            ultimoFolio = null
                        )
                    }
                    cargarPagosPendientes()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("COBRANZA_VM", "❌ Error ${response.code()}: $errorBody")
                    _uiState.update { it.copy(mensaje = "Error al registrar pago") }
                }
            } catch (e: Exception) {
                Log.e("COBRANZA_VM", "💥 Error al registrar", e)
                _uiState.update { it.copy(mensaje = "Error: ${e.localizedMessage}") }
            }
        }
    }

    // ✅ PUNTO 3: Liquidación total — registra todos los pagos pendientes
    // del préstamo en secuencia. El servidor calcula saldos y marca
    // el préstamo como LIQUIDADO automáticamente al llegar a 0.
    fun liquidarTodo(idPrestamo: Int, idEmpleado: Int, _metodoPago: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Filtramos todos los pagos pendientes del préstamo específico
                val pagosDePrestamo = _uiState.value.pagosPendientes
                    .filter { it.id_prestamo == idPrestamo }
                    .sortedBy { it.numero_pago }

                if (pagosDePrestamo.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false, mensaje = "No hay pagos pendientes para este préstamo") }
                    return@launch
                }

                Log.d("COBRANZA_VM", "=== LIQUIDANDO TOTAL === ${pagosDePrestamo.size} pagos del préstamo $idPrestamo")

                var pagosRegistrados = 0
                var errorEncontrado = false

                for (pago in pagosDePrestamo) {
                    val request = RegistrarPagoRequest(
                        id_pago = pago.id_pago,
                    )
                    val response = apiService.registrarPago(request)

                    if (response.isSuccessful && response.body() != null) {
                        pagosRegistrados++
                        Log.d("COBRANZA_VM", "✅ Pago ${pago.numero_pago} liquidado")
                    } else {
                        Log.e("COBRANZA_VM", "❌ Falló pago ${pago.numero_pago}: ${response.code()}")
                        errorEncontrado = true
                        break
                    }
                }

                if (!errorEncontrado) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mensaje = "✅ Préstamo liquidado ($pagosRegistrados pagos)",
                            ultimoFolio = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mensaje = "⚠️ Liquidación parcial: $pagosRegistrados/${pagosDePrestamo.size} pagos registrados"
                        )
                    }
                }

                cargarPagosPendientes()

            } catch (e: Exception) {
                Log.e("COBRANZA_VM", "💥 Error al liquidar", e)
                _uiState.update { it.copy(isLoading = false, mensaje = "Error: ${e.localizedMessage}") }
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null, ultimoFolio = null) }
    }
}