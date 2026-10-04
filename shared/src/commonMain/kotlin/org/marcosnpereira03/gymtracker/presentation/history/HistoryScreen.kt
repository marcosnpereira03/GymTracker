package org.marcosnpereira03.gymtracker.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onEditWorkout: (String) -> Unit,
    onNewWorkout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0 = Por Calendario, 1 = Por Ejercicio
    var selectedDay by remember { mutableStateOf(2) } // Día seleccionado del mes

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Scaffold(
        containerColor = Zinc950
    ) { paddingValues ->
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

            // Dual Tab Toggle: [ 📅 Por Calendario ] vs [ 🏋️ Por Ejercicio ]
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
                            Text("📅", fontSize = 14.sp)
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
                            Text("🏋️", fontSize = 14.sp)
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
                // Widget de Calendario Mensual
                item {
                    CalendarCard(
                        selectedDay = selectedDay,
                        onSelectDay = { selectedDay = it }
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
                            text = "SESIÓN DEL 2026-10-${if (selectedDay < 10) "0$selectedDay" else selectedDay}",
                            color = Zinc400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (state.filteredWorkouts.isNotEmpty()) "1 entrenamiento" else "0 entrenamientos",
                            color = Emerald400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Lista de entrenamientos del día
                if (state.isLoading && state.workouts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Emerald400)
                        }
                    }
                } else if (state.filteredWorkouts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Zinc900)
                                .border(1.dp, Zinc800, RoundedCornerShape(14.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay sesiones registradas en esta fecha.",
                                color = Zinc500,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(items = state.filteredWorkouts, key = { it.workout.id }) { item ->
                        CalendarWorkoutCard(
                            detail = item,
                            onEdit = { onEditWorkout(item.workout.id) },
                            onDelete = { viewModel.onDeleteWorkout(item.workout.id) }
                        )
                    }
                }
            } else {
                // Tab "Por Ejercicio": Buscador y lista
                item {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Buscar por ejercicio...", color = Zinc500, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = Zinc500)
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
                        singleLine = true
                    )
                }

                items(items = state.filteredWorkouts, key = { it.workout.id }) { item ->
                    CalendarWorkoutCard(
                        detail = item,
                        onEdit = { onEditWorkout(item.workout.id) },
                        onDelete = { viewModel.onDeleteWorkout(item.workout.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarCard(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del calendario (Mes + flechas)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Octubre De 2026",
                    color = White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Anterior",
                        tint = Zinc400,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { /* Mes anterior */ }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Siguiente",
                        tint = Zinc400,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { /* Mes siguiente */ }
                    )
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

            // Grid de días del mes (Octubre 2026 empieza en Jueves = index 3)
            val totalDays = 31
            val startOffset = 3 // 0=LU, 1=MA, 2=MI, 3=JU
            val rows = (totalDays + startOffset + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (row in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (col in 0 until 7) {
                            val dayNumber = (row * 7 + col) - startOffset + 1
                            if (dayNumber in 1..totalDays) {
                                val isSelected = dayNumber == selectedDay
                                val hasWorkout = dayNumber in listOf(1, 2) // Días con entreno registrado

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) White else Color.Transparent)
                                        .clickable { onSelectDay(dayNumber) },
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
                                                    .background(Emerald400)
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
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar entrenamiento", color = White) },
            text = { Text("¿Deseas eliminar '${detail.workout.title}'?", color = Zinc400) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Eliminar", color = Red500)
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
                Column(modifier = Modifier.weight(1f)) {
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
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Botón Editar estilo Pill
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Zinc800)
                            .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                            .clickable { onEdit() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Zinc400, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", color = White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    // Botón Eliminar
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Zinc800)
                            .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                            .clickable { showDeleteConfirm = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Zinc400, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Detalle de ejercicios y series (estilo idéntico a captura 3: CURL FEMORAL TUMBADO BÍCEPS)
            detail.workout.sets.groupBy { it.exerciseId }.forEach { (exerciseId, sets) ->
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
                                text = "EJERCICIO REGISTRADO",
                                color = White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "DETALLE",
                                color = Zinc400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
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
                                            color = Zinc400,
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

