package com.moon.casaprestamo.presentation.cliente.prestamo

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.SolicitudCreditoRequest
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SOLICITUD_VM"

data class SolicitarPrestamoUiState(
    val monto:                String  = "",
    val plazoMeses:           Int     = 12,
    val tasaConfigurada:      Double  = 5.0,
    val montoMaximoPermitido: Double  = 50000.0,
    val montoMinimoPermitido: Double  = 1000.0,
    val cuotaEstimada:        Double? = null,
    val isLoading:            Boolean = false,
    val resultado:            String? = null,
    val error:                String? = null,
    // ── Elegibilidad ─────────────────────────────────────────
    val elegibilidadCargando: Boolean = true,   // true = mostramos spinner/bloqueo hasta saber
    val puedeSolicitar:       Boolean = false,  // false por defecto: seguro antes de consultar
    val motivoBloqueo:        String? = null
)

@HiltViewModel
class SolicitarPrestamoViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    val plazosDisponibles = listOf(6, 12, 24, 36, 48)

    var uiState by mutableStateOf(SolicitarPrestamoUiState())
        private set

    // Llamar desde LaunchedEffect(Unit) Y desde LaunchedEffect(lifecycle == RESUMED)
    fun verificarElegibilidad(idCliente: Int) {
        viewModelScope.launch {
            // Reset a bloqueado mientras carga — nunca dejamos pasar por defecto
            uiState = uiState.copy(elegibilidadCargando = true, puedeSolicitar = false)
            try {
                val r = apiService.verificarElegibilidad(idCliente)
                if (r.isSuccessful && r.body() != null) {
                    val body = r.body()!!
                    uiState = uiState.copy(
                        elegibilidadCargando = false,
                        puedeSolicitar       = body.puedeSolicitar,
                        motivoBloqueo        = body.motivo
                    )
                } else {
                    // Error HTTP → bloqueamos por seguridad
                    uiState = uiState.copy(elegibilidadCargando = false, puedeSolicitar = false,
                        motivoBloqueo = "No se pudo verificar elegibilidad (HTTP ${r.code()}). Intenta más tarde.")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(elegibilidadCargando = false, puedeSolicitar = false,
                    motivoBloqueo = "Sin conexión. Revisa tu internet e intenta de nuevo.")
            }
        }
    }

    fun cargarConfiguracion() {
        viewModelScope.launch {
            try {
                val r = apiService.obtenerConfiguracion()
                if (r.isSuccessful) {
                    val item = r.body()?.configuracion?.firstOrNull() ?: return@launch
                    uiState = uiState.copy(
                        tasaConfigurada      = item.tasaInteres,
                        montoMinimoPermitido = item.montoMinimo,
                        montoMaximoPermitido = item.montoMaximo
                    )
                }
            } catch (e: Exception) {
                Log.w(TAG, "Config no disponible: ${e.localizedMessage}")
            }
        }
    }

    fun onMontoChange(value: String) {
        uiState = uiState.copy(monto = value, resultado = null, error = null)
        calcularCuota(value, uiState.plazoMeses)
    }

    fun onPlazoChange(plazo: Int) {
        uiState = uiState.copy(plazoMeses = plazo, resultado = null, error = null)
        calcularCuota(uiState.monto, plazo)
    }

    private fun calcularCuota(montoStr: String, plazo: Int) {
        val monto = montoStr.toDoubleOrNull() ?: return
        if (monto <= 0 || plazo <= 0) return
        val tasa = uiState.tasaConfigurada
        val cuota = monto * (tasa * Math.pow(1 + tasa, plazo.toDouble())) /
                (Math.pow(1 + tasa, plazo.toDouble()) - 1)
        uiState = uiState.copy(cuotaEstimada = cuota)
    }

    fun enviarSolicitud(idCliente: Int) {
        // Segunda línea de defensa en cliente (el servidor también valida)
        if (!uiState.puedeSolicitar) {
            uiState = uiState.copy(error = "❌ ${uiState.motivoBloqueo ?: "No puedes solicitar un préstamo ahora"}")
            return
        }
        val montoNum = uiState.monto.toDoubleOrNull() ?: 0.0
        when {
            montoNum <= 0 ->
            { uiState = uiState.copy(error = "❌ Ingresa un monto válido"); return }
            montoNum < uiState.montoMinimoPermitido ->
            { uiState = uiState.copy(error = "❌ El monto mínimo es $${uiState.montoMinimoPermitido.toLong()}"); return }
            montoNum > uiState.montoMaximoPermitido ->
            { uiState = uiState.copy(error = "❌ El monto máximo es $${uiState.montoMaximoPermitido.toLong()}"); return }
            uiState.plazoMeses !in plazosDisponibles ->
            { uiState = uiState.copy(error = "❌ Selecciona un plazo válido"); return }
        }
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null, resultado = null)
            try {
                val response = apiService.solicitarCredito(
                    SolicitudCreditoRequest(
                        id_cliente  = idCliente,
                        monto      = montoNum,
                        plazo_meses = uiState.plazoMeses
                    )
                )
                if (response.isSuccessful) {
                    uiState = uiState.copy(
                        isLoading     = false,
                        resultado     = "✅ Solicitud enviada. Un empleado la revisará pronto.",
                        monto         = "",
                        cuotaEstimada = null,
                        puedeSolicitar = false  // Bloquear de inmediato tras enviar
                    )
                } else {
                    val detail = runCatching {
                        org.json.JSONObject(response.errorBody()?.string() ?: "").getString("detail")
                    }.getOrDefault("Error al enviar solicitud")
                    uiState = uiState.copy(isLoading = false, error = "❌ $detail")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "❌ ${e.localizedMessage}")
            }
        }
    }
}