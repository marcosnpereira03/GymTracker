@file:Suppress("DEPRECATION")
package org.marcosnpereira03.gymtracker

import androidx.compose.runtime.Composable
import org.koin.compose.KoinApplication
import org.marcosnpereira03.gymtracker.di.appModules
import org.marcosnpereira03.gymtracker.presentation.navigation.AppNavigation
import org.marcosnpereira03.gymtracker.presentation.theme.GymTrackerTheme

/**
 * Punto de entrada principal de Compose Multiplatform para Android e iOS.
 * Inicializa el contexto de Koin con todos sus módulos y aplica el tema GymTrackerTheme.
 */
@Composable
fun App() {
    KoinApplication(application = {
        modules(appModules())
    }) {
        GymTrackerTheme {
            AppNavigation()
        }
    }
}