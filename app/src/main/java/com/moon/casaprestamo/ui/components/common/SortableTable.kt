package com.moon.casaprestamo.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SortDirection {
    ASC, DESC
}

data class SortConfig(
    val key: String = "",
    val direction: SortDirection? = null
)

data class TableColumn(
    val key: String,
    val label: String,
    val sortable: Boolean = true,
    val width: Float = 1f, // Peso relativo
    val alignment: Alignment.Horizontal = Alignment.Start
)

/**
 * SortableTableHeader - Header de tabla con ordenamiento
 * Usado en: GestionPrestamos, GestionPersonal, Cobranza
 */
@Composable
fun SortableTableHeader(
    columns: List<TableColumn>,
    sortConfig: SortConfig,
    onSort: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.3f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            columns.forEach { column ->
                SortableHeaderCell(
                    column = column,
                    isActive = sortConfig.key == column.key,
                    direction = if (sortConfig.key == column.key) sortConfig.direction else null,
                    onSort = { if (column.sortable) onSort(column.key) },
                    modifier = Modifier.weight(column.width)
                )
            }
        }
    }
}

@Composable
private fun SortableHeaderCell(
    column: TableColumn,
    isActive: Boolean,
    direction: SortDirection?,
    onSort: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .then(
                if (column.sortable) {
                    Modifier.clickable { onSort() }
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 8.dp),
        horizontalArrangement = when (column.alignment) {
            Alignment.Start -> Arrangement.Start
            Alignment.End -> Arrangement.End
            else -> Arrangement.Center
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = column.label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            ),
            color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
        )

        if (column.sortable) {
            Spacer(Modifier.width(4.dp))
            Column(
                modifier = Modifier.size(14.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ArrowDropUp,
                    contentDescription = null,
                    modifier = Modifier
                        .size(12.dp)
                        .offset(y = 3.dp),
                    tint = if (isActive && direction == SortDirection.ASC) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(12.dp)
                        .offset(y = (-3).dp),
                    tint = if (isActive && direction == SortDirection.DESC) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }
}

/**
 * TableRow genérico para datos
 */
@Composable
fun TableDataRow(
    columns: List<TableColumn>,
    rowData: Map<String, String>,
    onRowClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onRowClick != null) {
                    Modifier.clickable { onRowClick() }
                } else {
                    Modifier
                }
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                Box(
                    modifier = Modifier
                        .weight(column.width)
                        .padding(horizontal = 8.dp),
                    contentAlignment = when (column.alignment) {
                        Alignment.Start -> Alignment.CenterStart
                        Alignment.End -> Alignment.CenterEnd
                        else -> Alignment.Center
                    }
                ) {
                    Text(
                        text = rowData[column.key] ?: "",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

/**
 * Tabla completa con ordenamiento
 */
@Composable
fun <T> SortableTable(
    columns: List<TableColumn>,
    data: List<T>,
    keyExtractor: (T) -> String,
    rowDataExtractor: (T) -> Map<String, String>,
    onRowClick: ((T) -> Unit)? = null,
    emptyState: @Composable () -> Unit = { DefaultEmptyState() },
    modifier: Modifier = Modifier
) {
    var sortConfig by remember { mutableStateOf(SortConfig()) }

    // Función de ordenamiento
    val sortedData = remember(data, sortConfig) {
        if (sortConfig.direction == null) {
            data
        } else {
            val sorted = data.sortedWith { a, b ->
                val aValue = rowDataExtractor(a)[sortConfig.key] ?: ""
                val bValue = rowDataExtractor(b)[sortConfig.key] ?: ""

                // Intentar comparar como números si es posible
                val comparison = try {
                    val aNum = aValue.replace("[^0-9.-]".toRegex(), "").toDoubleOrNull()
                    val bNum = bValue.replace("[^0-9.-]".toRegex(), "").toDoubleOrNull()

                    if (aNum != null && bNum != null) {
                        aNum.compareTo(bNum)
                    } else {
                        aValue.compareTo(bValue, ignoreCase = true)
                    }
                } catch (e: Exception) {
                    aValue.compareTo(bValue, ignoreCase = true)
                }

                if (sortConfig.direction == SortDirection.ASC) comparison else -comparison
            }
            sorted
        }
    }

    // Handler de ordenamiento
    val handleSort = { key: String ->
        sortConfig = when {
            sortConfig.key != key -> SortConfig(key, SortDirection.ASC)
            sortConfig.direction == SortDirection.ASC -> SortConfig(key, SortDirection.DESC)
            else -> SortConfig()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            SortableTableHeader(
                columns = columns,
                sortConfig = sortConfig,
                onSort = handleSort
            )

            if (sortedData.isEmpty()) {
                emptyState()
            } else {
                LazyColumn {
                    items(sortedData, key = keyExtractor) { item ->
                        TableDataRow(
                            columns = columns,
                            rowData = rowDataExtractor(item),
                            onRowClick = onRowClick?.let { { it(item) } }
                        )
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DefaultEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(64.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Text(
                text = "NO SE ENCONTRARON RESULTADOS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                ),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}