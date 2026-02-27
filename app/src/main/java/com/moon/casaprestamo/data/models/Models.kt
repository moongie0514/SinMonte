package com.moon.casaprestamo.data.models

import com.google.gson.annotations.SerializedName

// ═══════════════════════════════════════════════════════════
// REQUESTS
// ═══════════════════════════════════════════════════════════

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegistroRequest(
    val nombre: String,
    @SerializedName("apellido_paterno") val apellido_paterno: String,
    @SerializedName("apellido_materno") val apellido_materno: String?,
    val email: String,
    val password: String,
    val curp: String?,
    val telefono: String?,
    val direccion: String?,
    @SerializedName("no_identificacion") val no_identificacion: String?
)

data class VerificarEmailRequest(
    val email: String,
    val codigo: String
)

data class VerificarCodigoRequest(
    @SerializedName("email")          val email: String,
    @SerializedName("codigo")         val codigo: String,
    @SerializedName("nueva_password") val nueva_password: String
)

// POST /cliente/solicitar_credito
data class SolicitudCreditoRequest(
    @SerializedName("id_cliente")   val id_cliente: Int,
    @SerializedName("monto")        val monto: Double,
    @SerializedName("plazo_meses")  val plazo_meses: Int          // 6 | 12 | 24 | 36 | 48
)

// PUT /cliente/{id}/perfil
data class ActualizarPerfilRequest(
    val nombre: String? = null,
    @SerializedName("apellido_paterno") val apellido_paterno: String? = null,
    @SerializedName("apellido_materno") val apellido_materno: String? = null,
    val telefono: String? = null,
    val direccion: String? = null
)

data class ConfiguracionRequest(
    val valor: String
)

data class AprobarPrestamoRequest(
    @SerializedName("id_prestamo") val idPrestamo: Int,
    val accion: String,
    @SerializedName("id_empleado") val idEmpleado: Int
)

data class RegistrarPagoRequest(
    @SerializedName("id_pago")     val id_pago: Int,
    @SerializedName("id_empleado") val id_empleado: Int
)

data class CrearEmpleadoRequest(
    val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String? = null,
    val email: String,
    val password: String,
    val telefono: String? = null
)

// ═══════════════════════════════════════════════════════════
// RESPONSES — AUTH
// ═══════════════════════════════════════════════════════════

data class LoginResponse(
    val status: String,
    val message: String? = null,
    val usuario: Usuario? = null,
    val detail: String? = null
)

data class Usuario(
    @SerializedName("id_usuario")       val idUsuario: Int,
    @SerializedName("nombre")           val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String?,
    @SerializedName("email")            val email: String,
    @SerializedName("rol")              val rol: String,           // "Cliente" | "Empleado" | "Admin"
    @SerializedName("curp")             val curp: String?,
    @SerializedName("telefono")         val telefono: String?,
    @SerializedName("email_verificado") val emailVerificado: Boolean
) {
    val nombreCompleto: String
        get() = "$nombre $apellidoPaterno ${apellidoMaterno ?: ""}".trim()
}

data class RegistroResponse(
    val status: String,
    val message: String,
    @SerializedName("requiere_verificacion") val requiereVerificacion: Boolean,
    @SerializedName("id_cliente")            val idCliente: Int,
    val detail: String? = null
)

data class VerificarEmailResponse(
    val status: String,
    val message: String,
    val detail: String? = null
)

data class SolicitarCodigoResponse(
    val status: String,
    val message: String,
    @SerializedName("codigo_debug") val codigo_debug: String? = null,
    val detail: String? = null
)

// ═══════════════════════════════════════════════════════════
// RESPONSES — CLIENTE
// ═══════════════════════════════════════════════════════════

// GET /cliente/{id}/prestamos
data class PrestamosClienteResponse(
    val status: String,
    val total: Int,
    val prestamos: List<PrestamoData>
)

// Modelo de préstamo (actualizado con campos del nuevo API)
data class PrestamoData(
    @SerializedName("id_prestamo")      val idPrestamo: Int,
    @SerializedName("folio")            val folio: String?,          // "MSP-{id}"
    @SerializedName("monto_total")      val montoTotal: Double,
    @SerializedName("saldo_pendiente")  val saldoPendiente: Double,
    @SerializedName("tasa_interes")     val tasaInteres: Double,
    @SerializedName("plazo_meses")      val plazoMeses: Int,
    @SerializedName("estado")           val estado: String,
    @SerializedName("fecha_creacion")   val fechaCreacion: String,
    @SerializedName("fecha_aprobacion") val fechaAprobacion: String?,
    @SerializedName("pagos_realizados") val pagosRealizados: Int = 0,
    @SerializedName("total_pagos")      val totalPagos: Int = 0
)
data class PrestamoConPagos(
    val prestamo: PrestamoData,
    val pagos: List<PagoData>
)
// GET /cliente/{id}/prestamos/{id_prestamo}/pagos
data class CalendarioPagosResponse(
    val status: String,
    @SerializedName("id_prestamo") val idPrestamo: Int,
    @SerializedName("total_pagos") val totalPagos: Int,
    val pagos: List<PagoData>
)

data class PagoData(
    @SerializedName("id_pago")           val idPago: Int,
    @SerializedName("numero_pago")       val numeroPago: Int,
    @SerializedName("monto")             val monto: Double,
    @SerializedName("fecha_vencimiento") val fechaVencimiento: String,
    @SerializedName("fecha_pago")        val fechaPago: String?,
    @SerializedName("estado")            val estado: String          // "pagado" | "pendiente" | "atrasado"
)

// POST /cliente/solicitar_credito
data class SolicitudCreditoResponse(
    val status: String,
    val message: String,
    @SerializedName("id_prestamo")   val idPrestamo: Int? = null,
    @SerializedName("cuota_mensual") val cuotaMensual: Double? = null,
    @SerializedName("plazo_meses")   val plazoMeses: Int? = null,
    @SerializedName("monto")         val monto: Double? = null,
    val detail: String? = null
)

// GET /cliente/{id}/perfil
data class PerfilClienteResponse(
    val status: String,
    val perfil: PerfilCliente
)

data class PerfilCliente(
    @SerializedName("id_usuario")        val idUsuario: Int,
    @SerializedName("nombre")            val nombre: String,
    @SerializedName("apellido_paterno")  val apellidoPaterno: String,
    @SerializedName("apellido_materno")  val apellidoMaterno: String?,
    @SerializedName("email")             val email: String,
    @SerializedName("telefono")          val telefono: String?,
    @SerializedName("curp")              val curp: String?,
    @SerializedName("direccion")         val direccion: String?,
    @SerializedName("no_identificacion") val noIdentificacion: String?,
    @SerializedName("fecha_registro")    val fechaRegistro: String?,
    @SerializedName("email_verificado")  val emailVerificado: Boolean
)

// ═══════════════════════════════════════════════════════════
// RESPONSES — ADMIN / EMPLEADO
// ═══════════════════════════════════════════════════════════

// GET /usuarios y GET /usuario/{id}
data class UsuariosResponse(
    val status: String,
    val total: Int,
    val usuarios: List<UsuarioResumen>
)

data class UsuarioResumen(
    @SerializedName("id_usuario")       val idUsuario: Int,
    @SerializedName("nombre")           val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("email")            val email: String,
    @SerializedName("rol")              val rol: String,
    @SerializedName("activo")           val activo: Boolean,
    @SerializedName("email_verificado") val emailVerificado: Boolean,
    @SerializedName("fecha_registro")   val fechaRegistro: String?
)

data class UsuarioDetalleResponse(
    val status: String,
    val usuario: PerfilCliente
)

data class EstadisticasResponse(
    val status: String? = null,
    @SerializedName("total_clientes")    val totalClientes: Int = 0,
    @SerializedName("prestamos_activos") val prestamosActivos: Int = 0,
    @SerializedName("capital_otorgado")  val capitalOtorgado: Double = 0.0,
    @SerializedName("saldo_pendiente")   val saldoPendiente: Double = 0.0,
    @SerializedName("monto_recuperado")  val montoRecuperado: Double = 0.0
)

data class CrearEmpleadoResponse(
    val status: String,
    val message: String?,
    @SerializedName("id_empleado") val idEmpleado: Int
)

data class ConfiguracionResponse(
    val status: String,
    val configuracion: List<ConfiguracionItem>
)

data class ConfiguracionItem(
    @SerializedName("id_config") val idConfig: Int,
    val clave: String,
    val valor: String
)

// ═══════════════════════════════════════════════════════════
// RESPONSES — PAGOS / TICKETS (empleado)
// ═══════════════════════════════════════════════════════════

data class PagoPendiente(
    @SerializedName("id_pago")           val id_pago: Int,
    @SerializedName("id_prestamo")       val id_prestamo: Int,
    @SerializedName("numero_pago")       val numero_pago: Int,
    @SerializedName("fecha_vencimiento") val fecha_vencimiento: String,
    @SerializedName("monto")             val monto: Double,
    @SerializedName("estado")            val estado: String? = null,
    @SerializedName("monto_total")       val monto_total: Double? = null,
    val folio: String? = null,
    @SerializedName("nombre_cliente")    val nombre_cliente: String? = null,
    val nombre: String? = null,
    @SerializedName("apellido_paterno")  val apellidoPaterno: String? = null,
    val telefono: String? = null
) {
    val nombreClienteUi: String
        get() = when {
            !nombre_cliente.isNullOrBlank() -> nombre_cliente.trim()
            !nombre.isNullOrBlank() -> listOf(nombre, apellidoPaterno).filterNot { it.isNullOrBlank() }.joinToString(" ").trim()
            else -> "CLIENTE"
        }
}

data class RegistrarPagoResponse(
    val status: String,
    val message: String,
    val monto: Double,
    @SerializedName("id_prestamo") val idPrestamo: Int
)

data class CorteCajaResponse(
    val status: String,
    val fecha: String,
    @SerializedName("total_pagos")   val totalPagos: Int,
    @SerializedName("total_cobrado") val totalCobrado: Double,
    val movimientos: List<TicketDetalle>
)

data class TicketDetalle(
    @SerializedName("id_ticket")       val idTicket: Int,
    val folio: String,
    @SerializedName("monto_pagado")    val montoPagado: Double,
    @SerializedName("fecha_generacion") val fechaGeneracion: String,
    @SerializedName("metodo_pago")     val metodoPago: String,
    val tipo: String,
    @SerializedName("folio_prestamo")  val folioPrestamo: String,
    val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String
)

data class TicketCompleto(
    val status: String,
    val ticket: TicketPrestamoDetalle
)

data class TicketPrestamoDetalle(
    @SerializedName("id_prestamo")     val idPrestamo: Int,
    val folio: String,
    @SerializedName("monto_total")     val montoTotal: Double,
    @SerializedName("saldo_pendiente") val saldoPendiente: Double,
    @SerializedName("tasa_interes")    val tasaInteres: Double,
    @SerializedName("plazo_meses")     val plazoMeses: Int,
    val estado: String,
    @SerializedName("fecha_creacion")  val fechaCreacion: String,
    @SerializedName("fecha_aprobacion") val fechaAprobacion: String?,
    val nombre: String,
    @SerializedName("apellido_paterno") val apellidoPaterno: String,
    @SerializedName("apellido_materno") val apellidoMaterno: String?,
    val curp: String?,
    val telefono: String?,
    val email: String,
    @SerializedName("pagos_realizados") val pagosRealizados: Int,
    @SerializedName("total_pagos") val totalPagos: Int,
    val pagos: List<PagoData>
)

// ═══════════════════════════════════════════════════════════
// GENÉRICO
// ═══════════════════════════════════════════════════════════

data class GenericResponse(
    val status: String,
    val message: String? = null,
    val detail: String? = null
)
