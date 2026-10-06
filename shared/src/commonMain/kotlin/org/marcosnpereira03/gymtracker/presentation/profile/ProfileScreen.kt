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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume
import org.marcosnpereira03.gymtracker.domain.model.PersonalRecord
import org.marcosnpereira03.gymtracker.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToWorkout: () -> Unit = {},
    onNavigateToAuth: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var showLogWeightDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    if (showLogWeightDialog) {
        LogWeightDialog(
            onDismiss = { showLogWeightDialog = false },
            onConfirm = { weight, notes ->
                viewModel.onAddWeightLog(weight, notes)
                showLogWeightDialog = false
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentUsername = state.currentUser?.username ?: "",
            currentAvatarUrl = state.currentUser?.avatarUrl,
            onDismiss = { showEditProfileDialog = false },
            onConfirm = { newUsername, newAvatarUrl ->
                viewModel.onUpdateProfile(newUsername, newAvatarUrl)
                showEditProfileDialog = false
            }
        )
    }

    Scaffold(
        containerColor = Zinc950,
        topBar = {
            TopAppBar(
                title = {
                    val handle = state.currentUser?.username?.takeIf { it.isNotBlank() }?.let { "@$it" }
                        ?: state.currentUser?.email?.substringBefore('@')?.let { "@$it" }
                        ?: "@atleta"
                    Text(
                        text = handle,
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (state.currentUser != null) {
                        IconButton(
                            onClick = { viewModel.onSignOut() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Zinc900)
                                .border(1.dp, Zinc800, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Cerrar Sesión",
                                tint = Zinc400,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onNavigateToAuth,
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald400,
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Ingresar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Zinc950
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.weightLogs.isEmpty() && state.totalWorkoutsCount == 0) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp)
            ) {
                // Notificación de éxito / error si existe
                if (state.successMessage != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Emerald400.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald400.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✅", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.successMessage ?: "",
                                    color = Emerald400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // 1. Tarjeta / Sección de Cabecera de Perfil
                item {
                    ProfileHeaderCard(
                        username = state.currentUser?.username?.takeIf { it.isNotBlank() }
                            ?: state.currentUser?.email?.substringBefore('@')
                            ?: "Atleta",
                        email = state.currentUser?.email,
                        totalWorkouts = state.totalWorkoutsCount,
                        currentWeightKg = state.latestWeight?.weightKg,
                        prsCount = state.personalRecords.size,
                        onEditProfileClick = { showEditProfileDialog = true },
                        onLogWeightClick = { showLogWeightDialog = true }
                    )
                }

                // 2. Selector de Pestañas (Estadísticas / PRs / Pesajes)
                item {
                    ProfileTabsSelector(
                        selectedTab = state.activeTab,
                        onTabSelected = { viewModel.onSelectTab(it) }
                    )
                }

                // 3. Contenido según la pestaña activa
                when (state.activeTab) {
                    ProfileTab.STATS -> {
                        // Selector de Período y resumen de volumen
                        item {
                            VolumePeriodSelector(
                                selectedPeriod = state.selectedPeriod,
                                totalPeriodVolume = state.totalPeriodVolumeKg,
                                onPeriodSelected = { viewModel.onSelectPeriod(it) }
                            )
                        }

                        if (state.muscleGroupVolumes.isEmpty()) {
                            item {
                                EmptyStatsCard(onNavigateToWorkout = onNavigateToWorkout)
                            }
                        } else {
                            items(items = state.muscleGroupVolumes, key = { it.muscleGroup }) { volumeItem ->
                                val maxVolume = state.muscleGroupVolumes.maxOfOrNull { it.totalVolumeKg } ?: 1.0
                                val progress = (volumeItem.totalVolumeKg / maxVolume).coerceIn(0.0, 1.0).toFloat()
                                MuscleVolumeCard(volumeItem, progress)
                            }
                        }
                    }

                    ProfileTab.PRS -> {
                        if (state.personalRecords.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EmojiEvents,
                                            contentDescription = null,
                                            tint = Zinc600,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Aún no tienes Récords (PRs)",
                                            color = White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Completa tus sesiones de entrenamiento para calcular automáticamente tus marcas máximas.",
                                            color = Zinc400,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(items = state.personalRecords, key = { it.exerciseId }) { pr ->
                                PersonalRecordCard(pr = pr)
                            }
                        }
                    }

                    ProfileTab.WEIGHT_LOGS -> {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HISTORIAL DE PESAJES",
                                    color = Zinc400,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                TextButton(
                                    onClick = { showLogWeightDialog = true },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Emerald400)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Anotar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (state.weightLogs.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Zinc900),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(20.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "No hay registros de peso aún.",
                                            color = Zinc400,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            items(items = state.weightLogs, key = { it.id }) { log ->
                                ProfileWeightLogRow(
                                    log = log,
                                    onDelete = { viewModel.onDeleteWeightLog(log.id) }
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
fun ProfileHeaderCard(
    username: String,
    email: String?,
    totalWorkouts: Int,
    currentWeightKg: Double?,
    prsCount: Int,
    onEditProfileClick: () -> Unit,
    onLogWeightClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Fila superior: Avatar + Info Usuario
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circular estilizado
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Emerald600, Emerald950)
                            )
                        )
                        .border(2.dp, Emerald400.copy(alpha = 0.6f), CircleShape)
                        .clickable { onEditProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    val initial = username.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                    Text(
                        text = initial,
                        color = White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = username,
                        color = White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!email.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = email,
                            color = Zinc400,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Resumen de Métricas Rápidas (Entrenos / Peso / PRs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Zinc950)
                    .border(1.dp, Zinc800, RoundedCornerShape(14.dp))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entrenos
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalWorkouts",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Entrenos",
                        color = Zinc400,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Zinc800)
                )

                // Peso
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val weightStr = currentWeightKg?.let { "$it kg" } ?: "--.-"
                    Text(
                        text = weightStr,
                        color = Emerald400,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Peso Actual",
                        color = Zinc400,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(Zinc800)
                )

                // PRs
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$prsCount",
                        color = White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Récords",
                        color = Zinc400,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Botones de acción rápida: [ Peso ] y [ Editar Perfil ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Botón Peso
                Button(
                    onClick = onLogWeightClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Zinc800,
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Scale,
                        contentDescription = "Peso",
                        tint = Emerald400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Peso",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                // Botón Editar Perfil
                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Zinc800,
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Zinc300,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Editar Perfil",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTabsSelector(
    selectedTab: ProfileTab,
    onTabSelected: (ProfileTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Zinc900)
            .border(1.dp, Zinc800, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ProfileTab.values().forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Zinc800 else Color.Transparent)
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val icon = when (tab) {
                        ProfileTab.STATS -> Icons.Default.BarChart
                        ProfileTab.PRS -> Icons.Default.EmojiEvents
                        ProfileTab.WEIGHT_LOGS -> Icons.Default.MonitorWeight
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) Emerald400 else Zinc400,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = tab.title,
                        color = if (isSelected) Emerald400 else Zinc400,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun VolumePeriodSelector(
    selectedPeriod: VolumePeriod,
    totalPeriodVolume: Double,
    onPeriodSelected: (VolumePeriod) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VOLUMEN POR MÚSCULO",
                color = Zinc400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Total: ${totalPeriodVolume.toInt()} kg",
                color = Emerald400,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Zinc900)
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            VolumePeriod.values().forEach { period ->
                val isSelected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Zinc800 else Color.Transparent)
                        .clickable { onPeriodSelected(period) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        color = if (isSelected) Emerald400 else Zinc400,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStatsCard(onNavigateToWorkout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Zinc800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Zinc500,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Aún no hay ningún dato",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Aquí verás cómo progresas en el tiempo y el volumen levantado por músculo.",
                color = Zinc400,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onNavigateToWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = Color.Black
                )
            ) {
                Text("Registrar entrenamiento", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun MuscleVolumeCard(item: MuscleGroupVolume, progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.muscleGroup,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.totalVolumeKg.toInt()} kg",
                    color = Emerald400,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Emerald400,
                trackColor = Zinc800
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${item.totalSets} series • ${item.totalReps} repeticiones",
                color = Zinc500,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun PersonalRecordCard(pr: PersonalRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de Ejercicio / Trofeo circular
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

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pr.exerciseName,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏆 ${pr.weightKg} kg × ${pr.reps}",
                        color = Emerald400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  1RM est.: ${pr.estimated1RM} kg",
                        color = Zinc400,
                        fontSize = 11.sp
                    )
                }
                val dateStr = pr.date.toString().substringBefore('T')
                Text(
                    text = "$dateStr  •  ${pr.muscleGroup}",
                    color = Zinc500,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ProfileWeightLogRow(log: BodyWeightLog, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Zinc900)
            .border(1.dp, Zinc800, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${log.weightKg} kg",
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            val dateStr = log.date.toString().substringBefore("T")
            val notesStr = if (!log.notes.isNullOrBlank()) " • ${log.notes}" else ""
            Text(
                text = "$dateStr$notesStr",
                color = Zinc400,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar pesaje",
                tint = Red500.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUsername: String,
    currentAvatarUrl: String?,
    onDismiss: () -> Unit,
    onConfirm: (username: String, avatarUrl: String?) -> Unit
) {
    var usernameText by remember { mutableStateOf(currentUsername) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Perfil",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Personaliza tu nombre de usuario para mostrar en el perfil y en la pantalla de inicio.",
                    color = Zinc400,
                    fontSize = 12.sp
                )

                OutlinedTextField(
                    value = usernameText,
                    onValueChange = { usernameText = it },
                    label = { Text("Nombre de usuario (ej. marcosn03)", color = Zinc400) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Zinc700,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(usernameText, currentAvatarUrl) },
                enabled = usernameText.trim().isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald400,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Zinc400)
            }
        },
        containerColor = Zinc900,
        shape = RoundedCornerShape(16.dp)
    )
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
        title = {
            Text(
                text = "Anotar Peso Corporal",
                color = White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Peso en kg (ej. 78.5)", color = Zinc400) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Zinc700,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    )
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notas (opcional, ej. en ayunas)", color = Zinc400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        focusedBorderColor = Emerald400,
                        unfocusedBorderColor = Zinc700,
                        focusedContainerColor = Zinc900,
                        unfocusedContainerColor = Zinc900
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    onConfirm(w, notesText)
                },
                enabled = weightText.replace(',', '.').toDoubleOrNull() != null && (weightText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald400,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Zinc400)
            }
        },
        containerColor = Zinc900,
        shape = RoundedCornerShape(16.dp)
    )
}
