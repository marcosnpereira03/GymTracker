package org.marcosnpereira03.gymtracker.domain.usecase

import org.marcosnpereira03.gymtracker.domain.repository.AuthRepository
import org.marcosnpereira03.gymtracker.domain.repository.ExerciseRepository
import org.marcosnpereira03.gymtracker.domain.repository.ProfileRepository
import org.marcosnpereira03.gymtracker.domain.repository.WorkoutRepository

/**
 * Caso de uso encargado de consolidar los datos del usuario desde Supabase (catálogo de ejercicios,
 * historial de entrenamientos recientes, peso corporal y perfil) para suministrarlos como contexto
 * al Coach de IA de Google Gemini.
 */
class BuildAiUserDataContextUseCase(
    private val exerciseRepository: ExerciseRepository,
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): String {
        val user = authRepository.currentUser.value ?: authRepository.checkCurrentSession()
        val exercises = exerciseRepository.getExercises().getOrDefault(emptyList())
        val workouts = workoutRepository.getWorkouts().getOrDefault(emptyList())
        val weights = profileRepository.getBodyWeightLogs().getOrDefault(emptyList())

        val exerciseMap = exercises.associateBy { it.id }

        val sb = StringBuilder()
        sb.appendLine("Eres el Coach de Inteligencia Artificial de GymTracker / IronLog.")
        sb.appendLine("Eres un entrenador de fuerza y acondicionamiento físico de élite, experto en hipertrofia, periodización, progresión de sobrecarga progresiva, RIR/RPE y cálculo de 1RM.")
        sb.appendLine("Responde de manera concisa, motivadora, técnica y estructurada. Utiliza formato markdown claro con listas o viñetas cuando sea apropiado.")
        sb.appendLine()
        sb.appendLine("=== CONTEXTO DEL ATLETA ===")

        // Perfil
        val userName = user?.username?.takeIf { it.isNotBlank() } ?: user?.email?.substringBefore("@") ?: "Atleta"
        sb.appendLine("- Nombre del atleta: $userName")

        val latestWeight = weights.firstOrNull()
        if (latestWeight != null) {
            sb.appendLine("- Peso corporal actual: ${latestWeight.weightKg} kg (registrado el ${latestWeight.date})")
        } else {
            sb.appendLine("- Peso corporal actual: No registrado")
        }
        sb.appendLine()

        // Catálogo de Ejercicios
        sb.appendLine("=== CATÁLOGO DE EJERCICIOS DISPONIBLES ===")
        if (exercises.isEmpty()) {
            sb.appendLine("No hay ejercicios registrados en el catálogo.")
        } else {
            exercises.forEach { ex ->
                sb.appendLine("- ${ex.name} [Grupo: ${ex.muscleGroup}]")
            }
        }
        sb.appendLine()

        // Entrenamientos recientes (últimos 30 días / últimos entrenos)
        sb.appendLine("=== HISTORIAL DE ENTRENAMIENTOS RECIENTES (ÚLTIMOS 30 DÍAS) ===")
        if (workouts.isEmpty()) {
            sb.appendLine("El usuario aún no ha registrado entrenamientos.")
        } else {
            // Tomamos hasta los 15 entrenamientos más recientes
            workouts.take(15).forEach { workout ->
                val dateStr = workout.date.toString().substringBefore("T")
                sb.appendLine("• Sesión: ${workout.title} | Fecha: $dateStr")
                if (!workout.notes.isNullOrBlank()) {
                    sb.appendLine("  Notas: ${workout.notes}")
                }
                if (workout.sets.isEmpty()) {
                    sb.appendLine("  (Sin series registradas)")
                } else {
                    val groupedSets = workout.sets.groupBy { it.exerciseId }
                    groupedSets.forEach { (exerciseId, sets) ->
                        val exerciseName = exerciseMap[exerciseId]?.name ?: "Ejercicio (ID: $exerciseId)"
                        val setDetails = sets.joinToString(", ") { s ->
                            "Serie ${s.setNumber}: ${s.weightKg}kg x ${s.reps} reps (RIR ${s.rir})"
                        }
                        sb.appendLine("  - $exerciseName: $setDetails")
                    }
                }
            }
        }

        return sb.toString()
    }
}
