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

                    // Cards superiores: solo préstamos vigentes
                    val estadosCapital  = setOf("ACTIVO", "MORA", "MOROSO", "LIQUIDADO")
                    val estadosSaldo    = setOf("ACTIVO", "MORA", "MOROSO")

                    val capitalOtorgado = prestamos
                        .filter { it.estado.uppercase() in estadosCapital }
                        .sumOf { it.montoTotal }.coerceAtLeast(0.0)

                    val saldoPendiente = prestamos
                        .filter { it.estado.uppercase() in estadosSaldo }
                        .sumOf { it.saldoPendiente }.coerceAtLeast(0.0)

                    val mesesTotales      = prestamos.sumOf { it.plazoMeses }

                    // Ordenar: ACTIVO/MORA primero, luego LIQUIDADO, por fecha descendente
                    val prestamosOrdenados = prestamos.sortedWith(
                        compareBy<Any?> { p ->
                            val prestamo = p as PrestamoData
                            when (prestamo.estado.uppercase()) {
                                "ACTIVO", "MORA", "MOROSO" -> 0
                                "LIQUIDADO"                -> 1
                                else                       -> 2
                            }
                        }.thenByDescending { p ->
                            (p as PrestamoData).fechaCreacion
                        }
                    )

                    val prestamosConPagos = prestamosOrdenados.map {
                        PrestamoConPagos(prestamo = it, pagos = emptyList())
                    }

                    _uiState.update {
                        it.copy(
                            isLoading         = false,
                            prestamos         = prestamosOrdenados,
                            prestamosConPagos = prestamosConPagos,
                            capitalOtorgado   = capitalOtorgado,
                            saldoPendiente    = saldoPendiente,
                            totalRestante     = saldoPendiente,
                            saldoActual       = saldoPendiente,
                            montoLiquidado    = 0.0, // se recalcula en cargarPagosTodos
                            mesesTotales      = mesesTotales,
                            prestamosExpandidos = emptySet(), // resetear expansiones al recargar
                            error             = null
                        )
                    }

                    // Cargar pagos de TODOS los préstamos para tener calendarios listos
                    prestamosOrdenados.forEach { p ->
                        cargarPagos(idCliente, p.idPrestamo)
                    }

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

                    _uiState.update { state ->
                        val nuevosPrestamosConPagos = state.prestamosConPagos.map { pc ->
                            if (pc.prestamo.idPrestamo == idPrestamo) pc.copy(pagos = pagos)
                            else pc
                        }

                        // Recalcular montoLiquidado sumando todos los pagos pagados de todos los préstamos
                        val montoLiquidadoTotal = nuevosPrestamosConPagos
                            .flatMap { it.pagos }
                            .filter { it.estado.equals("pagado", ignoreCase = true) }
                            .sumOf { it.monto }

                        Log.d(TAG, "pagos cargados prestamo $idPrestamo: ${pagos.size} — liquidado total: $montoLiquidadoTotal")

                        state.copy(
                            pagos = marcarPagable(pagos),
                            prestamosConPagos = state.prestamosConPagos.map { pcp ->
                                if (pcp.prestamo.idPrestamo == idPrestamo)
                                    pcp.copy(pagos = marcarPagable(pagos))   // ← y esta
                                 else pcp
                             },
                            montoLiquidado    = montoLiquidadoTotal
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error al cargar pagos: ${e.localizedMessage}")
            }
        }
    }
    private fun marcarPagable(pagos: List<PagoData>): List<PagoData> {
        // Encuentra el índice del primer pago que NO está pagado
        val primerPendiente = pagos.indexOfFirst { it.estado != "pagado" }
        return pagos.mapIndexed { index, pago ->
            pago.copy(esPagable = index == primerPendiente)
        }
    }


    // Expande o colapsa la tarjeta de un préstamo — solo uno expandido a la vez
    fun toggleExpansion(idPrestamo: Int) {
        _uiState.update { state ->
            val expandidos = state.prestamosExpandidos
            state.copy(
                prestamosExpandidos = if (expandidos.contains(idPrestamo))
                    expandidos - idPrestamo
                else
                    setOf(idPrestamo) // solo uno a la vez
            )
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