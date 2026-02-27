package com.moon.casaprestamo.presentation.admin.reportes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.AdminReportesUiState
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReportesUiState(isLoading = true))
    val uiState: StateFlow<AdminReportesUiState> = _uiState.asStateFlow()

    init {
        cargarEstadisticas()
    }

    fun cargarEstadisticas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                Log.d("REPORTES_VM", "=== CARGANDO ESTADÍSTICAS ===")

                val response = apiService.obtenerEstadisticas()

                Log.d("REPORTES_VM", "Response code: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val stats = response.body()!!

                    Log.d("REPORTES_VM", "✅ Estadísticas obtenidas:")
                    Log.d("REPORTES_VM", "  - Total Clientes: ${stats.totalClientes}")
                    Log.d("REPORTES_VM", "  - Préstamos Activos: ${stats.prestamosActivos}")
                    Log.d("REPORTES_VM", "  - Morosos: N/D (no enviado por API)")
                    Log.d("REPORTES_VM", "  - Capital Activo: ${stats.capitalOtorgado}")
                    Log.d("REPORTES_VM", "  - Recuperado: ${stats.montoRecuperado}")

                    _uiState.update {
                        it.copy(
                            capitalActivo = stats.capitalOtorgado,
                            capitalRecuperado = stats.montoRecuperado,
                            saldoPendiente = stats.saldoPendiente,
                            prestamosActivos = stats.prestamosActivos,
                            prestamosPendientes = 0,
                            prestamosEnMora = 0,
                            totalClientes = stats.totalClientes,
                            isLoading = false
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("REPORTES_VM", "❌ Error HTTP: ${response.code()}")
                    Log.e("REPORTES_VM", "Body: $errorBody")

                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                Log.e("REPORTES_VM", "💥 EXCEPTION al cargar estadísticas", e)
                Log.e("REPORTES_VM", "Tipo: ${e.javaClass.simpleName}")
                Log.e("REPORTES_VM", "Mensaje: ${e.localizedMessage}")

                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}