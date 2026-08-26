package com.electoral.personeros.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.electoral.personeros.ui.theme.PrimaryCleanBlue
import com.electoral.personeros.ui.theme.SecondaryCleanBlue
import com.electoral.personeros.ui.theme.SuccessCleanBlue
import com.electoral.personeros.ui.viewmodels.AdminVM
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminVM,
    onLogout: () -> Unit,
    onNavigateToSolicitudes: () -> Unit,
    onNavigateToPersoneros: () -> Unit
) {
    val dashState by viewModel.dashboardState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadCoberturaGeneral() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Cobertura Electoral",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val nivel = dashState.provinciaSeleccionada?.let { prov ->
                            dashState.distritoSeleccionado?.let { "$prov / $it" } ?: prov
                        } ?: "Nivel Nacional"
                        Text(nivel, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                navigationIcon = {
                    if (dashState.nivelActual != "GENERAL") {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(Icons.Default.ArrowBack, "Atrás", tint = Color.White)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSolicitudes) {
                        Icon(Icons.Default.Description, "Solicitudes", tint = Color.White)
                    }
                    IconButton(onClick = onNavigateToPersoneros) {
                        Icon(Icons.Default.AssignmentInd, "Asignar", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryCleanBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.loadCoberturaGeneral() },
                containerColor = SecondaryCleanBlue,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Refresh, "Actualizar") },
                text = { Text("Actualizar", fontWeight = FontWeight.SemiBold) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                dashState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = PrimaryCleanBlue) }

                dashState.errorMessage != null -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Error, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text(dashState.errorMessage!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadCoberturaGeneral() }) {
                            Text("Reintentar")
                        }
                    }
                }

                else -> {
                    val cobertura = viewModel.getCurrentCobertura()
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (cobertura != null) {
                            ProgressCard(cobertura.porcentajeCobertura, cobertura.mesasAsignadas, cobertura.totalMesas)
                            StatsRow(cobertura)
                        }
                        Text(
                            if (dashState.nivelActual == "GENERAL") "Provincias" else if (dashState.nivelActual == "PROVINCIA") "Distritos" else "Detalle",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp, end = 20.dp)
                        )
                        DesgloseList(
                            items = viewModel.getDesgloseList(),
                            nivel = dashState.nivelActual,
                            onItemClick = { item ->
                                if (dashState.nivelActual == "GENERAL") {
                                    viewModel.drillDownProvincia(item.provincia)
                                } else if (dashState.nivelActual == "PROVINCIA" && dashState.provinciaSeleccionada != null) {
                                    viewModel.drillDownDistrito(dashState.provinciaSeleccionada!!, item.distrito)
                                }
                            },
                            canDrillDown = dashState.nivelActual != "DISTRITO"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(porcentaje: Float, asignadas: Int, total: Int) {
    val pct = min(porcentaje, 100f)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryCleanBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgress(
                percentage = pct,
                size = 130.dp,
                strokeWidth = 14.dp
            )
            Spacer(Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Cobertura Actual",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${String.format("%.1f", pct)}%",
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "$asignadas de $total mesas asignadas",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${total - asignadas} mesas disponibles",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CircularProgress(
    percentage: Float,
    size: Dp,
    strokeWidth: Dp
) {
    val backgroundArc = Color.White.copy(alpha = 0.25f)
    val progressArc = Color.White
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val arcSize = Size(size.toPx() - strokePx, size.toPx() - strokePx)
            drawArc(
                color = backgroundArc,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
            val sweep = (percentage / 100f) * 360f
            drawArc(
                color = progressArc,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${percentage.toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatsRow(cobertura: com.electoral.personeros.data.models.CoberturaResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatMini("Centros", cobertura.totalCentros.toString(), Color(0xFFE3F2FD), PrimaryCleanBlue, Icons.Default.LocationOn)
        StatMini("Personeros", cobertura.totalPersoneros.toString(), Color(0xFFE8F5E9), SuccessCleanBlue, Icons.Default.People)
    }
}

@Composable
private fun RowScope.StatMini(label: String, value: String, bgColor: Color, iconColor: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(shape = CircleShape, color = iconColor.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF455A64))
        }
    }
}

@Composable
private fun DesgloseList(
    items: List<com.electoral.personeros.data.models.DesgloseItem>,
    nivel: String,
    onItemClick: (com.electoral.personeros.data.models.DesgloseItem) -> Unit,
    canDrillDown: Boolean
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Sin datos disponibles", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF78909C))
        }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(items, key = { it.clave }) { item ->
            DesgloseCard(
                item = item,
                onClick = { if (canDrillDown) onItemClick(item) },
                showArrow = canDrillDown && nivel != "DISTRITO"
            )
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun DesgloseCard(
    item: com.electoral.personeros.data.models.DesgloseItem,
    onClick: () -> Unit,
    showArrow: Boolean
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${item.asignadas}/${item.total} asignadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF546E7A)
                    )
                }
                Text(
                    "${String.format("%.1f", item.porcentaje)}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (item.porcentaje >= 80f) SuccessCleanBlue
                            else if (item.porcentaje >= 50f) PrimaryCleanBlue
                            else Color(0xFFF57C00)
                )
                if (showArrow) {
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ChevronRight, null, tint = PrimaryCleanBlue)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(
                            PrimaryCleanBlue.copy(alpha = 0.15f),
                            RoundedCornerShape(5.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(min(item.porcentaje / 100f, 1f))
                            .height(10.dp)
                            .background(
                                if (item.porcentaje >= 80f) SuccessCleanBlue
                                else if (item.porcentaje >= 50f) PrimaryCleanBlue
                                else Color(0xFFFF9800),
                                RoundedCornerShape(5.dp)
                            )
                    )
                }
                Spacer(Modifier.width(10.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "Libres: ${item.disponibles}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFECEFF1))
                )
            }
        }
    }
}
