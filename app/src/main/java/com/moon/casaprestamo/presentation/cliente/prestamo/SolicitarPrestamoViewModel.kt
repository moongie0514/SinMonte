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
import org.json.JSONObject
import javax.inject.Inject

private const val TAG = "SOLICITUD_VM"

// ✅ plazoMeses es Int con opciones fijas — ya no String libre
data class SolicitarPrestamoUiState(
    val monto: String = "",
    val plazoMeses: Int = 12,
    val tasaConfigurada: Double = 5.0,
    val montoMaximoPermitido: Double = 50000.0,
    val montoMinimoPermitido: Double = 1000.0,
    val cuotaEstimada: Double? = null,
    val isLoading: Boolean = false,
    val resultado: String? = null,
    val error: String? = null
)

@HiltViewModel
class SolicitarPrestamoViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    // ✅ Plazos válidos según el API (no acepta valores distintos a estos)
    val plazosDisponibles = listOf(6, 12, 24, 36, 48)

    var uiState by mutableStateOf(SolicitarPrestamoUiState())
        private set

    init {
        cargarConfiguracionVigente()
    }

    private fun cargarConfiguracionVigente() {
        viewModelScope.launch {
            try {
                val response = apiService.obtenerConfiguracion()
                if (response.isSuccessful && response.body() != null) {
                    val item = response.body()!!.configuracion.firstOrNull()

                    val tasa = item?.tasaInteres ?: uiState.tasaConfigurada
                    val min = item?.montoMinimo ?: uiState.montoMinimoPermitido
                    val max = item?.montoMaximo ?: uiState.montoMaximoPermitido

                    Log.d(TAG, "Config: tasa=$tasa min=$min max=$max")
                    uiState = uiState.copy(
                        tasaConfigurada = tasa,
                        montoMaximoPermitido = max,
                        montoMinimoPermitido = min
                    )
                } else {
                    Log.w(TAG, "Config no disponible: HTTP ${response.code()} — usando defaults")
                }
            } catch (e: Exception) {
                // Mantiene valores por defecto si falla la red
                Log.w(TAG, "No se pudo cargar configuración: ${e.localizedMessage}")
            }
        }
    }


    fun onMontoChange(value: String) {
        uiState = uiState.copy(monto = value, resultado = null, error = null)
        calcularCuotaEstimada(value, uiState.plazoMeses)
    }

    // ✅ antes: onMesesChange(value: String) — ahora recibe Int de opciones fijas
    fun onPlazoChange(plazo: Int) {
        uiState = uiState.copy(plazoMeses = plazo, resultado = null, error = null)
        calcularCuotaEstimada(uiState.monto, plazo)
    }

    // Cálculo local idéntico al del servidor (amortización francesa, 5% mensual fijo)
    private fun calcularCuotaEstimada(montoStr: String, plazo: Int) {
        val monto = montoStr.toDoubleOrNull() ?: return
        if (monto <= 0 || plazo <= 0) return
        val tasa  = 0.05
        val cuota = monto * (tasa * Math.pow(1 + tasa, plazo.toDouble())) /
                (Math.pow(1 + tasa, plazo.toDouble()) - 1)
        uiState = uiState.copy(cuotaEstimada = cuota)
    }

    fun enviarSolicitud(idCliente: Int) {
        val montoNum = uiState.monto.toDoubleOrNull() ?: 0.0

        // Validaciones
        when {
            montoNum <= 0 -> {
                uiState = uiState.copy(error = "❌ Ingresa un monto válido")
                return
            }
            montoNum < uiState.montoMinimoPermitido -> {
                uiState = uiState.copy(error = "❌ El monto mínimo es $${uiState.montoMinimoPermitido.toLong()}")
                return
            }
            montoNum > uiState.montoMaximoPermitido -> {
                uiState = uiState.copy(error = "❌ El monto máximo es $${uiState.montoMaximoPermitido.toLong()}")
                return
            }
            uiState.plazoMeses !in plazosDisponibles -> {
                uiState = uiState.copy(error = "❌ Selecciona un plazo válido")
                return
            }
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, resultado = null, error = null)
            try {
                // ✅ SolicitudCreditoRequest con plazo_meses (no meses)
                // ✅ apiService.solicitarCredito (no solicitarPrestamo)
                val request = SolicitudCreditoRequest(
                    id_cliente  = idCliente,
                    monto       = montoNum,
                    plazo_meses = uiState.plazoMeses
                )

                Log.d(TAG, "POST /cliente/solicitar_credito → monto: $montoNum, plazo: ${uiState.plazoMeses}")
                val response = apiService.solicitarCredito(request)
                Log.d(TAG, "HTTP ${response.code()}")

                if (response.isSuccessful && response.body()?.status == "success") {
                    uiState = uiState.copy(
                        isLoading     = false,
                        resultado     = "✅ SOLICITUD ENVIADA — Un empleado la revisará pronto.",
                        monto         = "",
                        plazoMeses    = 12,
                        cuotaEstimada = null
                    )
                } else {
                    val errorMsg = try {
                        JSONObject(response.errorBody()?.string() ?: "").optString("detail", "Error al enviar solicitud")
                    } catch (e: Exception) { "Error al enviar solicitud" }

                    Log.e(TAG, "❌ $errorMsg")
                    uiState = uiState.copy(isLoading = false, error = "❌ $errorMsg")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Error de red", e)
                uiState = uiState.copy(
                    isLoading = false,
                    error     = "⚠️ ERROR DE RED: ${e.localizedMessage}"
                )
            }
        }
    }
}