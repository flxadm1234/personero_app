package com.electoral.personeros.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.electoral.personeros.data.models.Persona
import com.electoral.personeros.data.models.SolicitudPersonero
import com.electoral.personeros.ui.theme.PrimaryCleanBlue
import com.electoral.personeros.utils.Constants
import com.electoral.personeros.ui.viewmodels.LoginViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroSolicitudModal(
    viewModel: LoginViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    var dni by remember { mutableStateOf("") }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var whatsapp by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var provincia by remember { mutableStateOf(Constants.PROVINCIAS_LORETO.first()) }
    var distrito by remember { mutableStateOf("") }
    var experiencia by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf("COMPLETA") }
    var formError by remember { mutableStateOf<String?>(null) }

    val distritos = Constants.DISTRITOS_POR_PROVINCIA[provincia] ?: emptyList()

    LaunchedEffect(provincia) {
        distrito = distritos.firstOrNull() ?: ""
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
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
                            "Registro de Personero",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCleanBlue
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = PrimaryCleanBlue
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "Datos Personales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryCleanBlue
                    )

                    OutlinedTextField(
                        value = dni,
                        onValueChange = { dni = it },
                        label = { Text("Cédula / DNI *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { nombres = it },
                        label = { Text("Nombres *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = apellidos,
                        onValueChange = { apellidos = it },
                        label = { Text("Apellidos *", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = whatsapp,
                            onValueChange = { whatsapp = it },
                            label = { Text("WhatsApp", style = MaterialTheme.typography.bodyMedium) },
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        "Ubicación Preferida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryCleanBlue
                    )

                    var provinciaExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = provinciaExpanded,
                        onExpandedChange = { provinciaExpanded = !provinciaExpanded }
                    ) {
                        OutlinedTextField(
                            value = provincia,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Provincia", style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = provinciaExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        ExposedDropdownMenu(
                            expanded = provinciaExpanded,
                            onDismissRequest = { provinciaExpanded = false }
                        ) {
                            Constants.PROVINCIAS_LORETO.forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text(prov, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        provincia = prov
                                        provinciaExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (distritos.isNotEmpty()) {
                        var distritoExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = distritoExpanded,
                            onExpandedChange = { distritoExpanded = !distritoExpanded }
                        ) {
                            OutlinedTextField(
                                value = distrito,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Distrito", style = MaterialTheme.typography.bodyMedium) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = distritoExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = MaterialTheme.typography.bodyLarge
                            )
                            ExposedDropdownMenu(
                                expanded = distritoExpanded,
                                onDismissRequest = { distritoExpanded = false }
                            ) {
                                distritos.forEach { dist ->
                                    DropdownMenuItem(
                                        text = { Text(dist, style = MaterialTheme.typography.bodyLarge) },
                                        onClick = {
                                            distrito = dist
                                            distritoExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        "Información Adicional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryCleanBlue
                    )

                    OutlinedTextField(
                        value = experiencia,
                        onValueChange = { experiencia = it },
                        label = { Text("Experiencia previa", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        minLines = 2,
                        maxLines = 4
                    )

                    var dispExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = dispExpanded,
                        onExpandedChange = { dispExpanded = !dispExpanded }
                    ) {
                        OutlinedTextField(
                            value = disponibilidad,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Disponibilidad", style = MaterialTheme.typography.bodyMedium) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dispExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        ExposedDropdownMenu(
                            expanded = dispExpanded,
                            onDismissRequest = { dispExpanded = false }
                        ) {
                            listOf("COMPLETA", "MEDIA JORNADA", "MAÑANA", "TARDE").forEach { disp ->
                                DropdownMenuItem(
                                    text = { Text(disp, style = MaterialTheme.typography.bodyLarge) },
                                    onClick = {
                                        disponibilidad = disp
                                        dispExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    val errorMostrar = state.solicitudError ?: formError
                    if (errorMostrar != null) {
                        Text(
                            text = errorMostrar,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                "Cancelar",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Button(
                            onClick = {
                                if (dni.isBlank() || nombres.isBlank() || apellidos.isBlank()) {
                                    formError = "Complete los campos obligatorios (*)"
                                    return@Button
                                }
                                formError = null
                                val persona = Persona(
                                    dni = dni.trim(),
                                    nombre = nombres.trim(),
                                    apellidos = apellidos.trim(),
                                    telefono1 = telefono.trim(),
                                    telefono2 = whatsapp.trim().ifBlank { null },
                                    provincia = provincia,
                                    distrito = distrito.ifBlank { "" },
                                    correo_real = correo.trim().ifBlank { null }
                                )
                                val fechaHoy = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                                val solicitud = SolicitudPersonero(
                                    persona = persona,
                                    id_persona = 0,
                                    tipo_personero_solicitado = Constants.TIPO_MESA,
                                    estado_vinculacion = Constants.ESTADO_SOLICITADO,
                                    fecha_registro = fechaHoy
                                )
                                viewModel.submitSolicitud(solicitud)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            enabled = !state.solicitudLoading,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCleanBlue)
                        ) {
                            if (state.solicitudLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = if (state.solicitudLoading) "Registrando..." else "Registrar Personero",
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
