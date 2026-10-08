package org.marcosnpereira03.gymtracker.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.marcosnpereira03.gymtracker.domain.model.Exercise
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    viewModel: WorkoutSessionViewModel,
    workoutId: String? = null,
    initialDate: String? = null,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }

    val actualWorkoutId = remember(workoutId) {
        if (workoutId.isNullOrBlank() || workoutId == "{workoutId}") null else workoutId
    }
    val actualDate = remember(initialDate) {
        if (initialDate.isNullOrBlank() || initialDate == "{date}") null else initialDate
    }

    LaunchedEffect(actualWorkoutId, actualDate) {
        viewModel.initSession(actualWorkoutId, actualDate)
    }

    LaunchedEffect(state.isSavedSuccess) {
        if (state.isSavedSuccess) {
            viewModel.onResetSavedSuccess()
            onNavigateBack()
        }
    }

    // Modal de Historial Detallado del Ejercicio
    if (state.viewingHistoryExerciseId != null) {
        ExerciseHistoryDialog(
            viewModel = viewModel,
            exerciseId = state.viewingHistoryExerciseId!!,
            onDismiss = { viewModel.onCloseExerciseHistory() }
        )
    }

    // Modal Selector de Ejercicios con buscador y filtros
    if (showExercisePicker) {
        ExercisePickerDialog(
            exercises = state.availableExercises,
            onSelectExercise = { exercise ->
                viewModel.onSelectExercise(exercise)
                viewModel.onAddSet()
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false }
        )
    }

    Scaffold(
        containerColor = Zinc950,
        bottomBar = {
            // Mostrar botón de guardar solo si hay series/ejercicios agregados
            if (state.sets.isNotEmpty()) {
                Surface(
                    color = Zinc950,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Button(
                        onClick = { viewModel.onSaveWorkout() },
                        enabled = !state.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald400,
                            contentColor = Color.Black
                        )
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (actualWorkoutId != null) "Guardar Cambios del Entrenamiento" else "Finalizar y Guardar el entrenamiento",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Zinc950)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Emerald400)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                // Header superior: Modo y Fecha
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (actualWorkoutId != null) {
                            // MODO EDICIÓN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Zinc900)
                                        .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                                        .clickable { onNavigateBack() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Volver",
                                        tint = White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Volver", color = White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }

                                Text(
                                    text = "MODO EDICIÓN",
                                    color = Color(0xFFFBBF24), // Amarillo ámbar
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        } else {
                            // ENTRENAMIENTO EN VIVO
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Emerald400)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ENTRENAMIENTO EN VIVO",
                                        color = Emerald400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clickable { viewModel.onResetSession() }
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Reiniciar",
                                        tint = Zinc400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reiniciar", color = Zinc400, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Título de la sesión + Fecha en píldora
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (actualWorkoutId != null) state.title else "Entrenamiento Diario",
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(end = 8.dp)
                            )

                            // Date Badge Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Zinc900)
                                    .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Emerald400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val dateStr = state.date?.toString()?.substringBefore("T") ?: "04/10/2026"
                                Text(
                                    text = dateStr,
                                    color = White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                    }
                }

                // Error Banner si ocurre un problema de validación o red
                if (state.errorMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Red500.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Red500.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Red500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.errorMessage ?: "",
                                        color = Red500,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.onClearError() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = Red500,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Notas de la sesión
                item {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = { viewModel.onNotesChange(it) },
                        placeholder = { Text("Notas de la sesión (energía, sensaciones, etc.)", color = Zinc500, fontSize = 13.sp) },
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
                        singleLine = true
                    )
                }

                // Empty state si no hay ejercicios cargados
                if (state.sets.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Zinc900.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Zinc800)
                                        .border(1.dp, Emerald500.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FitnessCenter,
                                        tint = Emerald400,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No hay ejercicios agregados aún",
                                    color = White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toca el botón de abajo para seleccionar el primer ejercicio que vas a realizar.",
                                    color = Zinc400,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Agrupación de series por ejercicio en orden
                    val distinctExerciseIds = state.sets.map { it.exerciseId }.distinct()
                    val setsByExercise = state.sets.groupBy { it.exerciseId }

                    items(items = distinctExerciseIds, key = { it }) { exerciseId ->
                        val exerciseSets = setsByExercise[exerciseId] ?: emptyList()
                        val exerciseObj = state.availableExercises.firstOrNull { it.id == exerciseId }
                        val exerciseName = exerciseSets.firstOrNull()?.exerciseName ?: exerciseObj?.name ?: "Ejercicio"
                        val muscleGroup = exerciseObj?.muscleGroup ?: "Pecho"
                        val equipment = exerciseObj?.equipment

                        val exerciseIndex = distinctExerciseIds.indexOf(exerciseId)
                        val canMoveUp = exerciseIndex > 0
                        val canMoveDown = exerciseIndex < distinctExerciseIds.size - 1
                        val lastSessionSummary = viewModel.getLastSessionSummary(exerciseId)

                        ExerciseWorkoutCard(
                            exerciseId = exerciseId,
                            exerciseName = exerciseName,
                            muscleGroup = muscleGroup,
                            equipment = equipment,
                            sets = exerciseSets,
                            canMoveUp = canMoveUp,
                            canMoveDown = canMoveDown,
                            lastSessionSummary = lastSessionSummary,
                            onMoveUp = { viewModel.onMoveExerciseUp(exerciseId) },
                            onMoveDown = { viewModel.onMoveExerciseDown(exerciseId) },
                            onOpenHistory = { viewModel.onOpenExerciseHistory(exerciseId) },
                            onAddSet = { viewModel.onAddSetToExercise(exerciseId) },
                            onUpdateSet = { sId, w, r, rir -> viewModel.onUpdateSet(sId, w, r, rir) },
                            onToggleSetCompleted = { sId -> viewModel.onToggleSetCompleted(sId) },
                            onDeleteSet = { sId -> viewModel.onDeleteSet(sId) },
                            onDeleteExercise = { viewModel.onDeleteExercise(exerciseId) }
                        )
                    }
                }

                // Botón "+ Añadir Ejercicio a la Sesión"
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Zinc900)
                            .border(1.dp, Zinc800, RoundedCornerShape(14.dp))
                            .clickable { showExercisePicker = true }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Añadir Ejercicio a la Sesión",
                                color = White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseWorkoutCard(
    exerciseId: String,
    exerciseName: String,
    muscleGroup: String,
    equipment: String? = null,
    sets: List<EditableSet>,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    lastSessionSummary: Pair<String, String>?,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onOpenHistory: () -> Unit,
    onAddSet: () -> Unit,
    onUpdateSet: (setId: String, weight: String, reps: String, rir: Int) -> Unit,
    onToggleSetCompleted: (setId: String) -> Unit,
    onDeleteSet: (setId: String) -> Unit,
    onDeleteExercise: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header del ejercicio (Músculo + Tag + Flechas de orden + Borrar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = muscleGroup.uppercase(),
                        color = Emerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    if (!equipment.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Zinc800)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(equipment, color = Zinc400, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flecha Arriba
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Mover ejercicio arriba",
                            tint = if (canMoveUp) Zinc300 else Zinc700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Flecha Abajo
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Mover ejercicio abajo",
                            tint = if (canMoveDown) Zinc300 else Zinc700,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Botón Eliminar
                    IconButton(onClick = onDeleteExercise, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar Ejercicio", tint = Zinc500, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = exerciseName.uppercase(),
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Banner de Última Vez (Historial rápido estructurado en 2 filas y Ver más a la derecha)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Zinc950)
                    .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                    .clickable { onOpenHistory() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (lastSessionSummary != null) "Última vez (${lastSessionSummary.first}):" else "Sin registros anteriores",
                            color = Zinc400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (lastSessionSummary != null) {
                        Text(
                            text = lastSessionSummary.second,
                            color = Emerald400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Emerald950.copy(alpha = 0.35f))
                        .border(1.dp, Emerald500.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ver más ↗",
                        color = Emerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Encabezados de la Tabla: SERIE | KG | REPS | RIR | LISTO
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("SERIE", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.9f))
                Text("KG", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.5f))
                Text("REPS", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.2f))
                Text("RIR", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(0.9f))
                Text("LISTO", color = Zinc400, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filas de series compactas
            sets.forEach { set ->
                CompactSetRow(
                    set = set,
                    onUpdate = { w, r, rir -> onUpdateSet(set.id, w, r, rir) },
                    onToggleCompleted = { onToggleSetCompleted(set.id) },
                    onDelete = { onDeleteSet(set.id) }
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Botón "+ Añadir Serie"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                    .clickable { onAddSet() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("+", color = Zinc300, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir Serie", color = Zinc300, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * Modal detallado del historial de un ejercicio con sesiones anteriores, récord histórico y selector de límite.
 */
@Composable
fun ExerciseHistoryDialog(
    viewModel: WorkoutSessionViewModel,
    exerciseId: String,
    onDismiss: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val exerciseObj = state.availableExercises.firstOrNull { it.id == exerciseId }
    val exerciseName = exerciseObj?.name ?: "Ejercicio"

    val pastSessions = viewModel.getPastSessionsForExercise(exerciseId, state.historyLimit)
    val totalPastSessionsCount = viewModel.getTotalPastSessionsCount(exerciseId)
    val bestRecord = viewModel.getBestRecordForExercise(exerciseId)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header: Ícono + Título + Subtítulo + Botón ✕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Zinc800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = exerciseName.uppercase(),
                                color = White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Historial de registros anteriores",
                                color = Zinc400,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Zinc400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Banner de Récord Histórico Registrado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Zinc950)
                        .border(1.dp, Emerald500.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Récord histórico registrado:",
                        color = Zinc300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (bestRecord != null) {
                        val weightStr = if (bestRecord.weightKg % 1.0 == 0.0) "${bestRecord.weightKg.toInt()}" else "${bestRecord.weightKg}"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$weightStr kg × ${bestRecord.reps} reps",
                                color = Emerald400,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "Sin registros",
                            color = Zinc500,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Límite de Sesiones: "Mostrando X de Y sesiones" + Chips [5] [10] [30]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mostrando ${pastSessions.size} de $totalPastSessionsCount sesiones",
                        color = Zinc400,
                        fontSize = 12.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5, 10, 30).forEach { limit ->
                            val isSelected = state.historyLimit == limit
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Emerald400 else Zinc800)
                                    .clickable { viewModel.onSetHistoryLimit(limit) }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$limit",
                                    color = if (isSelected) Color.Black else Zinc400,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contenedor Scrollable de Sesiones Anteriores
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    if (pastSessions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay entrenamientos previos registrados con este ejercicio.",
                                color = Zinc500,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items = pastSessions, key = { it.workoutId }) { session ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Zinc950),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Top Row: Fecha + Tag de la Sesión
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.DateRange,
                                                    contentDescription = null,
                                                    tint = Emerald400,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                val dateFormatted = session.workoutDate.toString().substringBefore("T")
                                                Text(
                                                    text = dateFormatted,
                                                    color = White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Zinc800)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = session.workoutTitle.uppercase(),
                                                    color = Zinc300,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Encabezados de la tabla
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("SERIE", color = Zinc400, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                            Text("PESO", color = Zinc400, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1.3f))
                                            Text("REPS", color = Zinc400, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                            Text("RIR", color = Zinc400, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Filas de series
                                        session.sets.forEach { pastSet ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Zinc900)
                                                    .padding(vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "#${pastSet.setNumber}",
                                                    color = Zinc400,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                val weightStr = if (pastSet.weightKg % 1.0 == 0.0) "${pastSet.weightKg.toInt()} kg" else "${pastSet.weightKg} kg"
                                                Text(
                                                    text = weightStr,
                                                    color = Emerald400,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1.3f)
                                                )

                                                Text(
                                                    text = "${pastSet.reps}",
                                                    color = White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )

                                                Text(
                                                    text = "${pastSet.rir}",
                                                    color = White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Botón Cerrar inferior
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Zinc800,
                        contentColor = White
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Zinc700)
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CompactSetRow(
    set: EditableSet,
    onUpdate: (weight: String, reps: String, rir: Int) -> Unit,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Zinc950)
            .border(1.dp, if (set.isCompleted) Emerald400.copy(alpha = 0.5f) else Zinc800, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna SERIE (#1, #2...)
        Text(
            text = "#${set.setNumber}",
            color = Zinc400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.9f)
        )

        // Columna KG (Input oscuro con sufijo kg)
        Box(
            modifier = Modifier
                .weight(1.5f)
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Zinc900)
                .border(1.dp, Zinc800, RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = set.weightText,
                    onValueChange = { onUpdate(it, set.repsText, set.rir) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(Emerald400),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Text("kg", color = Zinc500, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Columna REPS (Input oscuro con número de repeticiones)
        Box(
            modifier = Modifier
                .weight(1.2f)
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Zinc900)
                .border(1.dp, Zinc800, RoundedCornerShape(6.dp))
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = set.repsText,
                onValueChange = { onUpdate(set.weightText, it, set.rir) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                cursorBrush = SolidColor(Emerald400),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Columna RIR (Selector o Input directo de RIR)
        var rirMenuExpanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .weight(0.9f)
                .height(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Zinc900)
                .border(1.dp, Zinc800, RoundedCornerShape(6.dp))
                .clickable { rirMenuExpanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.rir}",
                color = if (set.rir == 0) Red500 else White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            DropdownMenu(
                expanded = rirMenuExpanded,
                onDismissRequest = { rirMenuExpanded = false },
                modifier = Modifier.background(Zinc900)
            ) {
                (0..5).forEach { rirVal ->
                    DropdownMenuItem(
                        text = { Text("RIR $rirVal", color = White, fontWeight = FontWeight.Bold) },
                        onClick = {
                            onUpdate(set.weightText, set.repsText, rirVal)
                            rirMenuExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Columna LISTO (Botón verde si está lista, gris si está pendiente)
        Row(
            modifier = Modifier.weight(1.3f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (set.isCompleted) Emerald400 else Zinc800)
                    .border(
                        1.dp,
                        if (set.isCompleted) Emerald500 else Zinc700,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onToggleCompleted() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (set.isCompleted) "Completada" else "Pendiente",
                    tint = if (set.isCompleted) Color.Black else Zinc500,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar Serie", tint = Zinc500, modifier = Modifier.size(14.dp))
            }
        }
    }
}

/**
 * Modal de selección de ejercicios con buscador en tiempo real, filtros por grupo muscular y etiquetas de equipamiento.
 */
@Composable
fun ExercisePickerDialog(
    exercises: List<Exercise>,
    onSelectExercise: (Exercise) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("Todos") }

    val defaultMuscleGroups = listOf("Todos", "Pecho", "Espalda", "Cuádriceps", "Isquios", "Glúteos", "Hombros", "Bíceps", "Tríceps", "Abdomen", "Piernas")
    val dynamicGroups = remember(exercises) {
        val extracted = exercises.map { it.muscleGroup.trim() }.filter { it.isNotBlank() }.distinct()
        (listOf("Todos") + extracted).distinct()
    }
    val muscleCategories = if (dynamicGroups.size > 1) dynamicGroups else defaultMuscleGroups

    val filteredExercises = remember(exercises, searchQuery, selectedMuscleGroup) {
        exercises.filter { ex ->
            val matchesSearch = searchQuery.isBlank() ||
                    ex.name.contains(searchQuery, ignoreCase = true) ||
                    ex.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                    (ex.equipment?.contains(searchQuery, ignoreCase = true) == true)

            val matchesMuscle = selectedMuscleGroup.equals("Todos", ignoreCase = true) ||
                    ex.muscleGroup.equals(selectedMuscleGroup, ignoreCase = true)

            matchesSearch && matchesMuscle
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Zinc900),
            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header: Ícono + Título + Botón ✕
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Seleccionar Ejercicio",
                            color = White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Zinc400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por nombre...", color = Zinc500, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Zinc500,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Borrar búsqueda",
                                    tint = Zinc400,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Zinc800,
                        focusedContainerColor = Zinc950,
                        unfocusedContainerColor = Zinc950
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filtros de grupo muscular (Chips horizontales con scroll)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = muscleCategories) { muscle ->
                        val isSelected = selectedMuscleGroup.equals(muscle, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Emerald400 else Zinc800)
                                .border(
                                    1.dp,
                                    if (isSelected) Emerald500 else Zinc700,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedMuscleGroup = muscle }
                                .padding(horizontal = 14.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = muscle,
                                color = if (isSelected) Color.Black else Zinc300,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lista de ejercicios scrolleable
                if (filteredExercises.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron ejercicios con ese criterio",
                            color = Zinc500,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = filteredExercises, key = { it.id }) { exercise ->
                            ExercisePickerItemCard(
                                exercise = exercise,
                                onClick = { onSelectExercise(exercise) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExercisePickerItemCard(
    exercise: Exercise,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc950),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name.uppercase(),
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exercise.muscleGroup,
                    color = Zinc400,
                    fontSize = 12.sp
                )
            }

            if (!exercise.equipment.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Zinc800)
                        .border(1.dp, Zinc700, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = exercise.equipment,
                        color = Zinc300,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


