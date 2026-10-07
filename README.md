# GymTracker (IronLog) — Plataforma Multiplataforma de Registro y Análisis de Fuerza

---

## 1. Resumen Ejecutivo y Enfoque de Negocio

**GymTracker** es una aplicación móvil multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** (Android & iOS), diseñada para el registro preciso, análisis biométrico y seguimiento de la sobrecarga progresiva en entrenamientos de fuerza e hipertrofia.

### El Enfoque de Negocio (Superando el anotador tradicional)
El registro de entrenamientos de fuerza suele realizarse en aplicaciones genéricas que funcionan como meros bloques de notas digitales o planillas estáticas. Esto genera importantes fricciones:
*   Falta de estimación del esfuerzo real y proximidad al fallo muscular (**RIR - Reps In Reserve**).
*   Desconexión entre el tonelaje de volumen levantado y la distribución por grupo muscular.
*   Dificultad para visualizar la tendencia del peso corporal y correlacionarla con el rendimiento físico.
*   Pérdida de datos por desconexión a internet o falta de sincronización multi-dispositivo.

**GymTracker** resuelve esta problemática estructurando una experiencia deportiva técnica y moderna:
1. **Registro granular de series y esfuerzo**: Registro de peso (kg), repeticiones e intensidad subjetiva mediante RIR ($0 \le \text{RIR} \le 10$).
2. **Cálculo automatizado de 1RM Estimado**: Implementación de la fórmula de Epley ponderada por el esfuerzo en reserva, permitiendo proyectar la fuerza máxima sin someter al atleta a repeticiones máximas lesivas.
3. **Analítica de volumen y composición corporal**: Gráficos interactivos en tiempo real con filtrado dinámico por períodos (Diario, Semanal, Mensual, Histórico) para volumen muscular acumulado y evolución del peso corporal.
4. **Resiliencia total (Offline-First & Cero Crashes)**: Arquitectura resiliente con sincronización transparente a la base de datos PostgreSQL en Supabase protegida por Row-Level Security (RLS).

---

## 2. Arquitectura de Software y Patrones de Diseño

El proyecto implementa **Clean Architecture + MVVM** con **Unidirectional Data Flow (UDF)**, estructurado para maximizar la reutilización de código (100% de la lógica de negocio y de UI en `commonMain`). La lógica se divide en capas bien delimitadas:
*   **Dominio (`domain`)**: Puro Kotlin sin dependencias externas, con modelos inmutables (`Workout`, `Exercise`, `BodyWeightLog`), interfaces de repositorio y casos de uso de lógica de negocio (1RM y volumen).
*   **Datos (`data`)**: Implementación de repositorios consumiendo Supabase Postgrest con DTOs serializables, mappers bidireccionales y fallback en memoria para funcionamiento offline.
*   **Presentación (`presentation`)**: UI declarativa con Compose Multiplatform, tema Material 3 Dark Theme deportivo y ViewModels que exponen estado reactivo e inmutable mediante `StateFlow`.
*   **Inyección de Dependencias (`di`)**: Módulos centralizados con Koin para gestionar el ciclo de vida de los componentes.

### Principios Arquitectónicos Clave:
*   **Separación de Responsabilidades (SoC)**: La capa de `domain` no tiene conocimiento alguno de Supabase, Compose o Android. Toda regla matemática y de negocio es testeable de forma aislada.
*   **Unidirectional Data Flow (UDF)**: Cada ViewModel expone un único `StateFlow<ScreenUiState>` inmutable. La UI emite eventos hacia el ViewModel y este actualiza el estado de forma reactiva y atómica.
*   **Aislamiento Multiplataforma**: Prohibición de APIs exclusivas de JVM/Android en `commonMain` (ej. `java.time.*` o `java.util.UUID`). Se utilizan `kotlinx.datetime.*` y la utilidad pure-KMP `UuidUtil`.

---

## 3. Bitácora de Copiloto: Orquestación y Co-creación con IA

El desarrollo de GymTracker se orquestó adoptando un rol de liderazgo técnico y arquitectura, utilizando a **Antigravity (Google DeepMind)** como copiloto de desarrollo autónomo.

### La IA como Multiplicador de Velocidad
La colaboración con Antigravity permitió acelerar drásticamente los ciclos de iteración:
*   **Generación de Boilerplate & DTOs**: Creación y mapeo de clases serializables en Kotlin con `@SerialName` y extension functions bidireccionales (`toDomain()` y `toDto()`).
*   **Maquetación en Compose Multiplatform**: Construcción de componentes declarativos avanzados (gráficos en Canvas con curvas Bézier, listas animadas `LazyColumn`, modales de historial con badges de récords).
*   **Casos de Uso Matemáticos**: Implementación instantánea de fórmulas de biomecánica deportiva y cálculo de tonelaje con cobertura de pruebas unitarias.

### Desafíos Complejos y Depuración Asistida

1. **Alineación con el Esquema de Base de Datos y Row-Level Security (RLS)**:
   - *Problema*: La aplicación requería sincronizar datos con el esquema existente de la plataforma Web (`ejercicios`, `entrenamientos`, `series_realizadas`, `pesajes`). Postgrest rechazaba las peticiones si no se adjuntaba el `user_id` del usuario autenticado bajo las políticas de RLS (`auth.uid() = user_id`).
   - *Solución*: Se unificaron los DTOs al español y se inyectó dinámicamente el `currentUserOrNull()?.id` en todos los repositorios antes de ejecutar los `upsert`/`insert`, garantizando persistencia y seguridad multi-usuario sin colapsar el cliente.

2. **Generación de Identificadores Únicos en Pure Kotlin (KMP)**:
   - *Problema*: `java.util.UUID.randomUUID()` no es multiplataforma y rompía la compilación en targets iOS.
   - *Solución*: Se diseñó `UuidUtil.kt` en `commonMain`, un generador de UUID v4 criptográficamente pseudoaleatorio compatible con RFC 4122, con sanitización (`ensureUuid`) y validación por expresiones regulares.

3. **Ciclo de Vida y Reseteo de Estados en ViewModels Singleton**:
   - *Problema*: Al guardar un entrenamiento, el ViewModel mantenía las series en memoria para preservar borradores entre pestañas, pero esto provocaba que al guardar la sesión quedara "pegada" en la pantalla de entrenamiento.
   - *Solución*: Se rediseñó el flujo de guardado en `WorkoutSessionViewModel.kt` para que, tras persistir exitosamente en Supabase, ejecute un reseteo atómico del estado (`sets = emptyList()`, nuevo UUID y fecha actual), permitiendo transicionar limpiamente a una nueva sesión.

---

## 4. Stack Tecnológico y Justificación Técnica

| Tecnología | Rol en el Proyecto | Justificación Arquitectónica |
| :--- | :--- | :--- |
| **Kotlin Multiplatform (KMP 2.x)** | Core Multiplataforma | Permite compartir el 100% de la lógica de negocio, red, persistencia y modelos entre Android e iOS sin duplicar código. |
| **Compose Multiplatform** | Framework de UI Declarativa | Renderizado nativo y reactivo de interfaces en Android e iOS compartiendo componentes, animaciones y temas Material 3. |
| **Supabase Postgrest (`supabase-kt`)** | Cliente REST / Backend as a Service | Interacción reactiva con PostgreSQL mediante consultas fuertemente tipadas y serialización con `kotlinx.serialization`. |
| **Supabase Auth** | Autenticación y Sesiones | Gestión de usuarios con JWT, inicio de sesión por correo/contraseña y control de acceso seguro. |
| **Supabase RLS (Row Level Security)** | Seguridad de Base de Datos | Políticas de acceso a nivel de fila que garantizan que cada atleta solo acceda y modifique sus propios entrenamientos y pesajes. |
| **Koin (Core & Compose)** | Inyección de Dependencias | Framework ligero de DI nativo para Kotlin Multiplatform con soporte de ViewModels integrados en el ciclo de Compose. |
| **Kotlinx Coroutines & Flow** | Concurrencia y Reactividad | Manejo asíncrono no bloqueante de llamadas de red y exposición reactiva de estados con `StateFlow`. |
| **Kotlinx DateTime** | Manipulación Temporal Multiplataforma | Gestión de fechas y tiempos en formato ISO-8601 y `YYYY-MM-DD` compatible con los tipos `DATE` y `TIMESTAMPTZ` de PostgreSQL. |
| **Jetpack / Compose Canvas** | Renderizado Gráfico Personalizado | Dibujado de curvas de Bézier cúbicas, gradientes translúcidos y cuadrículas para análisis de volumen y peso corporal. |

---

## 5. Esquema de Base de Datos (PostgreSQL en Supabase)

El modelo de datos relacional garantiza integridad referencial con borrado en cascada y seguridad multi-usuario mediante **Row Level Security (RLS)** vinculada a `auth.users(id)`:
*   **`ejercicios`**: Catálogo maestro de ejercicios indexados por usuario y grupo muscular.
*   **`entrenamientos`**: Cabecera de sesiones de entrenamiento con fecha, nombre de sesión y observaciones.
*   **`series_realizadas`**: Registro granular de series con peso en kg, repeticiones efectivas y RIR ($0 \le \text{RIR} \le 10$), vinculadas al entrenamiento y ejercicio correspondiente.
*   **`pesajes`**: Registro histórico de peso corporal para correlación de rendimiento y composición física.

---

## 6. Lógica de Negocio y Fórmulas Matemáticas

### 1. Cálculo de 1RM Estimado (`CalculateOneRepMaxUseCase`)
Utiliza la fórmula de **Epley** ajustada por las repeticiones en reserva (**RIR**), calculando las repeticiones efectivas al fallo ($r_{\text{eff}} = \text{reps} + \text{rir}$):

$$\text{1RM} = \text{peso\_kg} \times \left(1 + \frac{\text{reps} + \text{rir}}{30.0}\right)$$

*   *Caso base*: Si $\text{reps} = 1$ y $\text{rir} = 0$, el 1RM es exactamente $\text{peso\_kg}$.
*   *Límites*: Si $\text{peso\_kg} \le 0$ o $\text{reps} \le 0$, retorna $0.0$.

### 2. Cálculo de Tonelaje Total (`CalculateWorkoutVolumeUseCase`)
Calcula el volumen de carga acumulado de todas las series completadas de una sesión:

$$\text{Volumen Total (kg)} = \sum_{i=1}^{n} (\text{peso\_kg}_i \times \text{reps}_i)$$

---

## 7. QA y Pruebas Unitarias

La lógica del dominio está respaldada por una suite de pruebas unitarias en `shared/src/commonTest/kotlin`:

*   **`CalculateOneRepMaxUseCaseTest`**:
    - Validación del cálculo con repeticiones estándar y RIR variable.
    - Validación de 1RM puro (1 repetición al fallo RIR 0).
    - Resiliencia ante valores de borde (peso cero, repeticiones cero, valores negativos).
*   **`CalculateWorkoutVolumeUseCaseTest`**:
    - Cálculo de sumatoria de tonelaje para múltiples series.
    - Manejo seguro de sesiones vacías o series con peso/repeticiones nulas.
*   **`CalculateMuscleGroupVolumeUseCaseTest`**:
    - Agrupamiento y cálculo discriminado de volumen por grupos musculares (Pecho, Espalda, Piernas, etc.).

Para ejecutar las pruebas unitarias:
```bash
./gradlew :shared:testDebugUnitTest
```

---

## 8. Guía de Instalación y Ejecución Local

### Requisitos Previos
*   **Java Development Kit (JDK)**: Versión 17 o superior.
*   **Android Studio**: Ladybug / Meerkat o superior con Android SDK instalado.
*   **Xcode**: Versión 15+ (requerido únicamente para compilar y ejecutar en el simulador de iOS / macOS).
*   Una cuenta activa de **Supabase** con el esquema SQL inicial ejecutado.

---

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/marcosnpereira03/GymTracker.git
cd GymTracker
```

---

### Paso 2: Configuración de Conexión a Supabase
Las credenciales de conexión se encuentran centralizadas en [SupabaseClientFactory.kt](file:///c:/Users/ryzen/Desktop/GymTracker/shared/src/commonMain/kotlin/org/marcosnpereira03/gymtracker/data/remote/SupabaseClientFactory.kt):

```kotlin
object SupabaseConfig {
    const val DEFAULT_URL = "https://tu-proyecto.supabase.co"
    const val DEFAULT_ANON_KEY = "tu-anon-key-de-supabase"
}
```

---

### Paso 3: Inicializar la Base de Datos en Supabase
1. Ingresa a tu panel de **Supabase Dashboard** -> **SQL Editor**.
2. Abre el archivo [01_initial_schema.sql](file:///c:/Users/ryzen/Desktop/GymTracker/supabase/migrations/01_initial_schema.sql).
3. Pega y ejecuta el script para crear las tablas (`ejercicios`, `entrenamientos`, `series_realizadas`, `pesajes`), los índices de rendimiento y las políticas de Row-Level Security (RLS).

---

### Paso 4: Compilar y Ejecutar la Aplicación

#### En Android:
Desde la terminal o desde el botón *Run* de Android Studio:
```bash
# Compilar e instalar en emulador/dispositivo conectado
./gradlew :androidApp:installDebug
```

#### En iOS (requiere macOS):
Abre el directorio `iosApp` en Xcode o ejecuta:
```bash
open iosApp/iosApp.xcworkspace
```
Selecciona tu simulador de iPhone de preferencia y presiona **Cmd + R**.

#### Ejecutar Suite de Tests Unitarios:
```bash
./gradlew :shared:testDebugUnitTest
```