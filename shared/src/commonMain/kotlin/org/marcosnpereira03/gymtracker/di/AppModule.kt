package org.marcosnpereira03.gymtracker.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import org.marcosnpereira03.gymtracker.data.remote.SupabaseClientFactory
import org.marcosnpereira03.gymtracker.data.repository.ExerciseRepositoryImpl
import org.marcosnpereira03.gymtracker.data.repository.ProfileRepositoryImpl
import org.marcosnpereira03.gymtracker.data.repository.WorkoutRepositoryImpl
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateMuscleGroupVolumeUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateOneRepMaxUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.CalculateWorkoutVolumeUseCase
import org.marcosnpereira03.gymtracker.domain.usecase.GetExerciseHistoryUseCase
import org.marcosnpereira03.gymtracker.presentation.exercises.ExercisesViewModel
import org.marcosnpereira03.gymtracker.presentation.history.HistoryViewModel
import org.marcosnpereira03.gymtracker.presentation.home.HomeViewModel
import org.marcosnpereira03.gymtracker.presentation.profile.ProfileViewModel
import org.marcosnpereira03.gymtracker.presentation.workout.WorkoutSessionViewModel

/**
 * Módulo de red y clientes remotos.
 */
val networkModule = module {
    single { SupabaseClientFactory.create() }
}

/**
 * Módulo de repositorios (enlaza interfaces del dominio con implementaciones de datos).
 */
val repositoryModule = module {
    singleOf(::ExerciseRepositoryImpl) { bind<ExerciseRepository>() }
    singleOf(::WorkoutRepositoryImpl) { bind<WorkoutRepository>() }
    singleOf(::ProfileRepositoryImpl) { bind<ProfileRepository>() }
}

/**
 * Módulo de casos de uso de lógica de negocio pura.
 */
val useCaseModule = module {
    factoryOf(::CalculateOneRepMaxUseCase)
    factoryOf(::CalculateWorkoutVolumeUseCase)
    factoryOf(::CalculateMuscleGroupVolumeUseCase)
    factoryOf(::GetExerciseHistoryUseCase)
}

/**
 * Módulo de ViewModels para la capa de presentación.
 */
val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::WorkoutSessionViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::ExercisesViewModel)
    viewModelOf(::ProfileViewModel)
}

/**
 * Lista consolidada de módulos de Koin para la aplicación multiplataforma.
 */
fun appModules(): List<Module> = listOf(
    networkModule,
    repositoryModule,
    useCaseModule,
    viewModelModule
)

/**
 * Función de inicialización de Koin para KMP.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModules())
    }
