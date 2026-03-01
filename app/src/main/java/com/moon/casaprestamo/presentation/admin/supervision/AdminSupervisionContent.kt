package com.moon.casaprestamo.presentation.admin.supervision

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.moon.casaprestamo.data.models.PrestamoPendienteAdmin
import com.moon.casaprestamo.data.models.TicketDetalle
import com.moon.casaprestamo.data.models.TicketPrestamoDetalle
import com.moon.casaprestamo.presentation.admin.supervision.components.BarraSuperior
import com.moon.casaprestamo.presentation.admin.supervision.components.TabCartera
import com.moon.casaprestamo.presentation.admin.supervision.components.TabFolios
import com.moon.casaprestamo.presentation.admin.supervision.components.TabSolicitudes
import com.moon.casaprestamo.presentation.cliente.cartera.PagoRowPrototipo
import com.moon.casaprestamo.presentation.cliente.cartera.SpecificCard
import java.text.SimpleDateFormat
import java.util.*

internal val Rojo     = Color(0xFFA6032F)
internal val Oscuro   = Color(0xFF0F172A)
internal val Verde    = Color(0xFF10B981)
internal val Amarillo = Color(0xFFF59E0B)

// Convierte "yyyy-MM-dd" a millis (medianoche UTC) para comparar
internal fun fechaStringAMillis(fecha: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(fecha)?.time
    } catch (e: Exception) { null }
}

// Convierte "dd/MM/yyyy" (formato display) a "yyyy-MM-dd" (formato API)
internal fun displayAApiDate(display: String): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val api = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        api.format(sdf.parse(display)!!)
    } catch (e: Exception) { display }
}

internal fun milisAFecha(milis: Long?): String {
    if (milis == null) return ""
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(milis))
}

// ═════════════════════════════════════════════════════════════
// CONTENT PRINCIPAL
// ═════════════════════════════════════════════════════════════

@Composable
fun AdminSupervisionContent(
    uiState: SupervisionUiState,
    idAprobador: Int,
    onSetTab: (SupervisionTab) -> Unit,
    onSetFechas: (String, String) -> Unit,
    onCargarFolios: (String?) -> Unit,
    onAbrirEstadoCuenta: (String) -> Unit,
    onCerrarEstadoCuenta: () -> Unit,
    onAbrirSolicitud: (PrestamoPendienteAdmin) -> Unit,
    onCerrarSolicitud: () -> Unit,
    onAprobar: (Int) -> Unit,
    onRechazar: (Int) -> Unit,
    onLimpiarMensaje: () -> Unit,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.fillMaxSize().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BarraSuperior(
            recaudacion           = if (uiState.tab == SupervisionTab.FOLIOS) uiState.recaudacionFolios else uiState.recaudacionCartera,
            recaudacionCargando   = uiState.tab == SupervisionTab.FOLIOS && uiState.foliosLoading,
            solicitudesPendientes = uiState.solicitudesPendientes,
            tabActual             = uiState.tab,
            fechaDesde            = uiState.fechaDesde,
            fechaHasta            = uiState.fechaHasta,
            onLoad                = onSetFechas,
            onClickRecaudacion    = { onSetTab(SupervisionTab.CARTERA) },
            onClickSolicitudes    = { onSetTab(SupervisionTab.SOLICITUDES) },
            onFechasChange        = onSetFechas,
        )

        when (uiState.tab) {
            SupervisionTab.CARTERA -> TabCartera(
                uiState   = uiState,
                onSwitch  = { onSetTab(SupervisionTab.FOLIOS) },
                onDetalle = onAbrirEstadoCuenta
            )
            SupervisionTab.FOLIOS -> TabFolios(
                uiState        = uiState,
                onSwitch       = { onSetTab(SupervisionTab.CARTERA) },
                onCargarFolios = onCargarFolios,
                onClickFolio   = onAbrirEstadoCuenta
            )
            SupervisionTab.SOLICITUDES -> TabSolicitudes(
                uiState        = uiState,
                onVolver       = { onSetTab(SupervisionTab.CARTERA) },
                onAbrirDetalle = onAbrirSolicitud
            )
        }

        uiState.mensaje?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                onLimpiarMensaje()
            }
            Surface(
                color    = if (msg.contains("✅")) Verde.copy(0.12f) else Rojo.copy(0.12f),
                shape    = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    msg, modifier = Modifier.padding(12.dp),
                    color = if (msg.contains("✅")) Verde else Rojo,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Modales
    if (uiState.estadoCuentaLoading) {
        Dialog(onDismissRequest = onCerrarEstadoCuenta) {
            Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Rojo)
            }
        }
    }
    uiState.estadoCuenta?.let {
        EstadoDeCuentaModal(detalle = it, onDismiss = onCerrarEstadoCuenta)
    }
    uiState.solicitudDetalle?.let { sol ->
        DetalleSolicitudModal(
            prestamo   = sol,
            onDismiss  = onCerrarSolicitud,
            onAprobar  = { onAprobar(sol.idPrestamo) },
            onRechazar = { onRechazar(sol.idPrestamo) }
        )
    }
}

// ═════════════════════════════════════════════════════════════
// BARRA SUPERIOR
// ═════════════════════════════════════════════════════════════

@Composable
private fun BarraSuperior(
    recaudacion: Double,
    recaudacionCargando: Boolean,
    solicitudesPendientes: Int,
    tabActual: SupervisionTab,
    fechaDesde: String,
    fechaHasta: String,
    onLoad: (String, String) -> Unit,
    onClickRecaudacion: () -> Unit,
    onClickSolicitudes: () -> Unit,
    onFechasChange: (String, String) -> Unit
) {
    var showSelectorFechas by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { onLoad("", "") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // FILA 1: KPIs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiCard(
                titulo     = "RECAUDACIÓN",
                valor      = if (recaudacionCargando) "CARGANDO..." else "\$${String.format("%,.0f", recaudacion)}",
                icono      = Icons.Default.AttachMoney,
                color      = Verde,
                isSelected = tabActual != SupervisionTab.SOLICITUDES,
                onClick    = onClickRecaudacion,
                modifier   = Modifier.weight(1f)
            )
            KpiCard(
                titulo     = "SOLICITUDES",
                valor      = "$solicitudesPendientes NUEVAS",
                icono      = Icons.Default.Schedule,
                color      = Amarillo,
                isSelected = tabActual == SupervisionTab.SOLICITUDES,
                onClick    = onClickSolicitudes,
                modifier   = Modifier.weight(1f)
            )
        }

        // FILA 2: Botón selector de fechas
        OutlinedButton(
            onClick  = { showSelectorFechas = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape    = RoundedCornerShape(12.dp),
            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(
                if (fechaDesde.isNotBlank() && fechaHasta.isNotBlank())
                    "Del $fechaDesde al $fechaHasta"
                else
                    "Filtrar por rango de fechas",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (fechaDesde.isNotBlank()) {
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.Close, null, Modifier.size(16.dp).clickable {
                        onFechasChange("", "")
                    },
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }

    // FIX 6: Selector de fechas limpio — dos DatePicker simples
    if (showSelectorFechas) {
        SelectorFechasDialog(
            inicialDesde = fechaDesde,
            inicialHasta = fechaHasta,
            onDismiss    = { showSelectorFechas = false },
            onConfirmar  = { desde, hasta ->
                onFechasChange(desde, hasta)
                showSelectorFechas = false
            }
        )
    }
}

@Composable
private fun KpiCard(
    titulo: String,
    valor: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(16.dp),
        color    = if (isSelected) color.copy(0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
        border   = if (isSelected) BorderStroke(1.5.dp, color) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(Modifier.size(32.dp), CircleShape, color.copy(0.15f)) {
                Icon(icono, null, tint = color, modifier = Modifier.padding(6.dp))
            }
            Column {
                Text(titulo, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color, letterSpacing = 0.5.sp)
                Text(valor, fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// FIX 6: SELECTOR DE FECHAS — dos campos simples sin DateRangePicker
// ═════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorFechasDialog(
    inicialDesde: String,
    inicialHasta: String,
    onDismiss: () -> Unit,
    onConfirmar: (String, String) -> Unit
) {
    // Usamos dos DatePickerState independientes
    val hoy = Calendar.getInstance()

    fun parseMillis(display: String): Long? {
        if (display.isBlank()) return null
        return try {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(display)?.time
        } catch (e: Exception) { null }
    }

    val stateDesde = rememberDatePickerState(
        initialSelectedDateMillis = parseMillis(inicialDesde) ?: hoy.timeInMillis
    )
    val stateHasta = rememberDatePickerState(
        initialSelectedDateMillis = parseMillis(inicialHasta) ?: hoy.timeInMillis
    )

    var paso by remember { mutableStateOf(0) } // 0 = elegir desde, 1 = elegir hasta

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape    = RoundedCornerShape(24.dp),
            color    = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column {
                // Indicador de paso
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (paso == 0) "Fecha de inicio" else "Fecha de fin",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            if (paso == 0) "Selecciona desde cuándo" else "Selecciona hasta cuándo",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    // Chips visuales de paso
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0, 1).forEach { i ->
                            Surface(
                                shape = CircleShape,
                                color = if (i == paso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                    }
                }

                // Resumen de selección actual
                if (stateDesde.selectedDateMillis != null || stateHasta.selectedDateMillis != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ResumenFechaChip(
                            label  = "Desde",
                            fecha  = milisAFecha(stateDesde.selectedDateMillis),
                            activo = paso == 0,
                            onClick = { paso = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        ResumenFechaChip(
                            label  = "Hasta",
                            fecha  = milisAFecha(stateHasta.selectedDateMillis),
                            activo = paso == 1,
                            onClick = { paso = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // DatePicker sin título extra
                DatePicker(
                    state    = if (paso == 0) stateDesde else stateHasta,
                    showModeToggle = false,
                    title    = null,
                    headline = null,
                    modifier = Modifier.fillMaxWidth()
                )

                // Botones
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick  = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text("CANCELAR") }

                    if (paso == 0) {
                        Button(
                            onClick  = { paso = 1 },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            enabled  = stateDesde.selectedDateMillis != null
                        ) { Text("SIGUIENTE") }
                    } else {
                        Button(
                            onClick = {
                                val desde = milisAFecha(stateDesde.selectedDateMillis)
                                val hasta = milisAFecha(stateHasta.selectedDateMillis)
                                onConfirmar(desde, hasta)
                            },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(12.dp),
                            enabled  = stateHasta.selectedDateMillis != null
                        ) { Text("APLICAR") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenFechaChip(
    label: String,
    fecha: String,
    activo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(10.dp),
        color    = if (activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        border   = if (activo) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
            Text(fecha.ifBlank { "—" }, fontSize = 13.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ═════════════════════════════════════════════════════════════
// TAB CARTERA ACTIVA
// ═════════════════════════════════════════════════════════════

@Composable
private fun TabCartera(
    uiState: SupervisionUiState,
    onSwitch: () -> Unit,
    onDetalle: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // FIX 5: filtro por rango de fechas además del query de texto
    val filtrados = uiState.cartera.filter { item ->
        val pasaQuery = query.isBlank() || listOf(item.nombreCliente, item.curp, item.folio)
            .joinToString(" ").contains(query, ignoreCase = true)

        val pasaFechas = run {
            val desde = uiState.fechaDesde.let { if (it.isBlank()) null else fechaStringAMillis(displayAApiDate(it)) }
            val hasta = uiState.fechaHasta.let { if (it.isBlank()) null else fechaStringAMillis(displayAApiDate(it))?.plus(86_400_000L) }
            val fechaItem = fechaStringAMillis(item.fechaAprobacion)
            when {
                desde == null && hasta == null -> true
                fechaItem == null -> true
                desde != null && hasta != null -> fechaItem in desde..hasta
                desde != null -> fechaItem >= desde
                hasta != null -> fechaItem <= hasta
                else -> true
            }
        }
        pasaQuery && pasaFechas
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Oscuro) {
                    Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("CARTERA ACTIVA", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("SUPERVISIÓN DE CRÉDITOS VIGENTES", fontSize = 9.sp, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = onSwitch) {
                    Icon(Icons.Default.SwapHoriz, "Folios")
                }
            }

            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Filtrar resultados por nombre o ID...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true, shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor    = Color.Transparent, focusedBorderColor = Rojo
                )
            )

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.carteraLoading -> LoadBox()
                    filtrados.isEmpty()    -> EmptyBox("Sin registros activos")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // FIX 3: columnas correctas con espacio entre ellas
                                TH("CLIENTE",  Modifier.weight(1.4f))
                                Spacer(Modifier.width(8.dp))
                                TH("FECHA",    Modifier.weight(0.8f))
                                Spacer(Modifier.width(8.dp))
                                TH("MONTO",    Modifier.weight(0.9f))
                                Spacer(Modifier.width(8.dp))
                                TH("ESTADO",   Modifier.weight(0.9f))
                            }
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idPrestamo }) { item ->
                            CarteraRow(item = item, onDetalle = { onDetalle(item.folio) })
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

// FIX 3: CarteraRow con columnas corregidas
@Composable
private fun CarteraRow(item: CarteraAdminItem, onDetalle: () -> Unit) {
    // Nombre en líneas separadas para que no se comprima
    val nombreLinea1 = "${item.nombreCliente.split(" ").take(2).joinToString(" ")}"
    val nombreLinea2 = item.nombreCliente.split(" ").drop(2).joinToString(" ")

    // Fecha con salto antes del último guion
    val fechaRaw = item.fechaAprobacion.take(10)
    val fechaFormateada = if (fechaRaw.contains("-")) {
        val last = fechaRaw.lastIndexOf("-")
        fechaRaw.substring(0, last) + "\n" + fechaRaw.substring(last)
    } else fechaRaw

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetalle() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna CLIENTE: nombre + folio
        Column(modifier = Modifier.weight(1.4f)) {
            Text(
                nombreLinea1,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
            if (nombreLinea2.isNotBlank()) {
                Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            }
            Text(
                item.folio,
                fontSize = 10.sp,
                color = Rojo,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.width(8.dp))

        // Columna FECHA
        Text(
            fechaFormateada,
            modifier = Modifier.weight(0.8f),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.width(8.dp))

        // Columna MONTO
        Text(
            "\$${String.format("%,.0f", item.montoTotal)}",
            modifier = Modifier.weight(0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.width(8.dp))

        // FIX 3: ESTADO real desde railway (ACTIVO / MORA / LIQUIDADO)
        Box(modifier = Modifier.weight(0.9f)) {
            val (bgColor, textColor, label) = when (item.estado.uppercase()) {
                "ACTIVO"    -> Triple(Verde.copy(0.12f),   Verde,   "ACTIVO")
                "MORA"      -> Triple(Rojo.copy(0.12f),    Rojo,    "EN MORA")
                "LIQUIDADO" -> Triple(Color(0xFF3B82F6).copy(0.12f), Color(0xFF3B82F6), "LIQUIDADO")
                else        -> Triple(MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.colorScheme.outline, item.estado.uppercase())
            }
            Surface(color = bgColor, shape = RoundedCornerShape(50)) {
                Text(
                    label,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    maxLines = 1
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// TAB LIBRO DE FOLIOS
// ═════════════════════════════════════════════════════════════

@Composable
private fun TabFolios(
    uiState: SupervisionUiState,
    onSwitch: () -> Unit,
    onCargarFolios: (String?) -> Unit,
    onClickFolio: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }

    // FIX 4: cargar todos los folios al entrar, sin requerir fecha
    LaunchedEffect(Unit) { onCargarFolios(null) }

    // FIX 5: filtro por rango de fechas en folios
    val filtrados = uiState.folios.filter { ticket ->
        val pasaQuery = query.isBlank() || listOf(ticket.folio, ticket.folioPrestamo, ticket.nombre, ticket.apellidoPaterno)
            .joinToString(" ").contains(query, ignoreCase = true)

        val pasaFechas = run {
            val desde = uiState.fechaDesde.let { if (it.isBlank()) null else fechaStringAMillis(displayAApiDate(it)) }
            val hasta = uiState.fechaHasta.let { if (it.isBlank()) null else fechaStringAMillis(displayAApiDate(it))?.plus(86_400_000L) }
            val fechaTicket = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(ticket.fechaGeneracion.take(10))?.time
            } catch (e: Exception) { null }
            when {
                desde == null && hasta == null -> true
                fechaTicket == null -> true
                desde != null && hasta != null -> fechaTicket in desde..hasta
                desde != null -> fechaTicket >= desde
                hasta != null -> fechaTicket <= hasta
                else -> true
            }
        }
        pasaQuery && pasaFechas
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 380.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Rojo) {
                    Icon(Icons.Default.Receipt, null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("LIBRO DE FOLIOS", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("REGISTRO CRONOLÓGICO DE PAGOS", fontSize = 9.sp, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.outline)
                }
                IconButton(onClick = onSwitch) {
                    Icon(Icons.Default.SwapHoriz, "Cartera")
                }
            }
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                placeholder = { Text("Filtrar resultados por nombre o ID...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                singleLine = true, shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedBorderColor    = Color.Transparent, focusedBorderColor = Rojo
                )
            )

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.foliosLoading -> LoadBox()
                    filtrados.isEmpty()   -> EmptyBox("Sin folios registrados")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        stickyHeader {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TH("CLIENTE", Modifier.weight(1.4f))
                                Spacer(Modifier.width(8.dp))
                                TH("FECHA", Modifier.weight(0.8f))
                                Spacer(Modifier.width(8.dp))
                                TH("IMPORTE", Modifier.weight(0.9f))
                                Spacer(Modifier.width(8.dp))
                                TH("ESTADO", Modifier.weight(0.9f))
                            }
                            HorizontalDivider()
                        }
                        items(filtrados, key = { it.idTicket }) { ticket ->
                            FolioRow(ticket = ticket, onClickFolio = { onClickFolio(ticket.folioPrestamo) })
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolioRow(ticket: TicketDetalle, onClickFolio: () -> Unit) {
    val nombreCompleto = listOf(ticket.nombre, ticket.apellidoPaterno).joinToString(" ").trim()
    val nombreLinea1 = nombreCompleto.split(" ").take(2).joinToString(" ")
    val nombreLinea2 = nombreCompleto.split(" ").drop(2).joinToString(" ")

    val fechaRaw = ticket.fechaGeneracion.take(10)
    val fechaFormateada = if (fechaRaw.contains("-")) {
        val last = fechaRaw.lastIndexOf("-")
        fechaRaw.substring(0, last) + "\n" + fechaRaw.substring(last)
    } else fechaRaw

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClickFolio() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.4f)) {
            Text(nombreLinea1, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            if (nombreLinea2.isNotBlank()) {
                Text(nombreLinea2, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 14.sp)
            }
            Text(
                ticket.folio,
                color = Rojo,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            fechaFormateada,
            modifier = Modifier.weight(0.8f),
            fontSize = 11.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(Modifier.width(8.dp))

        Text(
            "\$${String.format("%,.0f", ticket.montoPagado)}",
            modifier = Modifier.weight(0.9f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Black
        )

        Spacer(Modifier.width(8.dp))

        Box(modifier = Modifier.weight(0.9f)) {
            Surface(color = Rojo.copy(0.12f), shape = RoundedCornerShape(50)) {
                Text(
                    "PAGADO",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Rojo,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TabSolicitudes(
    uiState: SupervisionUiState,
    onVolver: () -> Unit,
    onAbrirDetalle: (PrestamoPendienteAdmin) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtrados = uiState.solicitudes.filter {
        query.isBlank() || listOfNotNull(it.nombre, it.apellidoPaterno, it.curp, it.folio)
            .joinToString(" ").contains(query, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Surface(Modifier.size(44.dp), RoundedCornerShape(14.dp), Amarillo) {
                Icon(Icons.Default.Schedule, null, tint = Color.White, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("APROBACIÓN DE CRÉDITOS", fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text("TRÁMITES DE CLIENTES NUEVOS", fontSize = 9.sp, letterSpacing = 0.8.sp, color = MaterialTheme.colorScheme.outline)
            }
            TextButton(onClick = onVolver) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("VOLVER A CARTERA", fontSize = 11.sp)
            }
        }

        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Filtrar resultados por nombre o ID...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedBorderColor    = Color.Transparent, focusedBorderColor = Amarillo
            )
        )

        when {
            uiState.solicitudesLoading -> LoadBox()
            filtrados.isEmpty() -> Box(Modifier.fillMaxWidth().padding(48.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Inbox, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(0.3f))
                    Text("No hay solicitudes pendientes", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                }
            }
            else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filtrados, key = { it.idPrestamo }) { sol ->
                    SolicitudCard(prestamo = sol, onVerMas = { onAbrirDetalle(sol) })
                }
            }
        }
    }
}

@Composable
private fun SolicitudCard(prestamo: PrestamoPendienteAdmin, onVerMas: () -> Unit) {
    val inicial = (prestamo.nombre?.firstOrNull() ?: '?').uppercaseChar()
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(Modifier.size(40.dp), CircleShape, Amarillo.copy(0.2f)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(inicial.toString(), fontWeight = FontWeight.Black, color = Amarillo, fontSize = 16.sp)
                        }
                    }
                    Column {
                        Text(
                            "${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(),
                            fontWeight = FontWeight.Black, fontSize = 14.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(12.dp), tint = Amarillo)
                            Text("POR REVISAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Amarillo)
                        }
                    }
                }
                Text(
                    "REF: ${prestamo.folio ?: "SOL-${prestamo.idPrestamo}"}",
                    fontSize = 10.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline, fontFamily = FontFamily.Monospace
                )
            }
            Column {
                Text("MONTO SOLICITADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline, letterSpacing = 0.5.sp)
                Text("\$${String.format("%,.0f", prestamo.montoTotal)}", fontSize = 26.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onVerMas, modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Oscuro)
            ) {
                Text("VER MÁS", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// MODAL: ESTADO DE CUENTA
// ═════════════════════════════════════════════════════════════

@Composable
fun EstadoDeCuentaModal(detalle: TicketPrestamoDetalle, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f)
        ) {
            Column {
                Box(Modifier.fillMaxWidth().background(Oscuro).padding(20.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("Estado de Cuenta", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        "${detalle.nombre} ${detalle.apellidoPaterno} ${detalle.apellidoMaterno ?: ""}".trim().uppercase(),
                                        color = Rojo, fontWeight = FontWeight.Black, fontSize = 11.sp
                                    )
                                    Text("|", color = Color.White.copy(0.3f))
                                    Text("EXPEDIENTE: ${detalle.folio}", color = Color.White.copy(0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, null, tint = Color.White)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = { /* TODO compartir */ },
                                colors  = ButtonDefaults.buttonColors(containerColor = Rojo),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Email, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("COMPARTIR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { /* TODO descargar PDF */ },
                                colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border  = BorderStroke(1.dp, Color.White.copy(0.3f)),
                                shape   = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Download, null, Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("DESCARGAR", fontWeight = FontWeight.Black, fontSize = 11.sp)
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // FIX 2: total pagado nunca negativo
                    item {
                        val liquidado = maxOf(0.0, detalle.montoTotal - detalle.saldoPendiente)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SpecificCard("SALDO PENDIENTE", "\$${String.format("%,.0f", detalle.saldoPendiente)}", Oscuro, Color.White, Modifier.weight(1f))
                            SpecificCard("TOTAL PAGADO",    "\$${String.format("%,.0f", liquidado)}",             Verde, Color.White, Modifier.weight(1f))
                        }
                    }
                    item {
                        val cuota = if (detalle.plazoMeses > 0) detalle.montoTotal / detalle.plazoMeses else 0.0
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("CUOTA MENSUAL", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                Text("\$${String.format("%,.0f", cuota)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Rojo)
                            }
                        }
                    }
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                    Text("TITULAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                }
                                Text("${detalle.nombre} ${detalle.apellidoPaterno}".uppercase(), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                detalle.telefono?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline) }
                            }
                        }
                    }
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.AccountBalance, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                    Text("CONDICIONES", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    CondItem("MONTO", "\$${String.format("%,.0f", detalle.montoTotal)}", Modifier.weight(1f))
                                    CondItem("PLAZO", "${detalle.plazoMeses} Meses", Modifier.weight(1f))
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    val tasa = if (detalle.tasaInteres > 1) "${detalle.tasaInteres.toInt()}%" else "${(detalle.tasaInteres * 100).toInt()}%"
                                    CondItem("TASA",  tasa, Modifier.weight(1f))
                                    CondItem("PAGOS", "${detalle.pagosRealizados}/${detalle.totalPagos}", Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text("CALENDARIO DE PAGOS", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            Icon(Icons.Default.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.outline)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                            CalH("NO.",    Modifier.width(30.dp))
                            CalH("FECHA",  Modifier.weight(1f))
                            CalH("MONTO",  Modifier.weight(1f))
                            CalH("ESTADO", Modifier.width(80.dp))
                        }
                    }
                    items(detalle.pagos, key = { it.idPago }) { pago ->
                        PagoRowPrototipo(pago)
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// MODAL: DETALLE SOLICITUD
// ═════════════════════════════════════════════════════════════

@Composable
internal fun DetalleSolicitudModal(
    prestamo: PrestamoPendienteAdmin,
    onDismiss: () -> Unit,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth(0.95f)) {
            Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                    Column {
                        Text("FOLIO #${prestamo.idPrestamo.toString().padStart(5, '0')}", fontSize = 10.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("${prestamo.nombre ?: ""} ${prestamo.apellidoPaterno ?: ""}".trim().uppercase(), fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                    Surface(color = Amarillo.copy(0.15f), shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), tint = Amarillo)
                            Text("PENDIENTE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Amarillo)
                        }
                    }
                }
                HorizontalDivider()
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Column {
                        Text("MONTO SOLICITADO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("\$${String.format("%,.0f", prestamo.montoTotal)}", fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("PLAZO", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.outline)
                        Text("${prestamo.plazoMeses} MESES", fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
                if (!prestamo.curp.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Badge, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(prestamo.curp!!, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                prestamo.email?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Email, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
                Text("Solicitud: ${prestamo.fechaCreacion.take(10)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onRechazar, modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Rojo)
                    ) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("RECHAZAR", fontWeight = FontWeight.Black)
                    }
                    Button(
                        onClick = onAprobar, modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Oscuro)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("APROBAR", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// ─── Micro-helpers ───────────────────────────────────────────

@Composable
internal fun TH(text: String, modifier: Modifier) =
    Text(text, modifier = modifier, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.outline)

@Composable
internal fun CalH(text: String, modifier: Modifier) =
    Text(text, modifier = modifier, fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color(0xFF94A3B8))

@Composable
internal fun CondItem(label: String, value: String, modifier: Modifier) =
    Column(modifier) {
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black)
    }

@Composable
internal fun LoadBox() =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { CircularProgressIndicator(color = Rojo) }

@Composable
internal fun EmptyBox(msg: String) =
    Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text(msg, color = MaterialTheme.colorScheme.outline) }