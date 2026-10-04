package org.marcosnpereira03.gymtracker.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.compose.viewmodel.koinViewModel
import org.marcosnpereira03.gymtracker.presentation.exercises.ExercisesScreen
import org.marcosnpereira03.gymtracker.presentation.exercises.ExercisesViewModel
import org.marcosnpereira03.gymtracker.presentation.history.HistoryScreen
import org.marcosnpereira03.gymtracker.presentation.history.HistoryViewModel
import org.marcosnpereira03.gymtracker.presentation.home.HomeScreen
import org.marcosnpereira03.gymtracker.presentation.home.HomeViewModel
import org.marcosnpereira03.gymtracker.presentation.profile.ProfileScreen
import org.marcosnpereira03.gymtracker.presentation.profile.ProfileViewModel
import org.marcosnpereira03.gymtracker.presentation.theme.*
import org.marcosnpereira03.gymtracker.presentation.workout.WorkoutSessionScreen
import org.marcosnpereira03.gymtracker.presentation.workout.WorkoutSessionViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = Zinc950,
        bottomBar = {
            // Barra de navegación personalizada estilo Web App con contenedor pill activo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Zinc950)
                    .border(width = 1.dp, color = Zinc800)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route || 
                        (item.route.startsWith("workout") && currentRoute?.startsWith("workout") == true)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(Zinc900)
                                        .border(1.dp, Zinc700, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                } else {
                                    Modifier
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                }
                            )
                            .clickable {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Emerald400 else Zinc400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                color = if (isSelected) Emerald400 else Zinc400,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            // 1. Pantalla Hoy (Home)
            composable(Screen.Home.route) {
                val viewModel = koinViewModel<HomeViewModel>()
                HomeScreen(
                    viewModel = viewModel,
                    onStartWorkout = {
                        navController.navigate(Screen.Workout.createRoute())
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.History.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onWorkoutClick = { workoutId ->
                        navController.navigate(Screen.Workout.createRoute(workoutId))
                    }
                )
            }

            // 2. Pantalla Entrenar (WorkoutSession)
            composable(
                route = Screen.Workout.route,
                arguments = listOf(
                    navArgument("workoutId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getString("workoutId")
                val viewModel = koinViewModel<WorkoutSessionViewModel>()
                WorkoutSessionScreen(
                    viewModel = viewModel,
                    workoutId = workoutId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // 3. Pantalla Historial
            composable(Screen.History.route) {
                val viewModel = koinViewModel<HistoryViewModel>()
                HistoryScreen(
                    viewModel = viewModel,
                    onEditWorkout = { workoutId ->
                        navController.navigate(Screen.Workout.createRoute(workoutId))
                    },
                    onNewWorkout = {
                        navController.navigate(Screen.Workout.createRoute())
                    }
                )
            }

            // 4. Pantalla Ejercicios
            composable(Screen.Exercises.route) {
                val viewModel = koinViewModel<ExercisesViewModel>()
                ExercisesScreen(
                    viewModel = viewModel
                )
            }

            // 5. Pantalla Pesajes & Analíticas (Profile)
            composable(Screen.Profile.route) {
                val viewModel = koinViewModel<ProfileViewModel>()
                ProfileScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
