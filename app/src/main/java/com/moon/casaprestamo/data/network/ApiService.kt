package com.moon.casaprestamo.data.network

import com.moon.casaprestamo.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ──────────────────────────────────────────────────────────

    // 1. Login unificado (todos los roles)
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // 2. Registro cliente → envía email verificación
    @POST("registrar_cliente")
    suspend fun registrarCliente(@Body request: RegistroRequest): Response<RegistroResponse>

    // 3. Verificar email con código
    @POST("verificar_email")
    suspend fun verificarEmail(@Body request: VerificarEmailRequest): Response<VerificarEmailResponse>

    // 4. Reenviar código de verificación
    @POST("reenviar_codigo_verificacion")
    suspend fun reenviarCodigoVerificacion(
        @Query("email") email: String
    ): Response<GenericResponse>

    // 5. Solicitar código recuperación de contraseña
    @POST("solicitar_codigo")
    suspend fun solicitarCodigo(
        @Query("email") email: String
    ): Response<SolicitarCodigoResponse>

    // 6. Verificar código y cambiar contraseña
    @POST("verificar_codigo")
    suspend fun verificarCodigo(@Body request: VerificarCodigoRequest): Response<GenericResponse>

    // ── CLIENTE ───────────────────────────────────────────────────────

    // 7. Préstamos del cliente
    @GET("cliente/{id_cliente}/prestamos")
    suspend fun obtenerPrestamosCliente(
        @Path("id_cliente") idCliente: Int
    ): Response<PrestamosClienteResponse>

    // 8. Calendario de pagos de un préstamo
    @GET("cliente/{id_cliente}/prestamos/{id_prestamo}/pagos")
    suspend fun obtenerCalendarioPagos(
        @Path("id_cliente")   idCliente: Int,
        @Path("id_prestamo")  idPrestamo: Int
    ): Response<CalendarioPagosResponse>

    // 9. Solicitar nuevo crédito
    @POST("cliente/solicitar_credito")
    suspend fun solicitarCredito(
        @Body request: SolicitudCreditoRequest
    ): Response<SolicitudCreditoResponse>

    // 10. Perfil del cliente (GET)
    @GET("cliente/{id_cliente}/perfil")
    suspend fun obtenerPerfil(
        @Path("id_cliente") idCliente: Int
    ): Response<PerfilClienteResponse>

    // 11. Actualizar perfil (PUT)
    @PUT("cliente/{id_cliente}/perfil")
    suspend fun actualizarPerfil(
        @Path("id_cliente") idCliente: Int,
        @Body request: ActualizarPerfilRequest
    ): Response<GenericResponse>

    // ── ADMIN ─────────────────────────────────────────────────────────

    // 12. Lista de usuarios (filtrable por rol)
    @GET("usuarios")
    suspend fun obtenerUsuarios(
        @Query("rol") rol: String? = null
    ): Response<UsuariosResponse>

    // 13. Usuario específico
    @GET("usuario/{id_usuario}")
    suspend fun obtenerUsuario(
        @Path("id_usuario") idUsuario: Int
    ): Response<UsuarioDetalleResponse>

    // 14. Aprobar / rechazar préstamo
    @POST("admin/aprobar_prestamo")
    suspend fun procesarPrestamo(@Body request: AprobarPrestamoRequest): Response<GenericResponse>

    // 15. Estadísticas
    @GET("admin/estadisticas")
    suspend fun obtenerEstadisticas(): Response<EstadisticasResponse>

    // 16. Crear empleado
    @POST("admin/crear_empleado")
    suspend fun crearEmpleado(@Body request: CrearEmpleadoRequest): Response<CrearEmpleadoResponse>

    // 17. Configuración (GET)
    @GET("configuracion_sistema")
    suspend fun obtenerConfiguracion(): Response<ConfiguracionResponse>

    // 18. Configuración (PUT)
    @PUT("configuracion_sistema/{id_config}")
    suspend fun actualizarConfiguracion(
        @Path("id_config") idConfig: Int,
        @Body config: ConfiguracionRequest
    ): Response<GenericResponse>

    // ── EMPLEADO ──────────────────────────────────────────────────────

    // 19. Registrar pago y generar ticket
    @POST("empleado/registrar_pago")
    suspend fun registrarPago(@Body request: RegistrarPagoRequest): Response<RegistrarPagoResponse>

    // 20. Pagos pendientes del día
    @GET("empleado/pagos_pendientes")
    suspend fun obtenerPagosPendientes(): Response<List<PagoPendiente>>

    // 21. Corte de caja
    @GET("empleado/corte_caja")
    suspend fun obtenerCorteCaja(
        @Query("id_empleado") idEmpleado: Int,
        @Query("fecha")       fecha: String? = null
    ): Response<CorteCajaResponse>

    // 22. Buscar ticket por folio
    @GET("tickets/{folio}")
    suspend fun buscarTicket(@Path("folio") folio: String): Response<TicketCompleto>
}