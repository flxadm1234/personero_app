package com.electoral.personeros.ui.screens

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
import com.electoral.personeros.data.models.SolicitudPersonero
import com.electoral.personeros.ui.theme.*
import com.electoral.personeros.utils.Constants
import com.electoral.personeros.ui.viewmodels.AdminVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudesScreen(
    viewModel: AdminVM,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.solicitudesState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSolicitudes() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Solicitudes de Registro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadSolicitudes() }) {
                        Icon(Icons.Default.Refresh, "Recargar", tint = Color.White)
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
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSolicitudesSearch(it) },
                    leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                    label = { Text("Buscar por nombre/DNI", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoChip("Todos", null, state.selectedEstadoFilter) { viewModel.updateSolicitudesFiltroEstado(null) }
                    EstadoChip("Pendientes", Constants.ESTADO_SOLICITADO, state.selectedEstadoFilter) { viewModel.updateSolicitudesFiltroEstado(Constants.ESTADO_SOLICITADO) }
                    EstadoChip("Aprobadas", Constants.ESTADO_ASIGNADO, state.selectedEstadoFilter) { viewModel.updateSolicitudesFiltroEstado(Constants.ESTADO_ASIGNADO) }
                    EstadoChip("Rechazadas", Constants.ESTADO_RECHAZADO, state.selectedEstadoFilter) { viewModel.updateSolicitudesFiltroEstado(Constants.ESTADO_RECHAZADO) }
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCleanBlue)
                }
                state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
                state.filteredSolicitudes.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inbox, null, Modifier.size(64.dp), tint = WarningCleanBlue)
                        Spacer(Modifier.height(8.dp))
                        Text("No hay solicitudes", style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredSolicitudes, key = { it.id_solicitud }) { sol ->
                        SolicitudCard(
                            solicitud = sol,
                            isProcessing = state.isProcessing == sol.id_solicitud,
                            onAprobar = { viewModel.aprobarSolicitud(sol.id_solicitud) },
                            onRechazar = { viewModel.rechazarSolicitud(sol.id_solicitud) }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun EstadoChip(label: String, value: String?, selectedValue: String?, onClick: () -> Unit) {
    val selected = (value == null && selectedValue == null) || (value != null && value == selectedValue)
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
        } else null
    )
}

@Composable
private fun SolicitudCard(
    solicitud: SolicitudPersonero,
    isProcessing: Boolean,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    val persona = solicitud.persona
    val estadoStr = solicitud.estadoEnum
    val estadoColor = when (estadoStr) {
        Constants.ESTADO_SOLICITADO -> Color(0xFFFFB74D)
        Constants.ESTADO_ASIGNADO -> PrimaryCleanBlue
        Constants.ESTADO_RECHAZADO -> ErrorCleanBlue
        else -> Color(0xFF90A4AE)
    }
    val esPendiente = estadoStr == Constants.ESTADO_SOLICITADO

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = estadoColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = estadoColor, modifier = Modifier.size(30.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        persona?.nombreCompleto ?: "Sin datos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    persona?.dni?.let { Text("DNI: $it", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF546E7A)) }
                    val ub = listOfNotNull(solicitud.provinciaPreferida, solicitud.distritoPreferido).joinToString(" / ")
                    if (ub.isNotBlank()) Text(ub, style = MaterialTheme.typography.bodySmall)
                }
                AssistChip(
                    onClick = {},
                    label = { Text(solicitud.estado, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = estadoColor.copy(alpha = 0.15f), labelColor = estadoColor)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp), tint = Color(0xFF546E7A))
                Spacer(Modifier.width(4.dp))
                val location = listOfNotNull(persona?.provincia, persona?.distrito, persona?.corregimiento).joinToString(" - ")
                Text(if (location.isBlank()) "Sin ubicación" else location, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp), tint = Color(0xFF546E7A))
                Spacer(Modifier.width(4.dp))
                Text(persona?.telefono ?: "Sin teléfono", style = MaterialTheme.typography.bodySmall)
                persona?.whatsapp?.let {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp), tint = Color(0xFF25D366))
                    Spacer(Modifier.width(4.dp))
                    Text("WhatsApp: $it", style = MaterialTheme.typography.bodySmall)
                }
            }

            solicitud.experiencia?.let {
                Spacer(Modifier.height(6.dp))
                Text("Experiencia: $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFF546E7A))
            }
            solicitud.disponibilidad?.let {
                Spacer(Modifier.height(2.dp))
                Text("Disponibilidad: $it", style = MaterialTheme.typography.bodySmall, color = Color(0xFF546E7A))
            }

            if (esPendiente) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onRechazar,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCleanBlue)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ErrorCleanBlue)
                            Spacer(Modifier.width(6.dp))
                        } else {
                            Icon(Icons.Default.Close, null)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Rechazar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onAprobar,
                        enabled = !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessCleanBlue)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(6.dp))
                        } else {
                            Icon(Icons.Default.Check, null)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text("Aprobar", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
