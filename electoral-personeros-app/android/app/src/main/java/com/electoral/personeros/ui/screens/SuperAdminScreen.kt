package com.electoral.personeros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.electoral.personeros.data.models.CentroVotacion
import com.electoral.personeros.data.models.Usuario
import com.electoral.personeros.ui.theme.*
import com.electoral.personeros.ui.viewmodels.SuperAdminVM
import com.electoral.personeros.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    viewModel: SuperAdminVM,
    onLogout: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSolicitudes: () -> Unit,
    onNavigateToPersoneros: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Usuarios", "Accesos Rápidos")

    LaunchedEffect(Unit) {
        viewModel.inicializar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Panel Super Administrador",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryCleanBlue,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryCleanBlue
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> UsuariosTab(viewModel)
                1 -> QuickAccessTab(
                    onDashboard = onNavigateToDashboard,
                    onSolicitudes = onNavigateToSolicitudes,
                    onPersoneros = onNavigateToPersoneros
                )
            }
        }
    }

    if (state.showForm) {
        UsuarioFormDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.toggleForm(false) }
        )
    }

    if (state.showBuscarCentro) {
        BuscarCentroModal(
            viewModel = viewModel,
            onDismiss = { viewModel.cerrarBuscarCentro() }
        )
    }
}

@Composable
private fun UsuariosTab(viewModel: SuperAdminVM) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                label = { Text("Buscar usuario / DNI", style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.selectedRolFilter == null,
                    onClick = { viewModel.updateRolFilter(null) },
                    label = { Text("Todos", style = MaterialTheme.typography.bodyMedium) }
                )
                FilterChip(
                    selected = state.selectedRolFilter == Constants.ROLE_SUPER_ADMIN,
                    onClick = { viewModel.updateRolFilter(Constants.ROLE_SUPER_ADMIN) },
                    label = { Text("SuperAdmin", style = MaterialTheme.typography.bodyMedium) }
                )
                FilterChip(
                    selected = state.selectedRolFilter == Constants.ROLE_ADMINISTRADOR,
                    onClick = { viewModel.updateRolFilter(Constants.ROLE_ADMINISTRADOR) },
                    label = { Text("Admin", style = MaterialTheme.typography.bodyMedium) }
                )
                FilterChip(
                    selected = state.selectedRolFilter == Constants.ROLE_PERSONERO_CENTRO ||
                        state.selectedRolFilter == Constants.ROLE_PERSONERO_MESA,
                    onClick = { viewModel.updateRolFilter(Constants.ROLE_PERSONERO_CENTRO) },
                    label = { Text("Personeros", style = MaterialTheme.typography.bodyMedium) }
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.toggleForm(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessCleanBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Nuevo Usuario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCleanBlue)
            }
        } else if (state.errorMessage != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        } else if (state.filteredUsuarios.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PeopleOutline, null, modifier = Modifier.size(64.dp), tint = WarningCleanBlue)
                    Spacer(Modifier.height(8.dp))
                    Text("No hay usuarios registrados", style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(state.filteredUsuarios, key = { it.idusuario }) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        isDeleting = state.isDeleting == usuario.idusuario,
                        onEdit = { viewModel.toggleForm(true, usuario) },
                        onDelete = { viewModel.deleteUsuario(usuario.idusuario) }
                    )
                }
            }
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: Usuario,
    isDeleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PrimaryCleanBlue.copy(alpha = 0.15f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person,
                        null,
                        tint = PrimaryCleanBlue,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    usuario.nombreCompleto,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("@${usuario.usuario}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF546E7A))
                if (usuario.dniDisplay.isNotBlank()) {
                    Text("DNI ${usuario.dniDisplay}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF455A64))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = {},
                        label = { Text(usuario.rol, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (usuario.rol.uppercase()) {
                                Constants.ROLE_SUPER_ADMIN -> Color(0xFFE1BEE7)
                                Constants.ROLE_ADMINISTRADOR -> Color(0xFFBBDEFB)
                                Constants.ROLE_PERSONERO_CENTRO -> Color(0xFFFFF3E0)
                                else -> Color(0xFFC8E6C9)
                            }
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                if (usuario.estado) "Activo" else "Inactivo",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (usuario.estado) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
                        )
                    )
                }
                val ubicacion = listOfNotNull(usuario.provinciaDisplay.ifBlank { null }, usuario.distritoDisplay.ifBlank { null })
                    .joinToString(" · ")
                if (ubicacion.isNotBlank()) {
                    Text(ubicacion, style = MaterialTheme.typography.bodySmall)
                }
            }
            Column {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = PrimaryCleanBlue)
                }
                IconButton(onClick = onDelete, enabled = !isDeleting) {
                    if (isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Delete, "Eliminar", tint = ErrorCleanBlue)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAccessTab(
    onDashboard: () -> Unit,
    onSolicitudes: () -> Unit,
    onPersoneros: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickAccessCard(
            title = "Cobertura Electoral",
            desc = "Ver progreso y estadísticas",
            icon = Icons.Default.Dashboard,
            onClick = onDashboard
        )
        QuickAccessCard(
            title = "Solicitudes Pendientes",
            desc = "Aprobar o rechazar registros",
            icon = Icons.Default.Description,
            onClick = onSolicitudes
        )
        QuickAccessCard(
            title = "Asignar Mesas",
            desc = "Asignar personeros a centros",
            icon = Icons.Default.AssignmentInd,
            onClick = onPersoneros
        )
    }
}

@Composable
private fun QuickAccessCard(title: String, desc: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = PrimaryCleanBlue.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PrimaryCleanBlue,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF546E7A))
            }
            Icon(Icons.Default.ChevronRight, null, tint = PrimaryCleanBlue)
        }
    }
}

private val ROLES_DISPONIBLES = listOf(
    Constants.ROLE_SUPER_ADMIN,
    Constants.ROLE_ADMINISTRADOR,
    Constants.ROLE_PERSONERO_CENTRO,
    Constants.ROLE_PERSONERO_MESA
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsuarioFormDialog(viewModel: SuperAdminVM, onDismiss: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val form = state.formData
    val esPersoneroCentro = form.rol.equals(Constants.ROLE_PERSONERO_CENTRO, ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            if (state.isEditing) "Editar Usuario" else "Nuevo Usuario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCleanBlue
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Datos de la Persona",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCleanBlue
                    )
                    Divider(color = PrimaryCleanBlue.copy(alpha = 0.3f))
                    OutlinedTextField(
                        value = form.dni,
                        onValueChange = { viewModel.updateFormData(form.copy(dni = it)) },
                        leadingIcon = { Icon(Icons.Default.Badge, null) },
                        label = { Text("DNI *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.nombre,
                        onValueChange = { viewModel.updateFormData(form.copy(nombre = it)) },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Nombres *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = form.apellidos,
                        onValueChange = { viewModel.updateFormData(form.copy(apellidos = it)) },
                        leadingIcon = { Icon(Icons.Default.FamilyRestroom, null) },
                        label = { Text("Apellidos *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = form.telefono1 ?: "",
                            onValueChange = { viewModel.updateFormData(form.copy(telefono1 = it.ifBlank { null })) },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            label = { Text("Teléfono 1", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = form.telefono2 ?: "",
                            onValueChange = { viewModel.updateFormData(form.copy(telefono2 = it.ifBlank { null })) },
                            leadingIcon = { Icon(Icons.Default.Smartphone, null) },
                            label = { Text("Teléfono 2", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = form.direccion ?: "",
                        onValueChange = { viewModel.updateFormData(form.copy(direccion = it.ifBlank { null })) },
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        label = { Text("Dirección", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    if (esPersoneroCentro) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Centro de Votación Asignado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = SuccessCleanBlue
                        )
                        if (state.centroSeleccionado != null) {
                            val c = state.centroSeleccionado!!
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = SuccessCleanBlue.copy(alpha = 0.08f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = SuccessCleanBlue.copy(alpha = 0.2f),
                                        modifier = Modifier.size(46.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.LocationOn, null, tint = SuccessCleanBlue)
                                        }
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(c.nombre_local, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text("Código: ${c.codigo_local} · Ubigeo: ${c.ubigeo}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF455A64))
                                        Text("${c.provincia} · ${c.distrito}", style = MaterialTheme.typography.bodySmall)
                                        Text(c.direccion_local, style = MaterialTheme.typography.bodySmall, color = Color(0xFF546E7A))
                                    }
                                    IconButton(onClick = { viewModel.abrirBuscarCentro() }) {
                                        Icon(Icons.Default.Edit, "Cambiar centro", tint = PrimaryCleanBlue)
                                    }
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.abrirBuscarCentro() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCleanBlue)
                            ) {
                                Icon(Icons.Default.Search, null)
                                Spacer(Modifier.width(8.dp))
                                Text("🔍 Buscar Centro de Votación", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    } else {
                        var provExpanded by remember { mutableStateOf(false) }
                        val provinciaActual = form.provinciaAsignada ?: form.provincia ?: ""
                        val distritosActuales by remember(provinciaActual) {
                            derivedStateOf {
                                if (provinciaActual.isBlank()) emptyList<String>()
                                else Constants.DISTRITOS_POR_PROVINCIA[provinciaActual]
                                    ?: Constants.DISTRITOS_POR_PROVINCIA.entries.firstOrNull {
                                        it.key.equals(provinciaActual, ignoreCase = true)
                                    }?.value
                                    ?: emptyList()
                            }
                        }
                        var distExpanded by remember { mutableStateOf(false) }
                        LaunchedEffect(provinciaActual) {
                            distExpanded = false
                        }

                        ExposedDropdownMenuBox(
                            expanded = provExpanded,
                            onExpandedChange = { provExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = provinciaActual,
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = { Icon(Icons.Default.Map, null) },
                                label = { Text("Provincia Asignada", style = MaterialTheme.typography.bodyMedium) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = provExpanded,
                                onDismissRequest = { provExpanded = false }
                            ) {
                                Constants.PROVINCIAS_LORETO.forEach { provItem ->
                                    DropdownMenuItem(
                                        text = { Text(provItem, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            viewModel.updateFormData(form.copy(
                                                provinciaAsignada = provItem,
                                                provincia = provItem,
                                                distritoAsignado = null,
                                                distrito = null
                                            ))
                                            provExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        val distritoActual = (form.distritoAsignado ?: form.distrito) ?: ""
                        ExposedDropdownMenuBox(
                            expanded = distExpanded,
                            onExpandedChange = { nuevoEstado ->
                                distExpanded = distritosActuales.isNotEmpty() && nuevoEstado
                            }
                        ) {
                            OutlinedTextField(
                                value = distritoActual,
                                onValueChange = {},
                                readOnly = true,
                                enabled = distritosActuales.isNotEmpty(),
                                leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                                label = { Text("Distrito Asignado", style = MaterialTheme.typography.bodyMedium) },
                                placeholder = {
                                    Text(
                                        text = if (provinciaActual.isBlank()) "Primero seleccione una provincia" else "Seleccione distrito",
                                        color = Color(0xFF90A4AE)
                                    )
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge,
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = distExpanded,
                                onDismissRequest = { distExpanded = false }
                            ) {
                                if (distritosActuales.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("⚠️ No hay distritos para esta provincia", color = Color(0xFF90A4AE)) },
                                        onClick = { distExpanded = false },
                                        enabled = false
                                    )
                                } else {
                                    distritosActuales.forEach { distItem ->
                                        DropdownMenuItem(
                                            text = { Text(distItem, style = MaterialTheme.typography.bodyLarge) },
                                            onClick = {
                                                viewModel.updateFormData(form.copy(
                                                    distritoAsignado = distItem,
                                                    distrito = distItem
                                                ))
                                                distExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Datos de Acceso",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCleanBlue
                    )
                    Divider(color = PrimaryCleanBlue.copy(alpha = 0.3f))
                    OutlinedTextField(
                        value = form.usuario,
                        onValueChange = { viewModel.updateFormData(form.copy(usuario = it)) },
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, null) },
                        label = { Text("Usuario *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    if (!state.isEditing) {
                        OutlinedTextField(
                            value = form.password ?: "",
                            onValueChange = { viewModel.updateFormData(form.copy(password = it)) },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            label = { Text("Contraseña *", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                    }

                    var rolExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = rolExpanded,
                        onExpandedChange = { rolExpanded = !rolExpanded }
                    ) {
                        OutlinedTextField(
                            value = form.rol,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.AdminPanelSettings, null) },
                            label = { Text("Rol", style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        ExposedDropdownMenu(expanded = rolExpanded, onDismissRequest = { rolExpanded = false }) {
                            ROLES_DISPONIBLES.forEach { rol ->
                                DropdownMenuItem(
                                    text = { Text(rol, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        viewModel.updateFormData(form.copy(rol = rol))
                                        rolExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Usuario Activo", style = MaterialTheme.typography.titleSmall)
                        Switch(
                            checked = form.estado,
                            onCheckedChange = { viewModel.updateFormData(form.copy(estado = it)) }
                        )
                    }

                    if (state.formError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ErrorCleanBlue.copy(alpha = 0.08f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, "Error", tint = ErrorCleanBlue)
                                Spacer(Modifier.width(8.dp))
                                Text(state.formError!!, color = ErrorCleanBlue, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancelar", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = { viewModel.saveUsuario() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            enabled = !state.isSaving,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCleanBlue)
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                if (state.isSaving) "Guardando..." else if (state.isEditing) "Guardar Cambios" else "Crear Usuario",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuscarCentroModal(viewModel: SuperAdminVM, onDismiss: () -> Unit) {
    val state by viewModel.uiState.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Text(
                            "Buscar Centro de Votación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCleanBlue
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.centroSearchQuery,
                            onValueChange = { viewModel.actualizarCentroSearchQuery(it) },
                            leadingIcon = { Icon(Icons.Default.Search, "Buscar") },
                            label = { Text("Nombre / Distrito / Dirección", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                        Button(
                            onClick = { viewModel.ejecutarBuscarCentros() },
                            modifier = Modifier.height(56.dp),
                            enabled = !state.centroSearchLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCleanBlue)
                        ) {
                            if (state.centroSearchLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.TravelExplore, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Buscar", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Escribe un nombre, distrito o dirección y pulsa BUSCAR",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF546E7A)
                    )
                    Spacer(Modifier.height(10.dp))

                    when {
                        state.centroSearchLoading -> {
                            Box(Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryCleanBlue)
                            }
                        }
                        state.centroSearchError != null -> {
                            Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                                Text(state.centroSearchError!!, color = ErrorCleanBlue)
                            }
                        }
                        state.centroSearchResults.isEmpty() -> {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(48.dp), tint = WarningCleanBlue)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Pulsa Buscar para ver resultados", style = MaterialTheme.typography.titleSmall)
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 8.dp)
                            ) {
                                items(state.centroSearchResults, key = { it.id_centro }) { centro ->
                                    CentroResultCard(centro = centro, onSeleccionar = { viewModel.seleccionarCentro(centro) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CentroResultCard(centro: CentroVotacion, onSeleccionar: () -> Unit) {
    ElevatedCard(
        onClick = onSeleccionar,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PrimaryCleanBlue.copy(alpha = 0.15f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Apartment, null, tint = PrimaryCleanBlue)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(centro.nombre_local, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Cód: ${centro.codigo_local} · Ubigeo: ${centro.ubigeo}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF455A64))
                Text("📍 ${centro.provincia} · ${centro.distrito}", style = MaterialTheme.typography.bodySmall, color = PrimaryCleanBlue, fontWeight = FontWeight.Medium)
                Text(centro.direccion_local, style = MaterialTheme.typography.bodySmall, color = Color(0xFF546E7A))
            }
            Button(
                onClick = onSeleccionar,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessCleanBlue)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Seleccionar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
