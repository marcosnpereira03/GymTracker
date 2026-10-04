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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.domain.model.ExerciseHistoryItem
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    viewModel: ExercisesViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Catálogo de Ejercicios",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Nuevo Ejercicio",
                            tint = EmeraldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
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
                placeholder = { Text("Buscar por nombre o músculo...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
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

            // Chips de filtro por grupo muscular
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.muscleGroups) { muscle ->
                    val isSelected = state.selectedMuscleGroup == muscle
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.onSelectMuscleGroup(muscle) },
                        label = {
                            Text(
                                text = muscle,
                                color = if (isSelected) TextOnEmerald else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldPrimary,
                            containerColor = DarkSurfaceCard
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) EmeraldPrimary else DarkSurfaceBorder,
                            enabled = true,
                            selected = isSelected
                        )
                    )
                }
            }

            if (state.isLoading && state.exercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            } else if (state.filteredExercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron ejercicios.", color = TextMuted, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(items = state.filteredExercises, key = { it.exercise.id }) { item ->
                        val isExpanded = state.expandedExerciseId == item.exercise.id
                        ExerciseItemCard(
                            data = item,
                            isExpanded = isExpanded,
                            onToggleExpand = { viewModel.onToggleExpandExercise(item.exercise.id) }
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
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.exercise.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.exercise.muscleGroup,
                        color = EmeraldPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 1RM Récord Badge
                if (data.maxEstimated1Rm > 0.0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Récord: ${data.maxEstimated1Rm} kg",
                            color = AccentCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón de expansión para ver el historial de series
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurface)
                    .clickable { onToggleExpand() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (data.history.isNotEmpty()) "Ver últimas series (${data.history.size})" else "Sin series registradas",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Despliegue de series anteriores organizadas por fecha de realización
            AnimatedVisibility(visible = isExpanded && data.history.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    data.history.take(6).forEach { historyItem ->
                        HistorySetRow(historyItem)
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
        title = { Text("Nuevo Ejercicio", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Ejercicio", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text("Grupo Muscular:", color = TextSecondary, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(muscleGroups) { muscle ->
                        val isSelected = selectedMuscle == muscle
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedMuscle = muscle },
                            label = { Text(muscle, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedMuscle) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnEmerald)
            ) {
                Text("Crear", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = DarkSurfaceCard
    )
}
