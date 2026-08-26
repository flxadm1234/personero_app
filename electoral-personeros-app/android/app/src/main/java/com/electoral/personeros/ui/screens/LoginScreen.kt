package com.electoral.personeros.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.electoral.personeros.ui.theme.PrimaryCleanBlue
import com.electoral.personeros.ui.theme.SecondaryCleanBlue
import com.electoral.personeros.ui.viewmodels.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToSolicitudes: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    if (state.showRegistroModal) {
        RegistroSolicitudModal(
            viewModel = viewModel,
            onDismiss = { viewModel.showRegistroModal(false) }
        )
    }

    LaunchedEffect(state.solicitudSuccess) {
        if (state.solicitudSuccess) {
            viewModel.clearSolicitudSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PrimaryCleanBlue,
                        SecondaryCleanBlue,
                        Color(0xFF81D4FA)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Sistema de Personeros",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PrimaryCleanBlue
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Elecciones 202X",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF546E7A)
                )

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = PrimaryCleanBlue.copy(alpha = 0.3f),
                    thickness = 2.dp
                )

                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.updateUsername(it) },
                    label = { Text("Usuario", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = "Usuario", tint = PrimaryCleanBlue)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryCleanBlue,
                        unfocusedBorderColor = PrimaryCleanBlue.copy(alpha = 0.4f),
                        focusedLabelColor = PrimaryCleanBlue
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    isError = state.errorMessage != null
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Contraseña", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = "Contraseña", tint = PrimaryCleanBlue)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryCleanBlue,
                        unfocusedBorderColor = PrimaryCleanBlue.copy(alpha = 0.4f),
                        focusedLabelColor = PrimaryCleanBlue
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        viewModel.login()
                    }),
                    isError = state.errorMessage != null
                )

                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryCleanBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = if (state.isLoading) "Ingresando..." else "INICIAR SESIÓN",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedButton(
                    onClick = { viewModel.showRegistroModal(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SecondaryCleanBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, brush = Brush.linearGradient(listOf(SecondaryCleanBlue, PrimaryCleanBlue)))
                ) {
                    Text(
                        text = "Regístrate como Personero",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                }

                TextButton(
                    onClick = onNavigateToSolicitudes,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Ver solicitudes pendientes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryCleanBlue
                    )
                }
            }
        }
    }
}
