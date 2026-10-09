package org.marcosnpereira03.gymtracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEditWorkout: (String) -> Unit,
    onNewWorkout: (String?) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Por Calendario, 1 = Por Ejercicio
    val today = remember { org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.today() }
    var selectedDate by remember { mutableStateOf(today) }
    var displayedYear by remember { mutableStateOf(today.year) }
    var displayedMonth by remember { mutableStateOf(today.month.ordinal + 1) }
    var workoutToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    if (workoutToDelete != null) {
        AlertDialog(
            onDismissRequest = { workoutToDelete = null },
            title = { Text("Eliminar entrenamiento", color = White) },
            text = { Text("¿Deseas eliminar '${workoutToDelete?.second}' del historial?", color = Zinc400) },
            confirmButton = {
                TextButton(onClick = {
                    workoutToDelete?.first?.let { viewModel.onDeleteWorkout(it) }
                    workoutToDelete = null
                }) {
                    Text("Eliminar", color = Red500, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { workoutToDelete = null }) {
                    Text("Cancelar", color = Zinc400)
                }
            },
            containerColor = Zinc900
        )
    }

    Scaffold(
        containerColor = Zinc950
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                // Header: Historial de Entreno
                item {
                    Column {
                        Text(
                            text = "Historial de Entreno",
                            color = White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Consulta, edita o elimina tus sesiones anteriores",
                            color = Zinc400,
                            fontSize = 13.sp
                        )
                    }
                }

                // Dual Tab Toggle: [ Por Calendario ] vs [ Por Ejercicio ]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tab 1: Por Calendario
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 0) Emerald400 else Zinc900)
                                .border(1.dp, if (selectedTab == 0) Emerald400 else Zinc800, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 0 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (selectedTab == 0) Color.Black else Zinc400,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Por Calendario",
                                    color = if (selectedTab == 0) Color.Black else Zinc400,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Tab 2: Por Ejercicio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedTab == 1) Emerald400 else Zinc900)
                                .border(1.dp, if (selectedTab == 1) Emerald400 else Zinc800, RoundedCornerShape(10.dp))
                                .clickable { selectedTab = 1 }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) Color.Black else Zinc400,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Por Ejercicio",
                                    color = if (selectedTab == 1) Color.Black else Zinc400,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

            if (selectedTab == 0) {
                // ==========================================
                // VISTA 1: POR CALENDARIO (DINÁMICO)
                // ==========================================
                val selectedDateStr = selectedDate.toString()
                val matchingWorkouts = state.workouts.filter {
                    org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.toLocalDateString(it.workout.date) == selectedDateStr
                }

                // Días del mes con entrenamientos registrados (para los puntitos verdes)
                val monthPrefix = "${displayedYear}-${if (displayedMonth < 10) "0$displayedMonth" else "$displayedMonth"}-"
                val daysWithWorkouts = state.workouts.mapNotNull {
                    val dStr = org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil.toLocalDateString(it.workout.date)
                    if (dStr.startsWith(monthPrefix)) {
                        dStr.substringAfterLast("-").toIntOrNull()
                    } else null
                }.toSet()

                item {
                    CalendarCard(
                        selectedDate = selectedDate,
                        displayedYear = displayedYear,
                        displayedMonth = displayedMonth,
                        daysWithWorkouts = daysWithWorkouts,
                        onPreviousMonth = {
                            if (displayedMonth == 1) {
                                displayedMonth = 12
                                displayedYear -= 1
                            } else {
                                displayedMonth -= 1
                            }
                        },
                        onNextMonth = {
                            if (displayedMonth == 12) {
                                displayedMonth = 1
                                displayedYear += 1
                            } else {
                                displayedMonth += 1
                            }
                        },
                        onSelectDay = { date -> selectedDate = date }
                    )
                }

                // Header de Sesión del día seleccionado
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SESIÓN DEL $selectedDateStr",
                            color = Zinc400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        if (matchingWorkouts.isNotEmpty()) {
                            Text(
                                text = "${matchingWorkouts.size} entrenamiento${if (matchingWorkouts.size > 1) "s" else ""}",
                                color = Emerald400,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Lista de entrenamientos o Empty State si no hay entrenamientos
                if (matchingWorkouts.isEmpty()) {
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
                                    .padding(vertical = 32.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Zinc800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = null,
                                        tint = Zinc400,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Sin entrenamientos registrados el $selectedDateStr",
                                    color = White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "¿Entrenaste este día y no lo anotaste?",
                                    color = Zinc400,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedButton(
                                    onClick = { onNewWorkout(selectedDateStr) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald400),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Emerald400
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Anotar entrenamiento en esta fecha", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(items = matchingWorkouts, key = { it.workout.id }) { item ->
                        CalendarWorkoutCard(
                            detail = item,
                            availableExercises = state.availableExercises,
                            onEdit = { onEditWorkout(item.workout.id) },
                            onDelete = { viewModel.onDeleteWorkout(item.workout.id) }
                        )
                    }
                }
            } else {
                // ==========================================
                // VISTA 2: POR EJERCICIO (CON DROPDOWN SELECTOR)
                // ==========================================
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Seleccionar Ejercicio",
                            color = Zinc400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        var dropdownExpanded by remember { mutableStateOf(false) }
                        val selectedExercise = state.availableExercises.firstOrNull { it.id == state.selectedExerciseId }
                            ?: state.availableExercises.firstOrNull()

                        val selectedExerciseLabel = if (selectedExercise != null) {
                            "${selectedExercise.name.uppercase()} (${selectedExercise.muscleGroup})"
                        } else {
                            "APERTURAS EN POLEA (Pecho)"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Zinc900)
                                .border(1.dp, Emerald400, RoundedCornerShape(10.dp))
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedExerciseLabel,
                                    color = White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Desplegar lista de ejercicios",
                                    tint = Zinc400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(Zinc900)
                                    .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                            ) {
                                state.availableExercises.forEach { exercise ->
                                    val isCurrentSelected = exercise.id == state.selectedExerciseId
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${exercise.name.uppercase()} (${exercise.muscleGroup})",
                                                color = if (isCurrentSelected) Emerald400 else White,
                                                fontSize = 13.sp,
                                                fontWeight = if (isCurrentSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.onSelectExercise(exercise.id)
                                            dropdownExpanded = false
                                        },
                                        modifier = Modifier.background(
                                            if (isCurrentSelected) Color(0xFF064E3B).copy(alpha = 0.5f) else Color.Transparent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Tarjeta resumen del Ejercicio seleccionado + PR
                val activeExercise = state.availableExercises.firstOrNull { it.id == state.selectedExerciseId }
                    ?: state.availableExercises.firstOrNull()

                if (activeExercise != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Zinc900),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeExercise.muscleGroup.uppercase(),
                                        color = Emerald400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )

                                    if (state.bestPrString != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.EmojiEvents,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24), // Gold / Amber PR
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "PR: ${state.bestPrString}",
                                                color = Color(0xFFFBBF24), // Gold / Amber PR
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = activeExercise.name.uppercase(),
                                    color = White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Filtro de Registros Históricos: [ 5 ] [ 10 ] [ 30 ]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REGISTROS HISTÓRICOS",
                            color = Zinc400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Zinc900)
                                .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            listOf(5, 10, 30).forEach { count ->
                                val isSelected = state.recordLimit == count
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) Emerald400 else Color.Transparent)
                                        .clickable { viewModel.onSelectRecordLimit(count) }
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count",
                                        color = if (isSelected) Color.Black else Zinc400,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Lista de Registros Históricos del ejercicio
                if (state.exerciseRecords.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Zinc900)
                                .border(1.dp, Zinc800, RoundedCornerShape(12.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sin registros para este ejercicio.",
                                color = Zinc500,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(items = state.exerciseRecords, key = { "${it.workoutId}-${it.workoutDate}" }) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Zinc900),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Header: Fecha • Título (con elipsis para no superponerse) + Botones Editar (azul) / Borrar (rojo)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = null,
                                            tint = Zinc400,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${record.workoutDate}  •  ${record.workoutTitle}",
                                            color = White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.wrapContentWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        HoverableActionIcon(
                                            icon = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            defaultTint = Zinc400,
                                            hoverTint = Color(0xFF60A5FA), // Blue hover
                                            onClick = { onEditWorkout(record.workoutId) }
                                        )
                                        HoverableActionIcon(
                                            icon = Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            defaultTint = Zinc400,
                                            hoverTint = Red500, // Red hover
                                            onClick = { workoutToDelete = Pair(record.workoutId, record.workoutTitle) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Series registradas
                                record.sets.forEach { set ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Zinc950)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Serie #${set.setNumber}",
                                            color = Zinc400,
                                            fontSize = 12.sp
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${set.weightKg.toInt()} kg",
                                                color = Emerald400,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "×  ${set.reps} reps",
                                                color = White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Zinc800)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "RIR ${set.rir}",
                                                    color = if (set.rir == 0) Red500 else Zinc400,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
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
fun CalendarCard(
    selectedDate: kotlinx.datetime.LocalDate,
    displayedYear: Int,
    displayedMonth: Int,
    daysWithWorkouts: Set<Int>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (kotlinx.datetime.LocalDate) -> Unit
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val monthTitle = "${monthNames.getOrElse(displayedMonth - 1) { "Mes" }} De $displayedYear"

    // Calcular días en el mes mostrado
    val isLeap = (displayedYear % 4 == 0 && displayedYear % 100 != 0) || (displayedYear % 400 == 0)
    val totalDays = when (displayedMonth) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeap) 29 else 28
        else -> 31
    }

    val firstDayOfMonth = kotlinx.datetime.LocalDate(displayedYear, displayedMonth, 1)
    val startOffset = firstDayOfMonth.dayOfWeek.ordinal // 0=LU, 1=MA, 2=MI, 3=JU, 4=VI, 5=SÁ, 6=DO
    val rows = (totalDays + startOffset + 6) / 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del calendario (Mes + flechas navegables)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = monthTitle,
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onPreviousMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mes anterior",
                            tint = Zinc400,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onNextMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Mes siguiente",
                            tint = Zinc400,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Días de la semana
            val daysOfWeek = listOf("LU", "MA", "MI", "JU", "VI", "SÁ", "DO")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        color = Zinc400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grid dinámico de días del mes
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val dayNumber = (row * 7 + col) - startOffset + 1
                            if (dayNumber in 1..totalDays) {
                                val isSelected = selectedDate.year == displayedYear &&
                                        (selectedDate.month.ordinal + 1) == displayedMonth &&
                                        selectedDate.day == dayNumber
                                val hasWorkout = dayNumber in daysWithWorkouts

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) White else Color.Transparent)
                                        .clickable {
                                            onSelectDay(kotlinx.datetime.LocalDate(displayedYear, displayedMonth, dayNumber))
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            color = if (isSelected) Color.Black else White,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                        if (hasWorkout) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Emerald600 else Emerald400)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarWorkoutCard(
    detail: WorkoutHistoryDetail,
    availableExercises: List<org.marcosnpereira03.gymtracker.domain.model.Exercise>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar entrenamiento", color = White) },
            text = { Text("¿Deseas eliminar '${detail.workout.title}' del historial?", color = Zinc400) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Eliminar", color = Red500, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = Zinc400)
                }
            },
            containerColor = Zinc900
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: SESIÓN GUARDADA + Título + Botones Editar / Borrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "SESIÓN GUARDADA",
                        color = Emerald400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = detail.workout.title,
                        color = White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Editar estilo Pill con hover azul
                    val editInteractionSource = remember { MutableInteractionSource() }
                    val isEditHovered by editInteractionSource.collectIsHoveredAsState()
                    val isEditPressed by editInteractionSource.collectIsPressedAsState()
                    val isEditActive = isEditHovered || isEditPressed

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isEditActive) Color(0xFF1E3A8A).copy(alpha = 0.4f) else Zinc800)
                            .border(1.dp, if (isEditActive) Color(0xFF60A5FA) else Zinc700, RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = editInteractionSource,
                                indication = null,
                                onClick = onEdit
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = if (isEditActive) Color(0xFF60A5FA) else Zinc400,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Editar",
                            color = if (isEditActive) Color(0xFF60A5FA) else White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Botón Eliminar con hover rojo
                    HoverableActionIcon(
                        icon = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        defaultTint = Zinc400,
                        hoverTint = Red500,
                        onClick = { showDeleteConfirm = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detalle de ejercicios y series (estilo idéntico a captura: CURL FEMORAL TUMBADO BÍCEPS)
            val exerciseMap = availableExercises.associateBy { it.id }
            detail.workout.sets.groupBy { it.exerciseId }.forEach { (exerciseId, sets) ->
                val exObj = exerciseMap[exerciseId]
                val exName = exObj?.name ?: "EJERCICIO"
                val exMuscle = exObj?.muscleGroup ?: "MÚSCULO"

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Zinc950)
                        .border(1.dp, Zinc800, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exName.uppercase(),
                                color = White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = exMuscle.uppercase(),
                                color = Zinc400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        sets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Zinc900)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Serie #${set.setNumber}",
                                    color = Zinc400,
                                    fontSize = 12.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${set.weightKg.toInt()} kg", color = Emerald400, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("  ×  ${set.reps}", color = White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Zinc800)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "RIR ${set.rir}",
                                            color = if (set.rir == 0) Red500 else Zinc400,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun HoverableActionIcon(
    icon: ImageVector,
    contentDescription: String,
    defaultTint: Color = Zinc400,
    hoverTint: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActive = isHovered || isPressed

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) hoverTint.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) hoverTint else defaultTint,
            modifier = Modifier.size(16.dp)
        )
    }
}


