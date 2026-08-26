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
import com.electoral.personeros.data.models.Mesa
import com.electoral.personeros.data.models.PersoneroDisponible
import com.electoral.personeros.ui.theme.ErrorCleanBlue
import com.electoral.personeros.ui.theme.PrimaryCleanBlue
import com.electoral.personeros.ui.theme.SecondaryCleanBlue
import com.electoral.personeros.ui.theme.SuccessCleanBlue
import com.electoral.personeros.ui.viewmodels.PersoneroCentroVM
import com.electoral.personeros.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarMesaModal(
    viewModel: PersoneroCentroVM,
    mesa: Mesa,
    centro: CentroVotacion,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var searchPersonero by remember { mutableStateOf("") }

    val filteredPersoneros = remember(state.personerosDisponibles, searchPersonero) {
        if (searchPersonero.isBlank()) state.personerosDisponibles
        else state.personerosDisponibles.filter {
            it.nombreCompleto.contains(searchPersonero, ignoreCase = true) ||
                it.username.contains(searchPersonero, ignoreCase = true)
        }
    }

    if (state.showRegistrarNuevo) {
        RegistrarPersoneroDirectoModal(
            viewModel = viewModel,
            mesa = mesa,
            centro = centro,
            onDismiss = { viewModel.cerrarRegistrarNuevo() }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Asignar Personero",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCleanBlue
                            )
                            Text(
                                "Mesa ${mesa.nombreCompleto} · ${centro.nombre_local}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF546E7A)
                            )
                        }
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
                        .verticalScroll(rememberScrollState())
                ) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = SecondaryCleanBlue.copy(alpha = 0.08f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryCleanBlue,
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.TableChart, null, tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Mesa ${mesa.nombreCompleto}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${mesa.totalElectores} electores · ${centro.distrito}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "Buscar por DNI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = state.dniLookup,
                                    onValueChange = { viewModel.updateDniLookup(it) },
                                    leadingIcon = { Icon(Icons.Default.Badge, null) },
                                    label = { Text("Ingrese DNI", style = MaterialTheme.typography.bodyMedium) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = MaterialTheme.typography.bodyLarge,
                                    singleLine = true
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.buscarPersonaPorDni() },
                                    enabled = !state.dniLookupLoading && state.dniLookup.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCleanBlue)
                                ) {
                                    if (state.dniLookupLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Buscar")
                                    }
                                }
                            }

                            state.dniLookupResult?.let { res ->
                                Spacer(Modifier.height(10.dp))
                                if (res.encontrado && res.persona != null) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = SuccessCleanBlue.copy(alpha = 0.1f)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Surface(shape = RoundedCornerShape(10.dp), color = SuccessCleanBlue, modifier = Modifier.size(40.dp)) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                                                }
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Persona Encontrada", fontWeight = FontWeight.SemiBold, color = SuccessCleanBlue)
                                                Text(res.persona.nombreCompleto)
                                                val datos = listOfNotNull(
                                                    res.persona.dni.takeIf { it.isNotBlank() },
                                                    res.persona.telefono1?.takeIf { it.isNotBlank() },
                                                    res.persona.correo
                                                ).joinToString(" · ")
                                                if (datos.isNotBlank()) Text(datos, style = MaterialTheme.typography.bodySmall)
                                            }
                                            if (res.id_solicitud_activa != null) {
                                                AssistChip(
                                                    onClick = {},
                                                    label = { Text("Ya registrado", fontWeight = FontWeight.SemiBold) },
                                                    colors = AssistChipDefaults.assistChipColors(containerColor = SuccessCleanBlue.copy(alpha = 0.15f))
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFF8E1)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF57C00), modifier = Modifier.size(40.dp)) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.PersonAdd, null, tint = Color.White)
                                                    }
                                                }
                                                Spacer(Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text("Persona no encontrada", fontWeight = FontWeight.SemiBold, color = Color(0xFFF57C00))
                                                    Text("Puede registrar un nuevo personero de mesa a continuación.", style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                            Spacer(Modifier.height(10.dp))
                                            Button(
                                                onClick = { viewModel.abrirRegistrarNuevo() },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                                            ) {
                                                Icon(Icons.Default.PersonAddAlt, null)
                                                Spacer(Modifier.width(6.dp))
                                                Text("Registrar Personero", fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }

                            state.registrarError?.let { err ->
                                Spacer(Modifier.height(6.dp))
                                Text(err, color = ErrorCleanBlue, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    state.personeroSeleccionado?.let { p ->
                        SelectedPersoneroCard(personero = p) { viewModel.selectPersonero(p) }
                        Spacer(Modifier.height(14.dp))
                    }

                    OutlinedTextField(
                        value = searchPersonero,
                        onValueChange = { searchPersonero = it },
                        leadingIcon = { Icon(Icons.Default.PersonSearch, "Buscar") },
                        label = { Text("Buscar personero disponible", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )

                    Spacer(Modifier.height(10.dp))

                    Text(
                        "Personeros Disponibles (${filteredPersoneros.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp, max = 320.dp)
                    ) {
                        if (filteredPersoneros.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PersonOff, null, Modifier.size(48.dp), tint = Color(0xFF90A4AE))
                                    Spacer(Modifier.height(6.dp))
                                    Text("Sin personeros disponibles", style = MaterialTheme.typography.bodyMedium)
                                    Text("Use la opción Registrar Personero arriba", style = MaterialTheme.typography.bodySmall, color = Color(0xFF78909C))
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredPersoneros, key = { it.id_solicitud }) { personero ->
                                    PersoneroOption(
                                        personero = personero,
                                        selected = state.personeroSeleccionado?.id_solicitud == personero.id_solicitud,
                                        onClick = { viewModel.selectPersonero(personero) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.observacionesAsignacion,
                        onValueChange = { viewModel.updateObservaciones(it) },
                        label = { Text("Observaciones (opcional)", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        minLines = 2,
                        maxLines = 4
                    )

                    state.asignarError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    Spacer(Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancelar", style = MaterialTheme.typography.titleMedium)
                        }
                        Button(
                            onClick = { viewModel.confirmarAsignacion() },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            enabled = !state.isAsignando && state.personeroSeleccionado != null,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCleanBlue)
                        ) {
                            if (state.isAsignando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                text = if (state.isAsignando) "Asignando..." else "Confirmar Asignación",
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
private fun RegistrarPersoneroDirectoModal(
    viewModel: PersoneroCentroVM,
    mesa: Mesa,
    centro: CentroVotacion,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val dniIni = state.dniLookup.ifBlank { state.dniLookupResult?.persona?.dni ?: "" }
    var dni by remember { mutableStateOf(dniIni) }
    var nombres by remember { mutableStateOf(state.dniLookupResult?.persona?.nombre ?: "") }
    var apellidos by remember { mutableStateOf(state.dniLookupResult?.persona?.apellidos ?: "") }
    var telefono by remember { mutableStateOf(state.dniLookupResult?.persona?.telefono1 ?: "") }
    var whatsapp by remember { mutableStateOf(state.dniLookupResult?.persona?.telefono2 ?: "") }
    var correo by remember { mutableStateOf(state.dniLookupResult?.persona?.correo ?: "") }
    var provincia by remember { mutableStateOf(centro.provincia) }
    var distrito by remember { mutableStateOf(centro.distrito) }
    var direccion by remember { mutableStateOf(state.dniLookupResult?.persona?.direccion ?: "") }
    var formErr by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White
        ) {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Registrar Personero",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B1FA2)
                            )
                            Text(
                                "Asignar a Mesa ${mesa.nombreCompleto} automáticamente",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF546E7A)
                            )
                        }
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
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("Datos con * son obligatorios", style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF3E5F5))
                    )
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { dni = it },
                        label = { Text("DNI *", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Badge, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { nombres = it },
                        label = { Text("Nombres *", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        label = { Text("Apellidos *", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.People, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Phone, null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp", style = MaterialTheme.typography.bodyMedium) },
                            leadingIcon = { Icon(Icons.Default.Chat, null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo electrónico", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        var provExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = provExp,
                            onExpandedChange = { provExp = !provExp },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = provincia,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Provincia", style = MaterialTheme.typography.bodyMedium) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provExp) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            ExposedDropdownMenu(expanded = provExp, onDismissRequest = { provExp = false }) {
                                Constants.PROVINCIAS_LORETO.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = { provincia = p; provExp = false }
                                    )
                                }
                            }
                        }
                        val distritos = Constants.DISTRITOS_POR_PROVINCIA[provincia] ?: emptyList()
                        var distExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = distExp,
                            onExpandedChange = { distExp = !distExp },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = distrito,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Distrito", style = MaterialTheme.typography.bodyMedium) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distExp) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            ExposedDropdownMenu(expanded = distExp, onDismissRequest = { distExp = false }) {
                                distritos.forEach { d ->
                                    DropdownMenuItem(
                                        text = { Text(d, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = { distrito = d; distExp = false }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    val error = state.registrarError ?: formErr
                    if (error != null) {
                        Text(error, color = ErrorCleanBlue, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) { Text("Cancelar") }
                        Button(
                            onClick = {
                                if (dni.isBlank() || nombres.isBlank() || apellidos.isBlank()) {
                                    formErr = "Complete los campos obligatorios (*)"
                                    return@Button
                                }
                                formErr = null
                                viewModel.confirmarRegistrarYAsignar(
                                    dni = dni,
                                    nombre = nombres,
                                    apellidos = apellidos,
                                    telefono1 = telefono,
                                    whatsapp = whatsapp,
                                    correo = correo,
                                    provincia = provincia,
                                    distrito = distrito,
                                    direccion = direccion
                                )
                            },
                            enabled = !state.isRegistrandoYAsignando,
                            modifier = Modifier.weight(1f).height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                        ) {
                            if (state.isRegistrandoYAsignando) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(Modifier.width(6.dp))
                                Text("Registrando...", fontWeight = FontWeight.SemiBold)
                            } else {
                                Icon(Icons.Default.HowToReg, null)
                                Spacer(Modifier.width(6.dp))
                                Text("Registrar y Asignar", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersoneroOption(
    personero: PersoneroDisponible,
    selected: Boolean,
    onClick: () -> Unit
) {
    val persona = personero.persona
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) PrimaryCleanBlue.copy(alpha = 0.12f) else Color(0xFFFAFAFA),
        tonalElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = PrimaryCleanBlue))
            Spacer(Modifier.width(4.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = SuccessCleanBlue.copy(alpha = 0.2f), modifier = Modifier.size(44.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = SuccessCleanBlue)
                }
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(personero.nombreCompleto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("@${personero.username}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF546E7A))
                val ubic = listOfNotNull(personero.provinciaAsignada, personero.distritoAsignado).joinToString(" · ")
                if (ubic.isNotBlank()) Text(ubic, style = MaterialTheme.typography.labelSmall)
                persona?.telefono?.let {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                        Icon(Icons.Default.Phone, null, Modifier.size(14.dp), tint = Color(0xFF546E7A))
                        Spacer(Modifier.width(3.dp))
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPersoneroCard(personero: PersoneroDisponible, onRemove: () -> Unit) {
    val persona = personero.persona
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = SuccessCleanBlue.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(12.dp), color = SuccessCleanBlue, modifier = Modifier.size(50.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color.White)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Personero Seleccionado",
                    style = MaterialTheme.typography.labelMedium,
                    color = SuccessCleanBlue,
                    fontWeight = FontWeight.Bold
                )
                Text(personero.nombreCompleto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val ub = listOfNotNull(personero.provinciaAsignada, personero.distritoAsignado).joinToString(" · ")
                if (ub.isNotBlank()) Text(ub, style = MaterialTheme.typography.bodySmall)
                persona?.telefono?.let { Text("📞 $it", style = MaterialTheme.typography.bodySmall) }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, "Quitar", tint = Color(0xFFD32F2F))
            }
        }
    }
}
