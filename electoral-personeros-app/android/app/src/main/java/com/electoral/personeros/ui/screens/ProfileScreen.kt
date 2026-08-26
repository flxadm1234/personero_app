package com.electoral.personeros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.PerfilUsuarioCompletoResponse
import com.electoral.personeros.ui.theme.*
import com.electoral.personeros.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    tokenRol: String?,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var perfil by remember { mutableStateOf<PerfilUsuarioCompletoResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        try {
            val resp = RetrofitInstance.api.getAuthMe()
            if (resp.isSuccessful && resp.body() != null) {
                perfil = resp.body()!!
            } else {
                error = resp.errorBody()?.string() ?: "No se pudo cargar el perfil"
            }
        } catch (e: Exception) {
            error = e.message ?: "Error de conexión"
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mi Perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(rolDisplay(tokenRol), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White) }
                },
                actions = {
                    IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Salir", tint = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryCleanBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryCleanBlue)
                }
                error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Default.Error, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Text(error!!, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(10.dp))
                        Button(onClick = {
                            isLoading = true
                            error = null
                        }) { Text("Reintentar") }
                    }
                }
                else -> perfil?.let { p -> PerfilContenido(p) }
            }
        }
    }
}

@Composable
private fun PerfilContenido(p: PerfilUsuarioCompletoResponse) {
    val persona = p.persona
    val nombreCompleto = persona?.let { "${it.nombre} ${it.apellidos}".trim() }?.takeIf { it.isNotBlank() }
        ?: p.usuario.takeIf { it.isNotBlank() } ?: "Usuario"
    val rolName = rolDisplay(p.rol)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryCleanBlue,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(nombreCompleto, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                AssistChip(
                    onClick = {},
                    label = { Text(rolName, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = { Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(16.dp)) },
                    colors = when (p.rol.uppercase()) {
                        Constants.ROLE_SUPER_ADMIN -> AssistChipDefaults.assistChipColors(containerColor = Color(0xFFFFF3E0))
                        Constants.ROLE_ADMINISTRADOR -> AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE3F2FD))
                        Constants.ROLE_PERSONERO_CENTRO -> AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE8F5E9))
                        else -> AssistChipDefaults.assistChipColors()
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text("@${p.usuario}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF546E7A))
            }
        }

        SeccionTarjeta(titulo = "Datos Personales", icon = Icons.Default.Badge, iconColor = PrimaryCleanBlue) {
            InfoRow(Icons.Default.Badge, "DNI", persona?.dni?.takeIf { it.isNotBlank() } ?: "-")
            InfoRow(Icons.Default.Person, "Nombres", persona?.nombre?.takeIf { it.isNotBlank() } ?: "-")
            InfoRow(Icons.Default.People, "Apellidos", persona?.apellidos?.takeIf { it.isNotBlank() } ?: "-")
            InfoRow(Icons.Default.Phone, "Teléfono", persona?.telefono1?.takeIf { it.isNotBlank() } ?: "-")
            persona?.telefono2?.takeIf { it.isNotBlank() }?.let { InfoRow(Icons.Default.Chat, "WhatsApp", it) }
            persona?.correo?.takeIf { it.isNotBlank() }?.let { InfoRow(Icons.Default.Email, "Correo", it) }
            persona?.direccion?.takeIf { it.isNotBlank() }?.let { InfoRow(Icons.Default.Home, "Dirección", it) }
        }

        SeccionTarjeta(titulo = "Ubicación", icon = Icons.Default.LocationOn, iconColor = SuccessCleanBlue) {
            InfoRow(Icons.Default.LocationCity, "Provincia", persona?.provincia?.takeIf { it.isNotBlank() } ?: "-")
            InfoRow(Icons.Default.Map, "Distrito", persona?.distrito?.takeIf { it.isNotBlank() } ?: "-")
        }

        if (p.rol.uppercase() == Constants.ROLE_ADMINISTRADOR || p.rol.uppercase() == Constants.ROLE_SUPER_ADMIN) {
            val distritos = p.distritos_asignados.orEmpty()
            SeccionTarjeta(
                titulo = if (p.rol.uppercase() == Constants.ROLE_SUPER_ADMIN) "Jurisdicción (Sin limites - SUPER ADMIN)" else "Jurisdicción Asignada",
                icon = Icons.Default.AdminPanelSettings,
                iconColor = Color(0xFFFFA726)
            ) {
                if (distritos.isEmpty()) {
                    Text("Sin distritos asignados", color = Color(0xFF78909C))
                } else {
                    distritos.forEach { d ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Surface(shape = CircleShape, color = PrimaryCleanBlue.copy(alpha = 0.2f), modifier = Modifier.size(22.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Done, null, tint = PrimaryCleanBlue, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(buildString {
                                append(d.distrito)
                                d.provincia?.takeIf { it.isNotBlank() }?.let { append(" · $it") }
                            }, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (p.rol.uppercase() == Constants.ROLE_PERSONERO_CENTRO || p.centro_asignado != null) {
            SeccionTarjeta(titulo = "Asignación", icon = Icons.Default.WhereToVote, iconColor = SecondaryCleanBlue) {
                p.centro_asignado?.let { c ->
                    InfoRow(Icons.Default.LocationOn, "Centro Asignado", c.nombre_local)
                    InfoRow(Icons.Default.QrCode, "Código Local", c.codigo_local)
                    InfoRow(Icons.Default.Map, "Distrito", "${c.provincia} · ${c.distrito}")
                } ?: run {
                    p.id_centro_asignado?.let { InfoRow(Icons.Default.LocationOn, "ID Centro", "#$it") }
                        ?: Text("Sin centro asignado aún.", color = Color(0xFFF57C00))
                }
                p.mesa_asignada?.let { m ->
                    Spacer(Modifier.height(6.dp))
                    InfoRow(Icons.Default.TableChart, "Mesa Asignada", m.nombreCompleto)
                } ?: p.id_mesa_asignada?.let {
                    InfoRow(Icons.Default.TableChart, "ID Mesa", "#$it")
                }
            }
        }

        SeccionTarjeta(titulo = "Estado de la Cuenta", icon = Icons.Default.ManageAccounts, iconColor = Color(0xFF7B1FA2)) {
            InfoRow(
                Icons.Default.VerifiedUser,
                "Estado",
                if (p.estado) "ACTIVA" else "INACTIVA"
            )
            InfoRow(Icons.Default.AlternateEmail, "Usuario", p.usuario)
        }
    }
}

@Composable
private fun SeccionTarjeta(
    titulo: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    contenido: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.15f), modifier = Modifier.size(36.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Divider(modifier = Modifier.padding(bottom = 8.dp))
            contenido()
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(modifier = Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(18.dp), tint = Color(0xFF546E7A))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color(0xFF78909C))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

private fun rolDisplay(rol: String?): String {
    return when (rol?.uppercase()) {
        Constants.ROLE_SUPER_ADMIN -> "Super Administrador"
        Constants.ROLE_ADMINISTRADOR -> "Administrador Electoral"
        Constants.ROLE_PERSONERO_CENTRO -> "Personero de Centro"
        Constants.ROLE_PERSONERO_MESA -> "Personero de Mesa"
        else -> "Usuario"
    }
}
