package com.electoral.personeros.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electoral.personeros.data.models.CentroVotacion
import com.electoral.personeros.data.models.Mesa
import com.electoral.personeros.data.models.Usuario
import com.electoral.personeros.ui.theme.*
import com.electoral.personeros.utils.Constants
import com.electoral.personeros.ui.viewmodels.AdminVM
import com.electoral.personeros.ui.viewmodels.PersoneroCentroVM
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersoneroCentroScreen(
    viewModel: PersoneroCentroVM,
    adminViewModel: AdminVM,
    provincia: String?,
    distrito: String?,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(provincia, distrito) {
        viewModel.loadCentros(provincia, distrito)
    }

    if (state.showAsignarModal && state.mesaParaAsignar != null && state.centroParaAsignar != null) {
        AsignarMesaModal(
            viewModel = viewModel,
            mesa = state.mesaParaAsignar!!,
            centro = state.centroParaAsignar!!,
            onDismiss = { viewModel.closeAsignarModal() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Asignación de Mesas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        val subt = listOfNotNull(provincia, distrito).joinToString(" · ")
                        if (subt.isNotBlank()) Text(subt, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadCentros(provincia, distrito) }) {
                        Icon(Icons.Default.Refresh, "Recargar", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Salir", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryCleanBlue, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                    label = { Text("Buscar centro / distrito", style = MaterialTheme.typography.bodyMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = true
                )
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCleanBlue)
                }
                state.errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, null, Modifier.size(60.dp), tint = Color(0xFFFFA726))
                        Spacer(Modifier.height(8.dp))
                        Text(state.errorMessage!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = { viewModel.loadCentros(provincia, distrito) }) { Text("Reintentar") }
                    }
                }
                state.filteredCentros.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LocationOff, null, Modifier.size(64.dp), tint = WarningCleanBlue)
                        Spacer(Modifier.height(8.dp))
                        Text("No hay centros de votación", style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredCentros, key = { it.id_centro }) { centro ->
                        CentroCard(
                            centro = centro,
                            mesas = state.mesasPorCentro[centro.id_centro],
                            isExpanded = state.centroExpandido == centro.id_centro,
                            onToggleExpand = { viewModel.toggleCentroExpandido(centro.id_centro) },
                            onAsignarMesa = { mesa -> viewModel.openAsignarMesa(mesa, centro) },
                            onDesasignarMesa = { mesaId -> viewModel.desasignarMesa(mesaId) },
                            isDesasignando = state.isDesasignando,
                            onCall = { tel ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("${Constants.TEL_PREFIX}$tel"))
                                context.startActivity(intent)
                            },
                            onWhatsApp = { num ->
                                val clean = num.replace(Regex("[^0-9]"), "")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("${Constants.WHATSAPP_PREFIX}$clean"))
                                context.startActivity(intent)
                            }
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CentroCard(
    centro: CentroVotacion,
    mesas: List<Mesa>?,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onAsignarMesa: (Mesa) -> Unit,
    onDesasignarMesa: (Int) -> Unit,
    isDesasignando: Int?,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit
) {
    val pct = if (centro.totalMesas > 0) (centro.personerosAsignados.toFloat() / centro.totalMesas) * 100f else 0f

    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = RoundedCornerShape(12.dp), color = PrimaryCleanBlue.copy(alpha = 0.15f), modifier = Modifier.size(54.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.LocationOn, null, tint = PrimaryCleanBlue, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(centro.nombre_local, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Código: ${centro.codigo_local}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF546E7A))
                    Text("${centro.provincia} · ${centro.distrito}", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = PrimaryCleanBlue
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${centro.personerosAsignados}/${centro.totalMesas} mesas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "${String.format("%.1f", pct)}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (pct >= 80f) SuccessCleanBlue else PrimaryCleanBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(PrimaryCleanBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(min(pct / 100f, 1f))
                        .height(8.dp)
                        .background(
                            if (pct >= 80f) SuccessCleanBlue else PrimaryCleanBlue,
                            RoundedCornerShape(4.dp)
                        )
                )
            }

            if (isExpanded) {
                Spacer(Modifier.height(14.dp))
                Divider()
                Spacer(Modifier.height(10.dp))
                Text("Mesas del Centro", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (mesas == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = PrimaryCleanBlue, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Cargando mesas...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else if (mesas.isEmpty()) {
                    Text("Sin mesas registradas", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF78909C))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        mesas.forEach { mesa ->
                            MesaRow(
                                mesa = mesa,
                                onAsignar = { onAsignarMesa(mesa) },
                                onDesasignar = { onDesasignarMesa(mesa.id_mesa) },
                                isDesasignando = isDesasignando == mesa.id_mesa,
                                onCall = onCall,
                                onWhatsApp = onWhatsApp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MesaRow(
    mesa: Mesa,
    onAsignar: () -> Unit,
    onDesasignar: () -> Unit,
    isDesasignando: Boolean,
    onCall: (String) -> Unit,
    onWhatsApp: (String) -> Unit
) {
    val disponible = mesa.estado.uppercase() == Constants.MESA_LIBRE || mesa.personeroId == null
    val personero = mesa.personero

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (disponible) Color(0xFFF1F8E9) else PrimaryCleanBlue.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (disponible) SuccessCleanBlue.copy(alpha = 0.2f) else SecondaryCleanBlue.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.TableChart, null, tint = if (disponible) SuccessCleanBlue else SecondaryCleanBlue)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Mesa ${mesa.nombreCompleto}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (disponible) "DISPONIBLE · ${mesa.totalElectores} electores"
                        else "ASIGNADA · ${mesa.totalElectores} electores",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (disponible) SuccessCleanBlue else PrimaryCleanBlue
                    )
                    if (personero != null) {
                        Spacer(Modifier.height(2.dp))
                        Text(personero.nombreCompleto, style = MaterialTheme.typography.bodySmall)
                        if (!personero.telefono1.isNullOrBlank()) {
                            val tel = personero.telefono1.orEmpty()
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                IconButton(
                                    onClick = { onCall(tel) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = SuccessCleanBlue)
                                }
                                val ws = tel
                                IconButton(
                                    onClick = { onWhatsApp(ws) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
                                }
                            }
                        }
                    }
                }
                if (disponible) {
                    Button(
                        onClick = onAsignar,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCleanBlue)
                    ) {
                        Icon(Icons.Default.AssignmentInd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Asignar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onDesasignar,
                        enabled = !isDesasignando,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorCleanBlue)
                    ) {
                        if (isDesasignando) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = ErrorCleanBlue)
                        } else {
                            Icon(Icons.Default.RemoveCircleOutline, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Quitar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
