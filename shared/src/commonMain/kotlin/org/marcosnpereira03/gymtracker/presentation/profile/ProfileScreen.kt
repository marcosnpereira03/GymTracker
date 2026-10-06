package org.marcosnpereira03.gymtracker.presentation.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.marcosnpereira03.gymtracker.domain.model.BodyWeightLog
import org.marcosnpereira03.gymtracker.domain.model.MuscleGroupVolume
import org.marcosnpereira03.gymtracker.domain.model.PersonalRecord
import org.marcosnpereira03.gymtracker.domain.util.DateTimeUtil
import org.marcosnpereira03.gymtracker.presentation.theme.*
import kotlin.math.abs

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
                        // Navegador interactivo de período con selector desplegable y botones < >
                        item {
                            PeriodNavigatorCard(
                                title = "VOLUMEN POR MÚSCULO",
                                extraInfo = "Total: ${state.totalPeriodVolumeKg.toInt()} kg",
                                selectedPeriod = state.selectedPeriod,
                                periodRangeLabel = state.periodRangeLabel,
                                canNavigateForward = state.canNavigateForward,
                                onPeriodSelected = { viewModel.onSelectPeriod(it) },
                                onPreviousPeriod = { viewModel.onNavigatePreviousPeriod() },
                                onNextPeriod = { viewModel.onNavigateNextPeriod() }
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
                        // Navegador interactivo de período para la sección de peso
                        item {
                            PeriodNavigatorCard(
                                title = "EVOLUCIÓN DEL PESO",
                                extraInfo = state.latestWeight?.let { "Actual: ${it.weightKg} kg" } ?: "--.- kg",
                                selectedPeriod = state.weightPeriod,
                                periodRangeLabel = state.weightPeriodRangeLabel,
                                canNavigateForward = state.canNavigateWeightForward,
                                onPeriodSelected = { viewModel.onSelectWeightPeriod(it) },
                                onPreviousPeriod = { viewModel.onNavigatePreviousWeightPeriod() },
                                onNextPeriod = { viewModel.onNavigateNextWeightPeriod() }
                            )
                        }

                        // 1. Gráfico visual de evolución del peso estilo app fitness
                        item {
                            WeightEvolutionChartCard(
                                weightLogs = state.filteredWeightLogs,
                                latestWeight = state.latestWeight,
                                totalWeightLostKg = state.totalWeightLostKg
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HISTORIAL DEL PERÍODO",
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
                                    Text("Anotar Peso", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (state.filteredWeightLogs.isEmpty()) {
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
                                            text = "No has registrado pesajes en este período.",
                                            color = Zinc400,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(items = state.filteredWeightLogs, key = { _, log -> log.id }) { index, log ->
                                val nextOlderLog = state.filteredWeightLogs.getOrNull(index + 1)
                                val diffKg = nextOlderLog?.let { log.weightKg - it.weightKg }
                                ProfileWeightLogRow(
                                    log = log,
                                    diffKg = diffKg,
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
fun PeriodNavigatorCard(
    title: String,
    extraInfo: String,
    selectedPeriod: VolumePeriod,
    periodRangeLabel: String,
    canNavigateForward: Boolean,
    onPeriodSelected: (VolumePeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Fila superior: Selector de Período (Izquierda Dropdown, Derecha Info adicional)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dropdown interactivo
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Zinc800)
                            .clickable { expandedDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Período: ${selectedPeriod.label}",
                            color = Emerald400,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Seleccionar período",
                            tint = Emerald400,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(Zinc900)
                    ) {
                        VolumePeriod.values().forEach { period ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = period.label,
                                            color = if (selectedPeriod == period) Emerald400 else White,
                                            fontSize = 13.sp,
                                            fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (selectedPeriod == period) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Emerald400,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onPeriodSelected(period)
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Info de cabecera derecha
                Text(
                    text = extraInfo,
                    color = White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila de navegación temporal con [ < ] Fecha Inicio - Fin [ > ]
            if (selectedPeriod != VolumePeriod.ALL_TIME) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Zinc950)
                        .border(1.dp, Zinc800, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPreviousPeriod,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Período anterior",
                            tint = Emerald400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = periodRangeLabel,
                        color = Zinc300,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = onNextPeriod,
                        enabled = canNavigateForward,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Período siguiente",
                            tint = if (canNavigateForward) Emerald400 else Zinc700,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Mostrando todo el historial acumulado",
                    color = Zinc400,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
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

/**
 * Tarjeta interactiva con gráfico Canvas para la evolución del peso según el período
 */
@Composable
fun WeightEvolutionChartCard(
    weightLogs: List<BodyWeightLog>,
    latestWeight: BodyWeightLog?,
    totalWeightLostKg: Double?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Zinc900),
        border = androidx.compose.foundation.BorderStroke(1.dp, Zinc800)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Encabezado de Peso Actual y Variación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PESO ACTUAL",
                        color = Zinc400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val weightText = latestWeight?.let { "${it.weightKg} kg" } ?: "--.- kg"
                    Text(
                        text = weightText,
                        color = White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (totalWeightLostKg != null) {
                    val isLoss = totalWeightLostKg > 0
                    val sign = if (isLoss) "-" else "+"
                    val label = if (isLoss) "Cambio: $sign${abs(totalWeightLostKg)} kg" else "Cambio: +${abs(totalWeightLostKg)} kg"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLoss) Emerald400.copy(alpha = 0.15f) else Color(0xFFF97316).copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isLoss) Emerald400.copy(alpha = 0.4f) else Color(0xFFF97316).copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (isLoss) Emerald400 else Color(0xFFF97316),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gráfico Canvas con Curva y Gradiente
            val chronologicalLogs = remember(weightLogs) { weightLogs.sortedBy { it.date } }

            if (chronologicalLogs.size >= 2) {
                WeightLineChart(
                    logs = chronologicalLogs,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Zinc950),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (chronologicalLogs.isEmpty()) "Sin registros de peso en este período." else "Anota al menos 2 pesajes para ver tu curva de evolución.",
                        color = Zinc500,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Canvas nativo de Compose que dibuja la línea de progreso de peso,
 * puntos de control brillantes, líneas guía y etiquetas de fechas.
 */
@Composable
fun WeightLineChart(
    logs: List<BodyWeightLog>,
    modifier: Modifier = Modifier
) {
    val emeraldColor = Emerald400
    val gridLineColor = Zinc800
    val textMutedColor = Zinc500

    val weights = remember(logs) { logs.map { it.weightKg } }
    val minWeight = remember(weights) { (weights.minOrNull() ?: 50.0) - 0.5 }
    val maxWeight = remember(weights) { (weights.maxOrNull() ?: 100.0) + 0.5 }
    val weightRange = if (maxWeight - minWeight > 0) maxWeight - minWeight else 1.0

    Column(modifier = modifier) {
        Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val width = size.width
            val height = size.height
            val paddingH = 16f
            val paddingV = 16f
            val chartWidth = width - (paddingH * 2)
            val chartHeight = height - (paddingV * 2)

            // Líneas guía horizontales punteadas
            val lineCount = 3
            for (i in 0..lineCount) {
                val y = paddingV + (chartHeight / lineCount) * i
                drawLine(
                    color = gridLineColor,
                    start = Offset(paddingH, y),
                    end = Offset(width - paddingH, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            }

            // Calcular coordenadas de los puntos
            val points = logs.mapIndexed { index, log ->
                val x = paddingH + (chartWidth / (logs.size - 1).coerceAtLeast(1)) * index
                val normalizedY = ((log.weightKg - minWeight) / weightRange).toFloat()
                val y = height - paddingV - (normalizedY * chartHeight)
                Offset(x, y)
            }

            if (points.isNotEmpty()) {
                // 1. Path de degradado de fondo
                val fillPath = Path().apply {
                    moveTo(points.first().x, height - paddingV)
                    lineTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val cx = (p0.x + p1.x) / 2
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                    lineTo(points.last().x, height - paddingV)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            emeraldColor.copy(alpha = 0.35f),
                            emeraldColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )

                // 2. Path de la línea continua
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val cx = (p0.x + p1.x) / 2
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = emeraldColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // 3. Puntos de datos y halo brillante
                points.forEachIndexed { idx, pt ->
                    val isLatest = idx == points.size - 1
                    if (isLatest) {
                        drawCircle(
                            color = emeraldColor.copy(alpha = 0.3f),
                            radius = 9.dp.toPx(),
                            center = pt
                        )
                    }
                    drawCircle(
                        color = if (isLatest) emeraldColor else Color(0xFFFBBF24),
                        radius = 4.5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.Black,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Eje de fechas inferior
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateLabels = remember(logs) {
                if (logs.size <= 4) logs.map { DateTimeUtil.formatShortDate(it.date) }
                else listOf(
                    DateTimeUtil.formatShortDate(logs.first().date),
                    DateTimeUtil.formatShortDate(logs[logs.size / 2].date),
                    DateTimeUtil.formatShortDate(logs.last().date)
                )
            }
            dateLabels.forEach { label ->
                Text(
                    text = label,
                    color = textMutedColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ProfileWeightLogRow(
    log: BodyWeightLog,
    diffKg: Double?,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Zinc900)
            .border(1.dp, Zinc800, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            val dateStr = DateTimeUtil.formatFullShortDate(log.date)
            Text(
                text = dateStr,
                color = Zinc300,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!log.notes.isNullOrBlank()) {
                Text(
                    text = log.notes,
                    color = Zinc500,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${log.weightKg} kg",
                color = White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Indicador de variación (⬆️ / ⬇️ / ➖)
            if (diffKg != null) {
                val isLoss = diffKg < 0
                val isGain = diffKg > 0
                val arrowIcon = when {
                    isLoss -> Icons.Default.ArrowDownward
                    isGain -> Icons.Default.ArrowUpward
                    else -> Icons.Default.Remove
                }
                val iconTint = when {
                    isLoss -> Emerald400
                    isGain -> Color(0xFFF97316)
                    else -> Zinc500
                }

                Icon(
                    imageVector = arrowIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar pesaje",
                    tint = Red500.copy(alpha = 0.6f),
                    modifier = Modifier.size(15.dp)
                )
            }
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
