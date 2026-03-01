package com.moon.casaprestamo.presentation.admin.supervision

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
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "SUPERVISION_VM"

enum class SupervisionTab { CARTERA, FOLIOS, SOLICITUDES }

data class CarteraAdminItem(
    val idPrestamo: Int,
    val folio: String,
    val nombreCliente: String,
    val curp: String,
    val fechaAprobacion: String,
    val montoTotal: Double,
    val saldoPendiente: Double,
    val tasaInteres: Double,
    val plazoMeses: Int,
    val pagosRealizados: Int,
    val totalPagos: Int,
    val estado: String,
    val telefono: String?,
    val email: String
)

data class SupervisionUiState(
    val tab: SupervisionTab = SupervisionTab.CARTERA,
    val recaudacionCartera: Double = 0.0,
    val recaudacionFolios: Double = 0.0,
    val solicitudesPendientes: Int = 0,
    val cartera: List<CarteraAdminItem> = emptyList(),
    val carteraLoading: Boolean = false,
    val folios: List<TicketDetalle> = emptyList(),
    val foliosLoading: Boolean = false,
    val solicitudes: List<PrestamoPendienteAdmin> = emptyList(),
    val solicitudesLoading: Boolean = false,
    val estadoCuenta: TicketPrestamoDetalle? = null,
    val estadoCuentaLoading: Boolean = false,
    val solicitudDetalle: PrestamoPendienteAdmin? = null,
    val fechaDesde: String = "",
    val fechaHasta: String = "",
    val mensaje: String? = null,
    val error: String? = null
)

@HiltViewModel
class SupervisionViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val _uiState = MutableStateFlow(SupervisionUiState())
    val uiState: StateFlow<SupervisionUiState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
        cargarCartera()
        cargarFolios()
        cargarSolicitudes()
    }

    fun setTab(tab: SupervisionTab) {
        _uiState.update { it.copy(tab = tab, mensaje = null, error = null) }
        // Mantener KPI de cartera independiente del KPI de folios para evitar parpadeos entre pestañas
        if (_uiState.value.recaudacionCartera == 0.0) cargarEstadisticas()
        when (tab) {
            SupervisionTab.CARTERA -> if (_uiState.value.cartera.isEmpty()) cargarCartera()
            SupervisionTab.FOLIOS  -> cargarFolios() // siempre recargar al entrar
            SupervisionTab.SOLICITUDES -> cargarSolicitudes()
        }
    }

    fun setFechas(desde: String, hasta: String) {
        _uiState.update { it.copy(fechaDesde = desde, fechaHasta = hasta) }

        if (_uiState.value.tab != SupervisionTab.FOLIOS) return

        val desdeApi = displayDateToApi(desde)
        val hastaApi = displayDateToApi(hasta)
        when {
            desde.isBlank() && hasta.isBlank() -> cargarFolios(null)
            desdeApi != null && hastaApi != null -> cargarFoliosPorRango(desdeApi, hastaApi)
            else -> cargarFolios(null)
        }
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            try {
                val r = apiService.obtenerEstadisticas()
                if (r.isSuccessful) {
                    _uiState.update { it.copy(recaudacionCartera = r.body()!!.montoRecuperado) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "estadisticas: ${e.localizedMessage}")
            }
        }
    }

    fun cargarCartera() {
        viewModelScope.launch {
            _uiState.update { it.copy(carteraLoading = true, error = null) }
            try {
                val usuariosResp = apiService.obtenerUsuarios(rol = "Cliente")
                if (!usuariosResp.isSuccessful) {
                    _uiState.update { it.copy(carteraLoading = false, error = "Error al cargar clientes") }
                    return@launch
                }
                val clientes = usuariosResp.body()?.usuarios.orEmpty()
                val items = mutableListOf<CarteraAdminItem>()
                for (cliente in clientes) {
                    try {
                        val pr = apiService.obtenerPrestamosCliente(cliente.idUsuario)
                        if (pr.isSuccessful) {
                            pr.body().orEmpty()
                                .filter {
                                    it.estado.equals("ACTIVO", ignoreCase = true) ||
                                            it.estado.equals("MORA", ignoreCase = true)
                                }
                                .forEach { p ->
                                    items.add(CarteraAdminItem(
                                        idPrestamo    = p.idPrestamo,
                                        folio         = p.folio ?: "MSP-${p.idPrestamo}",
                                        nombreCliente = "${cliente.nombre} ${cliente.apellidoPaterno}".trim(),
                                        curp          = cliente.curp ?: "",
                                        fechaAprobacion = (p.fechaAprobacion ?: p.fechaCreacion).take(10),
                                        montoTotal    = p.montoTotal,
                                        saldoPendiente= p.saldoPendiente,
                                        tasaInteres   = p.tasaInteres,
                                        plazoMeses    = p.plazoMeses,
                                        pagosRealizados = p.pagosRealizados,
                                        totalPagos    = p.totalPagos,
                                        estado        = p.estado,
                                        telefono      = cliente.telefono,
                                        email         = cliente.email
                                    ))
                                }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "prestamos cliente ${cliente.idUsuario}: ${e.localizedMessage}")
                    }
                }
                _uiState.update { it.copy(cartera = items, carteraLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(carteraLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun abrirEstadoCuenta(folio: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(estadoCuentaLoading = true, estadoCuenta = null) }
            try {
                val r = apiService.buscarTicket(folio)
                if (r.isSuccessful) {
                    _uiState.update { it.copy(estadoCuenta = r.body()?.ticket, estadoCuentaLoading = false) }
                } else {
                    _uiState.update { it.copy(estadoCuentaLoading = false, error = "Error ${r.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(estadoCuentaLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun cerrarEstadoCuenta() = _uiState.update { it.copy(estadoCuenta = null) }

    fun cargarFolios(fecha: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(foliosLoading = true, error = null) }
            Log.d(TAG, "⏳ cargarFolios() — fecha=$fecha")
            try {
                val r = apiService.obtenerFoliosAdmin(fecha = fecha)
                Log.d(TAG, "📡 cargarFolios HTTP ${r.code()} — isSuccessful=${r.isSuccessful}")

                if (r.isSuccessful) {
                    val body = r.body()
                    val bodyFinal = if (fecha == null && body?.movimientos.isNullOrEmpty()) {
                        cargarFoliosHistoricos()
                    } else body

                    Log.d(TAG, "✅ body nulo=${body == null}")
                    Log.d(TAG, "✅ movimientos count=${bodyFinal?.movimientos?.size}")
                    Log.d(TAG, "✅ total_cobrado=${bodyFinal?.totalCobrado}")
                    bodyFinal?.movimientos?.forEachIndexed { i, t ->
                        Log.d(TAG, "   [$i] folio=${t.folio} fecha=${t.fechaGeneracion} monto=${t.montoPagado}")
                    }
                    _uiState.update { state ->
                        state.copy(
                            folios        = bodyFinal?.movimientos.orEmpty(),
                            foliosLoading = false,
                            recaudacionFolios = bodyFinal?.totalCobrado ?: 0.0
                        )
                    }
                } else {
                    val errorBody = r.errorBody()?.string()
                    Log.e(TAG, "❌ cargarFolios error ${r.code()} — body=$errorBody")
                    _uiState.update { it.copy(foliosLoading = false, error = "Error ${r.code()}: $errorBody") }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 cargarFolios exception: ${e.javaClass.simpleName} — ${e.localizedMessage}")
                _uiState.update { it.copy(foliosLoading = false, error = e.localizedMessage) }
            }
        }
    }

    private suspend fun cargarFoliosHistoricos(diasMaximos: Int = 30): CorteCajaResponse? {
        Log.d(TAG, "ℹ️ Sin folios del día, iniciando búsqueda histórica (${diasMaximos} días)")
        val acumulados = linkedMapOf<Int, TicketDetalle>()
        var totalCobrado = 0.0

        repeat(diasMaximos) { indice ->
            val fecha = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -indice) }.time
            val fechaApi = apiDateFormat.format(fecha)
            val resp: Response<CorteCajaResponse> = apiService.obtenerFoliosAdmin(fecha = fechaApi)
            if (!resp.isSuccessful) {
                Log.w(TAG, "⚠️ histórico $fechaApi HTTP ${resp.code()}")
                return@repeat
            }

            val body = resp.body() ?: return@repeat
            body.movimientos.forEach { ticket -> acumulados.putIfAbsent(ticket.idTicket, ticket) }
            totalCobrado += body.totalCobrado
        }

        Log.d(TAG, "✅ búsqueda histórica completada: ${acumulados.size} folios")
        return CorteCajaResponse(
            status = "success",
            fecha = "historico",
            totalPagos = acumulados.size,
            totalCobrado = totalCobrado,
            movimientos = acumulados.values.toList()
        )
    }


    private fun displayDateToApi(displayDate: String): String? {
        if (displayDate.isBlank()) return null
        return try {
            apiDateFormat.format(displayDateFormat.parse(displayDate)!!)
        } catch (_: Exception) {
            null
        }
    }

    private fun cargarFoliosPorRango(desdeApi: String, hastaApi: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(foliosLoading = true, error = null) }
            try {
                val desde = apiDateFormat.parse(desdeApi) ?: run {
                    _uiState.update { it.copy(foliosLoading = false) }
                    return@launch
                }
                val hasta = apiDateFormat.parse(hastaApi) ?: run {
                    _uiState.update { it.copy(foliosLoading = false) }
                    return@launch
                }

                if (desde.after(hasta)) {
                    _uiState.update { it.copy(foliosLoading = false, error = "Rango de fechas inválido") }
                    return@launch
                }

                val calendario = Calendar.getInstance().apply { time = desde }
                val limite = Calendar.getInstance().apply { time = hasta }
                val acumulados = linkedMapOf<Int, TicketDetalle>()
                var totalCobrado = 0.0

                while (!calendario.after(limite)) {
                    val fechaApi = apiDateFormat.format(calendario.time)
                    val resp = apiService.obtenerFoliosAdmin(fecha = fechaApi)
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        body?.movimientos?.forEach { t -> acumulados.putIfAbsent(t.idTicket, t) }
                        totalCobrado += body?.totalCobrado ?: 0.0
                    } else {
                        Log.w(TAG, "⚠️ rango $fechaApi HTTP ${resp.code()}")
                    }
                    calendario.add(Calendar.DAY_OF_YEAR, 1)
                }

                _uiState.update {
                    it.copy(
                        folios = acumulados.values.toList(),
                        foliosLoading = false,
                        recaudacionFolios = totalCobrado
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(foliosLoading = false, error = e.localizedMessage) }
            }
        }
    }
    fun cargarSolicitudes() {
        viewModelScope.launch {
            _uiState.update { it.copy(solicitudesLoading = true, error = null) }
            try {
                val r = apiService.obtenerPrestamosPendientesAdmin()
                if (r.isSuccessful) {
                    val lista = r.body().orEmpty()
                    _uiState.update {
                        it.copy(solicitudes = lista, solicitudesPendientes = lista.size, solicitudesLoading = false)
                    }
                } else {
                    _uiState.update { it.copy(solicitudesLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(solicitudesLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun abrirDetalleSolicitud(p: PrestamoPendienteAdmin) = _uiState.update { it.copy(solicitudDetalle = p) }
    fun cerrarDetalleSolicitud() = _uiState.update { it.copy(solicitudDetalle = null) }

    fun aprobarPrestamo(idPrestamo: Int, idAprobador: Int)  = procesarPrestamo(idPrestamo, true, idAprobador)
    fun rechazarPrestamo(idPrestamo: Int, idAprobador: Int) = procesarPrestamo(idPrestamo, false, idAprobador)

    private fun procesarPrestamo(id: Int, esAprobado: Boolean, idAprobador: Int) {
        viewModelScope.launch {
            try {
                val r = apiService.procesarPrestamo(
                    AprobarPrestamoRequest(idPrestamo = id, accion = if (esAprobado) "aprobar" else "rechazar", idEmpleado = idAprobador)
                )
                if (r.isSuccessful) {
                    _uiState.update { it.copy(mensaje = if (esAprobado) "✅ Préstamo aprobado" else "Préstamo rechazado", solicitudDetalle = null) }
                    cargarSolicitudes()
                    cargarEstadisticas()
                } else {
                    _uiState.update { it.copy(mensaje = "Error: ${r.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error: ${e.localizedMessage}") }
            }
        }
    }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }
}
