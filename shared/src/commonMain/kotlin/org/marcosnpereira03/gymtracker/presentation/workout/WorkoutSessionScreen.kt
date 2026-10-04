package org.marcosnpereira03.gymtracker.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutSessionViewModel,
    workoutId: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(workoutId) {
        viewModel.initSession(workoutId)
    }

    LaunchedEffect(state.isSavedSuccess) {
        if (state.isSavedSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (workoutId != null) "Editar Entrenamiento" else "Nueva Sesión",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.onSaveWorkout() },
                        enabled = !state.isSaving,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = TextOnEmerald
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = TextOnEmerald,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guardar", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                // Título de la sesión
                item {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { viewModel.onTitleChange(it) },
                        label = { Text("Título de la Sesión", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = DarkSurfaceBorder,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard
                        )
                    )
                }

                // Métricas rápidas: Peso Corporal del día y Volumen de sesión
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.bodyWeightText,
                            onValueChange = { viewModel.onBodyWeightChange(it) },
                            label = { Text("Peso Hoy (kg)", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = DarkSurfaceBorder,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard
                            )
                        )

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Volumen Total", color = TextMuted, fontSize = 11.sp)
                                Text(
                                    "${state.totalVolumeKg} kg",
                                    color = EmeraldPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Selector de Ejercicio para añadir series
                item {
                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Seleccionar Ejercicio", color = TextSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable { expanded = true }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = state.selectedExercise?.let { "${it.name} (${it.muscleGroup})" } ?: "Seleccionar ejercicio...",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(DarkSurfaceCard)
                                ) {
                                    state.availableExercises.forEach { exercise ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(exercise.name, color = TextPrimary, fontWeight = FontWeight.Medium)
                                                    Text(exercise.muscleGroup, color = TextMuted, fontSize = 11.sp)
                                                }
                                            },
                                            onClick = {
                                                viewModel.onSelectExercise(exercise)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { viewModel.onAddSet() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldPrimary.copy(alpha = 0.15f),
                                    contentColor = EmeraldPrimary
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Añadir Serie", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Encabezado de Series
                item {
                    Text(
                        text = "SERIES REGISTRADAS (${state.sets.size})",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                // Lista de Series Editables
                if (state.sets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Aún no has agregado series.\nSelecciona un ejercicio arriba y toca 'Añadir Serie'.",
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    items(items = state.sets, key = { it.id }) { editableSet ->
                        SetRowCard(
                            set = editableSet,
                            onUpdate = { w, r, rir ->
                                viewModel.onUpdateSet(editableSet.id, w, r, rir)
                            },
                            onDelete = {
                                viewModel.onDeleteSet(editableSet.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetRowCard(
    set: EditableSet,
    onUpdate: (weight: String, reps: String, rir: Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header con nombre de ejercicio y 1RM estimado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${set.setNumber}",
                            color = EmeraldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = set.exerciseName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 1RM Estimado Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AccentCyan.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "1RM ~ ${set.estimated1Rm} kg",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Inputs de Kg, Reps, RIR y Botón eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Peso (kg)
                OutlinedTextField(
                    value = set.weightText,
                    onValueChange = { onUpdate(it, set.repsText, set.rir) },
                    label = { Text("Kg", fontSize = 10.sp, color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )

                // Reps
                OutlinedTextField(
                    value = set.repsText,
                    onValueChange = { onUpdate(set.weightText, it, set.rir) },
                    label = { Text("Reps", fontSize = 10.sp, color = TextMuted) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    )
                )

                // Selector RIR (0 a 5)
                var rirMenuExpanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { rirMenuExpanded = true }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("RIR", color = TextMuted, fontSize = 9.sp)
                        val rirColor = when (set.rir) {
                            0 -> AccentRed
                            1, 2 -> EmeraldPrimary
                            else -> TextSecondary
                        }
                        Text(
                            text = "${set.rir}",
                            color = rirColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = rirMenuExpanded,
                        onDismissRequest = { rirMenuExpanded = false },
                        modifier = Modifier.background(DarkSurfaceCard)
                    ) {
                        (0..5).forEach { rirVal ->
                            DropdownMenuItem(
                                text = {
                                    val label = if (rirVal == 0) "RIR 0 (Fallo)" else "RIR $rirVal"
                                    Text(label, color = TextPrimary)
                                },
                                onClick = {
                                    onUpdate(set.weightText, set.repsText, rirVal)
                                    rirMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Botón eliminar serie
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar serie",
                        tint = AccentRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
