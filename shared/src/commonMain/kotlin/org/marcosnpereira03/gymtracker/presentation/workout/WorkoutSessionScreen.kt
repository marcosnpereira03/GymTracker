package org.marcosnpereira03.gymtracker.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
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
    var showExercisePicker by remember { mutableStateOf(false) }

    LaunchedEffect(workoutId) {
        viewModel.initSession(workoutId)
    }

    LaunchedEffect(state.isSavedSuccess) {
        if (state.isSavedSuccess) {
            onNavigateBack()
        }
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
                            Text("💾", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (workoutId != null) "Guardar Cambios del Entrenamiento" else "Finalizar y Guardar el entrenamiento",
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
                        if (workoutId != null) {
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
                                text = if (workoutId != null) state.title else "Entrenamiento Diario",
                                color = White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
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
                                    fontWeight = FontWeight.Medium
                                )
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
                                Text("🏋️", fontSize = 32.sp)
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
                    // Agrupación de series por ejercicio
                    val setsByExercise = state.sets.groupBy { it.exerciseId }
                    items(items = setsByExercise.keys.toList(), key = { it }) { exerciseId ->
                        val exerciseSets = setsByExercise[exerciseId] ?: emptyList()
                        val exerciseObj = state.availableExercises.firstOrNull { it.id == exerciseId }
                        val exerciseName = exerciseSets.firstOrNull()?.exerciseName ?: exerciseObj?.name ?: "Ejercicio"
                        val muscleGroup = exerciseObj?.muscleGroup ?: "Pecho"

                        ExerciseWorkoutCard(
                            exerciseId = exerciseId,
                            exerciseName = exerciseName,
                            muscleGroup = muscleGroup,
                            sets = exerciseSets,
                            onAddSet = { viewModel.onAddSetToExercise(exerciseId) },
                            onUpdateSet = { sId, w, r, rir -> viewModel.onUpdateSet(sId, w, r, rir) },
                            onDeleteSet = { sId -> viewModel.onDeleteSet(sId) },
                            onDeleteExercise = { viewModel.onDeleteExercise(exerciseId) }
                        )
                    }
                }

                // Botón "+ Añadir Ejercicio a la Sesión"
                item {
                    var pickerExpanded by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Zinc900)
                            .border(1.dp, Zinc800, RoundedCornerShape(14.dp))
                            .clickable { pickerExpanded = true }
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

                        DropdownMenu(
                            expanded = pickerExpanded,
                            onDismissRequest = { pickerExpanded = false },
                            modifier = Modifier.background(Zinc900)
                        ) {
                            state.availableExercises.forEach { exercise ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(exercise.name, color = White, fontWeight = FontWeight.Medium)
                                            Text(exercise.muscleGroup, color = Zinc400, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onSelectExercise(exercise)
                                        viewModel.onAddSet()
                                        pickerExpanded = false
                                    }
                                )
                            }
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
    sets: List<EditableSet>,
    onAddSet: () -> Unit,
    onUpdateSet: (setId: String, weight: String, reps: String, rir: Int) -> Unit,
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
            // Header del ejercicio (Músculo + Tag + Flechas + Borrar)
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
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Zinc800)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Máquina", color = Zinc400, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = Zinc500, modifier = Modifier.size(18.dp))
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Zinc500, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDeleteExercise, modifier = Modifier.size(24.dp)) {
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

            // Banner de Última Vez (Historial rápido)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Zinc950)
                    .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Emerald400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Última vez (2026-10-01): ", color = Zinc400, fontSize = 11.sp)
                    val lastWeight = sets.firstOrNull()?.weightText?.ifBlank { "50" } ?: "50"
                    val lastReps = sets.firstOrNull()?.repsText?.ifBlank { "15" } ?: "15"
                    Text("$lastWeight kg × $lastReps (RIR 1)", color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text("Ver más ↗", color = Emerald400, fontSize = 11.sp, fontWeight = FontWeight.Medium)
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

@Composable
fun CompactSetRow(
    set: EditableSet,
    onUpdate: (weight: String, reps: String, rir: Int) -> Unit,
    onDelete: () -> Unit
) {
    var isDone by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Zinc950)
            .border(1.dp, if (isDone) Emerald400.copy(alpha = 0.5f) else Zinc800, RoundedCornerShape(8.dp))
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

        // Columna LISTO (Botón verde con checkmark + icono de basura)
        Row(
            modifier = Modifier.weight(1.3f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isDone) Emerald400 else Emerald500)
                    .clickable { isDone = !isDone },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Listo",
                    tint = Color.Black,
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

