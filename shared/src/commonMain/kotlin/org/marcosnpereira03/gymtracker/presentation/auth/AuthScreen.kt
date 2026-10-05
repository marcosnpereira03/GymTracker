package org.marcosnpereira03.gymtracker.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated && state.successMessage != null) {
            onAuthSuccess()
        }
    }

    Scaffold(
        containerColor = Zinc950,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Zinc900)
                        .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo central con mancuerna
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Zinc900)
                        .border(1.dp, Emerald400.copy(alpha = 0.35f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Gym Tracker",
                        tint = Emerald400,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = "Gym Tracker",
                    color = White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Subtítulo según el modo
                Text(
                    text = if (state.mode == AuthMode.LOGIN) {
                        "Inicia sesión para acceder a tu historial de entrenamientos"
                    } else {
                        "Crea tu cuenta para comenzar a registrar tus cargas"
                    },
                    color = Zinc400,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Mensajes de error o éxito
                if (state.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Red500.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = Red500,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Campo: Correo Electrónico
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Correo Electrónico",
                        color = Zinc300,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.emailText,
                        onValueChange = { viewModel.onEmailChange(it) },
                        placeholder = { Text("tu@email.com", color = Zinc500, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Zinc500, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Zinc800,
                            focusedContainerColor = Zinc900,
                            unfocusedContainerColor = Zinc900
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Campo: Contraseña
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Contraseña",
                        color = Zinc300,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.passwordText,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        placeholder = { Text("••••••••", color = Zinc500, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Zinc500, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Zinc800,
                            focusedContainerColor = Zinc900,
                            unfocusedContainerColor = Zinc900
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal de acción
                Button(
                    onClick = { viewModel.onSubmit() },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald400,
                        contentColor = Color.Black
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (state.mode == AuthMode.LOGIN) "Iniciar Sesión" else "Crear Cuenta",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Alternador entre Login y Registro
                val toggleText = if (state.mode == AuthMode.LOGIN) {
                    buildAnnotatedString {
                        append("¿No tienes cuenta? ")
                        withStyle(style = SpanStyle(color = Emerald400, fontWeight = FontWeight.Bold)) {
                            append("Regístrate aquí")
                        }
                    }
                } else {
                    buildAnnotatedString {
                        append("¿Ya tienes una cuenta? ")
                        withStyle(style = SpanStyle(color = Emerald400, fontWeight = FontWeight.Bold)) {
                            append("Inicia sesión aquí")
                        }
                    }
                }

                Text(
                    text = toggleText,
                    color = Zinc400,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { viewModel.onToggleMode() }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
