package org.marcosnpereira03.gymtracker.presentation.profile

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val state by viewModel.uiState.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    if (showLogDialog) {
        LogWeightDialog(
            onDismiss = { showLogDialog = false },
            onConfirm = { weight, notes ->
                viewModel.onAddWeightLog(weight, notes)
                showLogDialog = false
            }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pesajes y Analíticas",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.weightLogs.isEmpty()) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                // Card de Peso Corporal Actual
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Peso Corporal Actual", color = TextSecondary, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val currentWeight = state.latestWeight?.let { "${it.weightKg} kg" } ?: "--.- kg"
                                    Text(
                                        text = currentWeight,
                                        color = TextPrimary,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    val lastDate = state.latestWeight?.let { "Último registro: ${it.date.toString().substringBefore("T")}" } ?: "Sin registros"
                                    Text(lastDate, color = TextMuted, fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { showLogDialog = true },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldPrimary,
                                        contentColor = TextOnEmerald
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Anotar Peso", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Sección Volumen por Grupo Muscular
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VOLUMEN POR MÚSCULO",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Total: ${state.totalPeriodVolumeKg} kg",
                                color = EmeraldPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Selector de Período (Diario / Semanal / Mensual)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            VolumePeriod.values().forEach { period ->
                                val isSelected = state.selectedPeriod == period
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) DarkSurfaceCard else DarkSurface)
                                        .clickable { viewModel.onSelectPeriod(period) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = period.label,
                                        color = if (isSelected) EmeraldPrimary else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Lista de Volúmenes por Grupo Muscular
                if (state.muscleGroupVolumes.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sin volumen registrado en este período.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(items = state.muscleGroupVolumes, key = { it.muscleGroup }) { volumeItem ->
                        val maxVolume = state.muscleGroupVolumes.maxOfOrNull { it.totalVolumeKg } ?: 1.0
                        val progress = (volumeItem.totalVolumeKg / maxVolume).coerceIn(0.0, 1.0).toFloat()
                        MuscleVolumeRow(volumeItem, progress)
                    }
                }

                // Historial de Pesajes
                item {
                    Text(
                        text = "HISTORIAL DE PESAJES",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (state.weightLogs.isEmpty()) {
                    item {
                        Text("No has registrado pesajes aún.", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    items(items = state.weightLogs, key = { it.id }) { log ->
                        WeightLogRow(log, onDelete = { viewModel.onDeleteWeightLog(log.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleVolumeRow(item: MuscleGroupVolume, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.muscleGroup,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.totalVolumeKg} kg",
                    color = EmeraldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Barra de progreso relativa
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = EmeraldPrimary,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.totalSets} series • ${item.totalReps} repeticiones",
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun WeightLogRow(log: BodyWeightLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${log.weightKg} kg",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            val dateStr = log.date.toString().substringBefore("T")
            val notesStr = if (!log.notes.isNullOrBlank()) " • ${log.notes}" else ""
            Text(
                text = "$dateStr$notesStr",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar pesaje",
                tint = AccentRed.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun LogWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (weightKg: Double, notes: String?) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Anotar Peso Corporal", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Peso en kg (ej. 78.5)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notas (opcional, ej. en ayunas)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toDoubleOrNull() ?: 0.0
                    onConfirm(w, notesText)
                },
                enabled = weightText.toDoubleOrNull() != null && weightText.toDouble() > 0,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary, contentColor = TextOnEmerald)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
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
