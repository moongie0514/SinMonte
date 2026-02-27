package com.moon.casaprestamo.presentation.admin.supervision

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moon.casaprestamo.data.models.AdminSupervisionUiState
import com.moon.casaprestamo.data.models.AprobarPrestamoRequest
import com.moon.casaprestamo.data.models.PrestamoSupervision
import com.moon.casaprestamo.data.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SUPERVISION_VM"

@HiltViewModel
class SupervisionViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSupervisionUiState())
    val uiState: StateFlow<AdminSupervisionUiState> = _uiState.asStateFlow()

    init {
        cargarPrestamos()
    }

    fun cargarPrestamos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                Log.d(TAG, "=== CARGANDO CLIENTES ===")

                val responseClientes = apiService.obtenerUsuarios(rol = "Cliente")

                if (!responseClientes.isSuccessful) {
                    Log.e(TAG, "Error clientes: ${responseClientes.code()}")
                    _uiState.update {
                        it.copy(isLoading = false, mensaje = "Error al cargar clientes")
                    }
                    return@launch
                }

                val clientes = responseClientes.body()?.usuarios ?: emptyList()
                Log.d(TAG, "Clientes: ${clientes.size}")

                val pendientes = mutableListOf<PrestamoSupervision>()

                clientes.forEach { cliente ->
                    try {
                        val resp = apiService.obtenerPrestamosCliente(cliente.idUsuario)
                        if (resp.isSuccessful) {
                            resp.body()
                                ?.filter { it.estado == "PENDIENTE" }
                                ?.forEach { p ->
                                    pendientes.add(
                                        PrestamoSupervision(
                                            idPrestamo    = p.idPrestamo,
                                            nombreCliente = "${cliente.nombre} ${cliente.apellidoPaterno}",
                                            curp          = cliente.email,
                                            fechaCreacion = p.fechaCreacion,
                                            montoTotal    = p.montoTotal,
                                            plazoMeses    = p.plazoMeses,
                                            estado        = p.estado
                                        )
                                    )
                                }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Error cliente ${cliente.idUsuario}: ${e.localizedMessage}")
                    }
                }

                Log.d(TAG, "Pendientes: ${pendientes.size}")
                _uiState.update {
                    it.copy(prestamos = pendientes, isLoading = false, mensaje = null)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error: ${e.localizedMessage}")
                _uiState.update {
                    it.copy(isLoading = false, mensaje = "Error de red: ${e.localizedMessage}")
                }
            }
        }
    }

    fun aprobarPrestamo(id: Int) {
        procesarPrestamo(id, true)
    }

    fun rechazarPrestamo(id: Int) {
        procesarPrestamo(id, false)
    }

    private fun procesarPrestamo(id: Int, esAprobado: Boolean) {
        viewModelScope.launch {
            try {
                val response = apiService.procesarPrestamo(
                    AprobarPrestamoRequest(idPrestamo = id, accion = if (esAprobado) "aprobar" else "rechazar")
                )
                if (response.isSuccessful) {
                    val msg = if (esAprobado) "Prestamo aprobado" else "Prestamo rechazado"
                    _uiState.update { it.copy(mensaje = msg) }
                    cargarPrestamos()
                } else {
                    _uiState.update { it.copy(mensaje = "Error: ${response.code()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(mensaje = "Error: ${e.localizedMessage}") }
            }
        }
    }
}