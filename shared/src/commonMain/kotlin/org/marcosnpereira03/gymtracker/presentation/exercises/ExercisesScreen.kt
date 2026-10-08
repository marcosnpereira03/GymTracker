package org.marcosnpereira03.gymtracker.presentation.exercises

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.domain.model.ExerciseHistoryItem
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: ExercisesViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadExercises()
    }

    if (showCreateDialog) {
        CreateExerciseDialog(
            muscleGroups = state.muscleGroups.filterNot { it == "Todos" },
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, muscle ->
                viewModel.onCreateCustomExercise(name, muscle)
                showCreateDialog = false
            }
        )
    }

    exerciseToEdit?.let { exercise ->
        EditExerciseDialog(
            exercise = exercise,
            muscleGroups = state.muscleGroups.filterNot { it == "Todos" },
            onDismiss = { exerciseToEdit = null },
            onConfirm = { updated ->
                viewModel.onUpdateExercise(updated)
                exerciseToEdit = null
            }
        )
    }

    exerciseToDelete?.let { exercise ->
        DeleteExerciseConfirmDialog(
            exercise = exercise,
            onDismiss = { exerciseToDelete = null },
            onConfirm = {
                viewModel.onDeleteExercise(exercise.id)
                exerciseToDelete = null
            }
        )
    }

    Scaffold(
        containerColor = Zinc950,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Catálogo de Ejercicios",
                            color = White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${state.exercises.size} ejercicios registrados",
                            color = Zinc400,
                            fontSize = 12.sp
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Emerald400)
                            .clickable { showCreateDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo Ejercicio",
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Zinc950
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Buscador
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                placeholder = { Text("Buscar ejercicio...", color = Zinc500, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Zinc500)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = Emerald400,
                    unfocusedBorderColor = Zinc800,
                    focusedContainerColor = Zinc900,
                    unfocusedContainerColor = Zinc900
                ),
                singleLine = true
            )

            // Chips de filtro por grupo muscular (horizontal scroll)
            val defaultFilterCategories = listOf("Todos", "Pecho", "Espalda", "Cuádriceps", "Isquios", "Glúteos", "Hombros", "Bíceps", "Tríceps")
            val availableCategories = (listOf("Todos") + (state.muscleGroups.filterNot { it == "Todos" })).distinct()
            val categoriesToShow = if (availableCategories.size > 1) availableCategories else defaultFilterCategories

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categoriesToShow) { muscle ->
                    val isSelected = state.selectedMuscleGroup == muscle || (muscle == "Todos" && (state.selectedMuscleGroup == "Todos" || state.selectedMuscleGroup.isBlank()))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Emerald400 else Zinc800)
                            .clickable { viewModel.onSelectMuscleGroup(muscle) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = muscle,
                            color = if (isSelected) Color.Black else Zinc400,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Emerald400)
                }
            } else if (state.filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron ejercicios.", color = Zinc500, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(items = state.filteredExercises, key = { it.exercise.id }) { item ->
                        val isExpanded = state.expandedExerciseId == item.exercise.id
                        ExerciseItemCard(
                            data = item,
                            isExpanded = isExpanded,
                            onToggleExpand = { viewModel.onToggleExpandExercise(item.exercise.id) },
                            onEdit = { exerciseToEdit = item.exercise },
                            onDelete = { exerciseToDelete = item.exercise }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseItemCard(
    data: ExerciseCardData,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono Mancuerna estilizado
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Zinc800)
                        .border(1.dp, Emerald500.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.exercise.name.uppercase(),
                        color = White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${data.exercise.muscleGroup} • Máquina / Peso Libre",
                        color = Zinc400,
                        fontSize = 12.sp
                    )
                }

                // 1RM Récord Badge si existe
                if (data.maxEstimated1Rm > 0.0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Emerald900.copy(alpha = 0.5f))
                            .border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${data.maxEstimated1Rm} kg",
                            color = Emerald400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sección expandida con acciones (Editar / Eliminar) e historial
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botones de acción Editar y Eliminar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc700),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Zinc800.copy(alpha = 0.6f),
                                contentColor = White
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Red500.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Red500.copy(alpha = 0.1f),
                                contentColor = Red500
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Red500,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eliminar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (data.history.isNotEmpty()) {
                        Text(
                            text = "Últimas series realizadas:",
                            color = Zinc400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        data.history.take(5).forEach { historyItem ->
                            HistorySetRow(historyItem)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorySetRow(item: ExerciseHistoryItem) {
    val dateStr = item.workoutDate.toString().substringBefore("T")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(DarkSurface.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$dateStr • Serie ${item.setNumber}",
            color = TextMuted,
            fontSize = 11.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "${item.weightKg} kg × ${item.reps} reps",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            val rirLabel = if (item.rir == 0) "RIR 0" else "RIR ${item.rir}"
            Text(
                text = "($rirLabel)",
                color = if (item.rir == 0) AccentRed else EmeraldPrimary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun CreateExerciseDialog(
    muscleGroups: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, muscle: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf(muscleGroups.firstOrNull() ?: "Pecho") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Ejercicio", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Ejercicio", color = Zinc400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Zinc700,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    ),
                    singleLine = true
                )

                Text("Grupo Muscular:", color = Zinc400, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(muscleGroups) { muscle ->
                        val isSelected = selectedMuscle == muscle
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMuscle = muscle },
                            label = { Text(muscle, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald400,
                                selectedLabelColor = Color.Black,
                                containerColor = Zinc800,
                                labelColor = Zinc300
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedMuscle) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Color.Black)
            ) {
                Text("Crear", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Zinc400)
            }
        },
        containerColor = Zinc900
    )
}

@Composable
fun EditExerciseDialog(
    exercise: Exercise,
    muscleGroups: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (updated: Exercise) -> Unit
) {
    var name by remember { mutableStateOf(exercise.name) }
    var selectedMuscle by remember { mutableStateOf(exercise.muscleGroup) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Ejercicio", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Ejercicio", color = Zinc400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Zinc700,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    ),
                    singleLine = true
                )

                Text("Grupo Muscular:", color = Zinc400, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(muscleGroups) { muscle ->
                        val isSelected = selectedMuscle.equals(muscle, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMuscle = muscle },
                            label = { Text(muscle, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald400,
                                selectedLabelColor = Color.Black,
                                containerColor = Zinc800,
                                labelColor = Zinc300
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(exercise.copy(name = name.trim(), muscleGroup = selectedMuscle.trim()))
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Color.Black)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Zinc400)
            }
        },
        containerColor = Zinc900
    )
}

@Composable
fun DeleteExerciseConfirmDialog(
    exercise: Exercise,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("¿Eliminar ejercicio?", color = White, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                text = "¿Estás seguro de que deseas eliminar \"${exercise.name}\" del catálogo? Esta acción no se puede deshacer.",
                color = Zinc300,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Red500, contentColor = White)
            ) {
                Text("Eliminar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Zinc400)
            }
        },
        containerColor = Zinc900
    )
}

