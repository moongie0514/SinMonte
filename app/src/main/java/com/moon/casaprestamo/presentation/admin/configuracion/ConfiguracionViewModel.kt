package com.moon.casaprestamo.presentation.admin.configuracion

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.*
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigAdminViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    var uiState by mutableStateOf(ConfigAdminUiState())
        private set

    private var configId: Int = 1

    init {
        cargarConfiguracion()
    }

    fun cargarConfiguracion() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = apiService.obtenerConfiguracion()

                if (response.isSuccessful && response.body() != null) {
                    val config = response.body()!!
                    val items = config.configuracion

                    fun valorClave(clave: String): String =
                        items.firstOrNull { it.clave.equals(clave, ignoreCase = true) }?.valor ?: ""

                    configId = items.firstOrNull { it.clave.equals("tasa_interes", ignoreCase = true) }?.idConfig
                        ?: items.firstOrNull()?.idConfig
                                ?: 1

                    val tasa = valorClave("tasa_interes")
                    val plazo = valorClave("plazo_maximo")
                    val min = valorClave("monto_minimo")
                    val max = valorClave("monto_maximo")

                    Log.d("CONFIG_VM", "Configuración cargada: tasa=$tasa plazo=$plazo min=$min max=$max")
                    uiState = uiState.copy(
                        tasaInteres = tasa,
                        plazoMaximo = plazo,
                        montoMinimo = min,
                        montoMaximo = max,
                        isLoading = false
                    )
                } else {
                    Log.e("CONFIG_VM", "Error al cargar: ${response.code()}")
                    uiState = uiState.copy(
                        isLoading = false,
                        mensaje = "Error: ${response.code()}",
                        esError = true
                    )
                }
            } catch (e: Exception) {
                Log.e("CONFIG_VM", "Error de red", e)
                uiState = uiState.copy(
                    isLoading = false,
                    mensaje = "Sin conexión: ${e.localizedMessage}",
                    esError = true
                )
            }
        }
    }

    fun guardarCambios() {
        val tasa = uiState.tasaInteres.toDoubleOrNull()
        val plazo = uiState.plazoMaximo.toIntOrNull()
        val minimo = uiState.montoMinimo.toDoubleOrNull()
        val maximo = uiState.montoMaximo.toDoubleOrNull()

        if (tasa == null || plazo == null || minimo == null || maximo == null) {
            uiState = uiState.copy(
                mensaje = "Valores inválidos",
                esError = true
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, mensaje = null)

            try {
                val updates = listOf(
                    configId to ConfiguracionRequest(valor = tasa.toString()),
                    (configId + 1) to ConfiguracionRequest(valor = plazo.toString()),
                    (configId + 2) to ConfiguracionRequest(valor = minimo.toString()),
                    (configId + 3) to ConfiguracionRequest(valor = maximo.toString())
                )
                updates.forEach { (id, req) ->
                    apiService.actualizarConfiguracion(id, req)
                }

                Log.d("CONFIG_VM", "✅ Configuración guardada")
                uiState = uiState.copy(
                    isLoading = false,
                    mensaje = "✅ CONFIGURACIÓN ACTUALIZADA",
                    esError = false
                )
            } catch (e: Exception) {
                Log.e("CONFIG_VM", "💥 Error", e)
                uiState = uiState.copy(
                    isLoading = false,
                    mensaje = "Error al guardar configuración",
                    esError = true
                )
            }
        }
    }

    fun onTasaChange(v: String) {
        uiState = uiState.copy(tasaInteres = v, mensaje = null)
    }

    fun onPlazoChange(v: String) {
        uiState = uiState.copy(plazoMaximo = v, mensaje = null)
    }

    fun onMinimoChange(v: String) {
        uiState = uiState.copy(montoMinimo = v, mensaje = null)
    }

    fun onMaximoChange(v: String) {
        uiState = uiState.copy(montoMaximo = v, mensaje = null)
    }
}