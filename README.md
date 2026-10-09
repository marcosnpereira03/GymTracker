# GymTracker — Plataforma Multiplataforma de Registro y Análisis de Fuerza

---

## 1. ¿De qué trata el proyecto y por qué se diferencia de las demás apps?

**GymTracker** es una aplicación móvil multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** (Android & iOS), diseñada para el registro de alta precisión, análisis biomecánico y seguimiento riguroso de la sobrecarga progresiva en atletas de fuerza e hipertrofia.

### ¿Por qué se diferencia de las apps tradicionales?

La mayoría de las aplicaciones comerciales operan como simples blocs de notas digitales o planillas estáticas sin rigor analítico:
* **Ignoran la intensidad real**: Asumen que todas las repeticiones tienen el mismo costo metabólico, ignorando la proximidad al fallo muscular.
* **Obligan a tests lesivos de 1RM**: Forzar a un atleta a levantar su peso máximo real a 1 repetición conlleva un alto riesgo de lesión articular y sobreentrenamiento.
* **Carecen de análisis de volumen por grupo muscular**: No correlacionan el tonelaje acumulado con la distribución muscular ni con la evolución del peso corporal.
* **Son frágiles sin conexión**: Fallan o bloquean la experiencia si se pierde la conexión en el gimnasio.

En contraste, **GymTracker** fundamenta su seguimiento en **modelos matemáticos y biomecánicos en tiempo real**:

---

### Fundamentos Científicos y Fórmulas Biomecánicas

#### 1. Cálculo de 1RM Estimado Ajustado por Esfuerzo en Reserva (`CalculateOneRepMaxUseCase`)
GymTracker proyecta la repetición máxima teórica utilizando la fórmula de **Epley** ponderada por el **RIR (Reps In Reserve)**, donde las repeticiones efectivas al fallo son $r_{\text{eff}} = \text{reps} + \text{rir}$:

```math
\text{1RM} = \text{peso} \times \left(1 + \frac{\text{reps} + \text{rir}}{30.0}\right)
```

* **Casos base y restricciones de seguridad**:
  * Si $\text{reps} = 1$ y $\text{rir} = 0$, el 1RM es exactamente el $\text{peso}$.
  * Si $\text{peso} \le 0$ o $\text{reps} \le 0$, retorna $0.0$.
  * Rango válido estricto: $0 \le \text{RIR} \le 10$ ($0$ = fallo concéntrico absoluto, $2$ = 2 repeticiones antes del fallo).

#### 2. Cálculo de Tonelaje Total y Sobrecarga Progresiva (`CalculateWorkoutVolumeUseCase`)
Cuantifica la carga externa total de una sesión sumando el tonelaje de todas las series completadas:

```math
\text{Volumen Total} = \sum_{i=1}^{n} (\text{peso}_i \times \text{reps}_i)
```

#### 3. Distribución del Volumen por Grupo Muscular (`CalculateMuscleGroupVolumeUseCase`)
Agrupa y pondera el tonelaje entre los 12 grupos musculares anatómicos (*Pecho, Espalda, Bíceps, Tríceps, Hombros, Antebrazos, Cuádriceps, Isquios, Glúteos, Gemelos, Aductores, Abductores*), permitiendo detectar desbalances de volumen y optimizar la periodización.

---

### Funcionalidades Principales
1. **Registro Granular de Entrenamientos**: Carga de series en tiempo real con peso (kg), repeticiones, RIR numérico y selección de equipamiento (*Barra, Mancuernas, Polea, Máquina, Peso libre*).
2. **Precarga Inteligente de Historial**: Al añadir un ejercicio a la sesión, precarga automáticamente las cargas de la última vez que fue realizado.
3. **Calendario Interactivo de Pesajes**: Registro del peso corporal en cualquier fecha histórica navegando mes a mes en un calendario interactivo.
4. **Analítica Visual con Compose Canvas**: Gráficos interactivos en tiempo real con curvas de Bézier cúbicas y filtros temporales (Día, Semana, Mes, Histórico).
5. **Coach de Inteligencia Artificial (Google Gemini)**: Entrenador interactivo conectado a la API de Gemini que analiza el historial del atleta para brindar feedback personalizado.
6. **Resiliencia Total y Modo Offline**: Persistencia en Supabase PostgreSQL con Row-Level Security (RLS) y almacenamiento local con tolerancia a fallos.

---

## 2. Comparativa Arquitectónica: ¿Qué arquitectura se eligió y por qué?

El proyecto implementa **Clean Architecture** estructurada en capas desacopladas, combinada con **MVVM (Model-View-ViewModel)** y **Unidirectional Data Flow (UDF)**. El **100%** de la lógica de negocio, datos y UI reside en `shared/src/commonMain/kotlin`.

```text
shared/src/commonMain/kotlin/org/marcosnpereira03/gymtracker/
├── domain/                          # PURO KOTLIN (Sin frameworks, UI ni dependencias de plataforma)
│   ├── model/                       # Modelos inmutables (Workout, Exercise, WorkoutSet, BodyWeightLog)
│   ├── repository/                  # Interfaces abstractas de repositorios
│   └── usecase/                     # Lógica de negocio (CalculateOneRepMax, CalculateWorkoutVolume, etc.)
├── data/                            # Implementación de persistencia y red
│   ├── remote/                      # Clientes HTTP (Supabase Postgrest, Gemini REST API, DTOs)
│   ├── mapper/                      # Mapeo bidireccional entre DTOs y entidades de dominio
│   └── repository/                  # Implementación concreta de repositorios con caché offline
├── di/                              # Inyección de dependencias modular con Koin
└── presentation/                    # UI Declarativa con Compose Multiplatform
    ├── theme/                       # Design System Material 3 (Dark Mode deportivo)
    ├── navigation/                  # Enrutamiento tipado (NavHost / Compose Navigation)
    ├── home/                        # Dashboard principal y resumen diario
    ├── workout/                     # Registro de sesión en vivo y edición de series
    ├── history/                     # Historial de entrenamientos y récords personales (PRs)
    ├── exercises/                   # Catálogo de ejercicios y gestión de equipamiento
    ├── profile/                     # Estadísticas, calendario de pesajes y perfil
    └── coach/                       # Chatbot interactivo con Google Gemini
```

---

### Comparativa con Otras Arquitecturas

| Criterio | MVC / Monolito en Activity | MVP / MVVM Tradicional (Sin Clean) | Redux / MVI Puro | Clean Architecture + MVVM + UDF (Elegida) |
| :--- | :--- | :--- | :--- | :--- |
| **Acoplamiento** | Muy Alto (Lógica atada al ciclo de vida de la vista) | Medio (ViewModels acoplados a APIs de red/BD) | Bajo | **Mínimo** (Dominio 100% aislado sin dependencias externas) |
| **Testabilidad** | Difícil (Requiere emuladores o mocks de UI) | Parcial (Requiere mockear capas de datos) | Alta | **Máxima** (Casos de uso testeables en milisegundos con JUnit puro) |
| **Reutilización Multiplataforma** | Nula (Específico de cada plataforma) | Limitada a la capa de datos | Alta | **Total** (100% de dominio, datos y Compose UI compartidos) |
| **Complejidad / Boilerplate** | Baja al inicio, inmanejable al escalar | Moderada | Muy Alta (Exceso de reducers, middlewares y actions) | **Equilibrada** (Modular, escalable y sin sobrecarga innecesaria) |
| **Previsibilidad del Estado** | Baja (Múltiples fuentes de verdad) | Media (Posibles condiciones de carrera) | Muy Alta | **Máxima** (`StateFlow<UiState>` inmutable con flujo unidireccional) |

### ¿Por qué elegimos Clean Architecture + MVVM + UDF?

1. **Aislamiento Total del Dominio**: La capa `domain` no contiene imports de Android, iOS, Supabase ni Compose. Las reglas biomecánicas son atemporales y no cambian si se migra de base de datos o de framework de UI.
2. **Flujo Unidireccional de Datos (UDF)**: Cada `ViewModel` expone un único `val uiState: StateFlow<ScreenUiState>`. La UI es una función pura del estado; los eventos del usuario fluyen hacia el ViewModel y el nuevo estado inmutable desciende hacia la UI, erradicando bugs de estado inconsistente.
3. **Mantenibilidad y Escalabilidad**: Agregar nuevas fuentes de datos o modificar servicios externos (ej. cambiar de Supabase a otra API) requiere editar únicamente `data/`, dejando `domain/` y `presentation/` intactos.

---

## 3. Pruebas Unitarias, Integración Continua (CI) y Despliegue Continuo (CD)

La confiabilidad técnica y la estabilidad total (cero crashes) son pilares fundamentales del proyecto.

### Pruebas Unitarias (Unit Tests)

#### ¿Por qué están y para qué sirven?
* **Validación de Algoritmos Biomecánicos**: Garantizan que fórmulas críticas como la proyección de 1RM y el tonelaje de volumen arrojen valores matemáticamente correctos ante cualquier combinación de datos y casos límite (peso cero, RIR máximo, repeticiones únicas).
* **Prevención de Regresiones**: Cada vez que se añade una funcionalidad o se refactoriza código, la suite de tests valida instantáneamente que el comportamiento preexistente no se haya roto.
* **Documentación Viva**: Los tests describen con precisión el comportamiento esperado de cada caso de uso y ViewModel.

#### Cobertura en `commonTest`:
* **Dominio**:
  * `CalculateOneRepMaxUseCaseTest`: Validación de la fórmula de Epley ponderada por RIR, límites de entrada y casos de 1 repetición.
  * `CalculateWorkoutVolumeUseCaseTest`: Suma precisa de tonelaje y filtrado de series válidas.
  * `CalculateMuscleGroupVolumeUseCaseTest`: Distribución y cálculo porcentual por grupo muscular.
  * `GetExerciseHistoryUseCaseTest`: Estimación del historial de 1RM ordenado cronológicamente.
* **Presentación (ViewModels)**:
  * `WorkoutSessionViewModelTest`: Flujo UDF, mutación de series, adición/eliminación, precarga automática de registros previos y guardado.
  * `ExercisesViewModelTest`: Búsqueda, filtrado por grupo muscular, creación de ejercicios con equipamiento y eliminación.
  * `DashboardViewModelTest`: Resumen del día y cálculo de volumen acumulado.

---

### Integración Continua (CI) y Despliegue Continuo (CD)

El proyecto cuenta con un pipeline automatizado de **GitHub Actions** configurado en [`.github/workflows/ci.yml`](.github/workflows/ci.yml) que se ejecuta en cada **Pull Request** hacia `main` y `develop`.

```text
Flujo del Pipeline de CI:
1. Checkout del Repositorio
2. Configuración de Entorno (JDK 17 + Gradle Wrapper)
3. Ejecución de Tests Unitarios de Dominio y Compartidos (:shared:testDebugUnitTest)
4. Publicación Automática de Reporte JUnit en los Checks de la PR
5. Verificación de Integridad KMP (compileCommonMainKotlinMetadata)
6. Análisis Estático de Código con Android Lint (:androidApp:lintDebug)
7. Verificación de Compilación de la Aplicación (:androidApp:assembleDebug)
8. Carga de Artefactos y Reportes de Cobertura
```

#### Ventajas del Pipeline de CI/CD:
1. **Puerta de Calidad Automática**: Ningún código puede integrarse a `develop` o `main` si rompe tests unitarios o introduce errores de compilación multiplataforma.
2. **Validación de Metadatos Multiplataforma**: La tarea `compileCommonMainKotlinMetadata` verifica que no se hayan introducido APIs exclusivas de JVM/Android en código compartido.
3. **Análisis Estático (Lint)**: Detecta advertencias de accesibilidad, rendimiento y buenas prácticas de Android automáticamente.
4. **Reportes Transparentes en GitHub**: Los resultados de las pruebas se publican directamente en la vista del Pull Request con detalle de tests ejecutados y tiempos de respuesta.

---

## 4. Stack Tecnológico y Componentes Clave

| Componente / Tecnología | Propósito | Beneficio Técnico |
| :--- | :--- | :--- |
| **Kotlin Multiplatform (KMP 2.x)** | Core Multiplataforma | 100% de lógica de negocio, datos y modelos compartidos entre Android e iOS. |
| **Compose Multiplatform** | UI Declarativa | Interfaz nativa compartida para Android e iOS con diseño responsivo y fluido. |
| **Supabase Postgrest (`supabase-kt`)** | Backend & Base de Datos | Consultas tipadas a PostgreSQL con seguridad a nivel de fila (RLS). |
| **Supabase Auth** | Autenticación | Control de sesiones seguras mediante correo y contraseña. |
| **Google Gemini REST API** | Inteligencia Artificial | Chatbot deportivo interactivo con conocimiento del contexto del atleta. |
| **Koin** | Inyección de Dependencias | Framework ligero de DI nativo para Kotlin Multiplatform. |
| **Kotlinx Coroutines & Flow** | Concurrencia Reactiva | Operaciones asíncronas no bloqueantes con flujos reactivos `StateFlow`. |
| **Kotlinx DateTime** | Fechas Multiplataforma | Manejo de tiempos ISO-8601 compatible con PostgreSQL `TIMESTAMPTZ`. |
| **Compose Canvas** | Visualización de Datos | Renderizado de gráficos vectoriales personalizados de volumen y peso. |

---

## 5. Herramientas de IA y Metodología de Desarrollo

El desarrollo de **GymTracker** se ejecutó bajo un enfoque de **Ingeniería de Software Acelerada por Inteligencia Artificial**, donde la IA no actúa como un generador desatendido, sino como un **copiloto de alta velocidad orquestado y auditado continuamente por el criterio del ingeniero**.


---

### 1. La Utilidad de la IA vs. La Programación Tradicional
En el desarrollo de software tradicional sin herramientas de IA, gran parte del tiempo de un desarrollador se consume en **tareas mecánicas y repetitivas**: redacción manual de *Data Transfer Objects* (DTOs), funciones de mapeo bidireccional, estructuración de archivos de configuración Gradle, boilerplate de inyección de dependencias y redacción línea por línea de componentes visuales declarativos.

La adopción de IA como asistente generativo transformó radicalmente este paradigma:
* **Velocidad de Entrega Exponencial:** Tareas que tradicionalmente demandaban semanas de desarrollo manual (como crear desde cero una app KMP completa para dos plataformas con persistencia en la nube, auth, canvas y tests) se alcanzaron en pocos días con calidad de producción.
* **Foco en el Valor y la Arquitectura:** Al delegar la escritura del código base repetitivo a la IA, el tiempo y energía mental del ingeniero se enfocan en lo que realmente importa: el diseño arquitectónico, la solidez del dominio, las fórmulas matemáticas y la experiencia de usuario.
* **Cobertura Rápida de Pruebas Unitarias:** La generación ágil de casos de prueba con múltiples combinaciones de entrada permitió blindar los casos de uso matemáticos en una fracción del tiempo habitual.

---

### 2. El Ingeniero como Orquestador y la Necesidad de Criterio Propio
El uso de IA **no reemplaza el conocimiento técnico ni la rigurosidad de ingeniería**. Un modelo generativo sin supervisión puede introducir errores sutiles o código inviable si se aplica *copy-paste* sin auditoría:

1. **Restricciones Multiplataforma Estrictas (KMP):**
   * *Riesgo de la IA:* Los LLMs tienden frecuentemente a sugerir librerías exclusivas de la JVM/Android (`java.time.LocalDate`, `java.util.UUID`, `java.util.Date`, `ViewModel` de AndroidX nativo) dentro del código compartido.
   * *Criterio y Corrección:* El ingeniero impuso directivas estrictas para forzar el uso exclusivo de `kotlinx.datetime`, `kotlin.uuid.Uuid`, `lifecycle-viewmodel-compose` y Koin multiplataforma en `commonMain`, asegurando compatibilidad 100% nativa con iOS.
2. **Corrección de Problemas de Zona Horaria y Persistencia:**
   * *Riesgo de la IA:* Conversión directa de `Instant` a texto mediante `toString().substringBefore("T")`, lo cual evalúa en UTC 00:00:00Z y provoca que los entrenamientos guardados a altas horas de la noche en Argentina (UTC-3) se guarden con la fecha del día siguiente.
   * *Criterio y Corrección:* Se diseñó un helper centralizado `DateTimeUtil` que contextualiza el instante temporal contra la zona horaria del sistema del usuario (`TimeZone.currentSystemDefault()`) y parsea fechas al mediodía UTC (`T12:00:00Z`) para garantizar invariabilidad de calendario.
3. **Manejo de Recomposiciones y Rendimiento en Compose:**
   * *Riesgo de la IA:* Generación de listas `LazyColumn` sin definir claves de identidad (`key`), provocando recomposiciones pesadas y saltos de scroll.
   * *Criterio y Corrección:* Se estructuraron todos los bloques `items(items = list, key = { it.id })` y se garantizó la inmutabilidad de todas las data classes de `UiState`.
4. **Resiliencia ante Fallos de Red y Cero Crashes:**
   * *Riesgo de la IA:* Asumir respuestas exitosas inmediatas de red en Supabase o Gemini sin control de excepciones.
   * *Criterio y Corrección:* Toda interacción con la nube fue encapsulada en bloques `runCatching`/`try-catch`, actualizando estados visuales con `ErrorMessage` y botones de reintento para que la aplicación jamás sufra un cierre inesperado.

---

### 3. Google Gemini API: Inteligencia Artificial Integrada en el Producto
Además de utilizar IA en el proceso de desarrollo, la aplicación integra el modelo **Google Gemini** como feature central:
* **Contexto Dinámico en Tiempo Real:** Mediante el caso de uso `BuildAiUserDataContextUseCase`, la aplicación consolida el peso actual del usuario, su catálogo de ejercicios y sus últimas 15 sesiones de entrenamiento para inyectarlos en el prompt del sistema.
* **Entrenador Personal Adaptativo:** El usuario puede consultar recomendaciones sobre volumen, descansos, técnicas de sobrecarga progresiva y ajustes de rutina basados exactamente en sus datos históricos reales.
* **Manejo Inteligente de Cuotas:** Implementación de reintentos automáticos y soporte para fallbacks de modelos (`gemini-flash-lite-latest` y variantes) para mantener siempre una respuesta fluida sin bloquear la UI.


---

## 6. Guía de Compilación y Ejecución Local

### Requisitos Previos
* **Java Development Kit (JDK)**: Versión 17 o superior.
* **Android Studio**: Ladybug / Meerkat o superior con Android SDK configurado.
* **Xcode**: Versión 15 o superior (necesario para compilar y ejecutar en el simulador o dispositivo iOS en macOS).
* **Git**: Para el control de versiones.

---

### Paso 1: Clonar el Repositorio
```bash
git clone https://github.com/marcosnpereira03/GymTracker.git
cd GymTracker
```

---

### Paso 2: Configurar Variables en `local.properties`
Crea o edita el archivo `local.properties` en la raíz del proyecto para definir las credenciales (estas se inyectan automáticamente en tiempo de compilación mediante `AppConfig` sin exponerse en el repositorio):

```properties
# Credenciales de Supabase
SUPABASE_URL=https://tu-proyecto.supabase.co
SUPABASE_ANON_KEY=tu-anon-key-de-supabase

# Clave de API de Google Gemini (opcional, para el Coach de IA)
GEMINI_API_KEY=tu-api-key-de-gemini
```

> **Nota:** La aplicación incluye valores de prueba y almacenamiento local de contingencia, por lo que puede ejecutarse directamente incluso sin configurar una base de datos externa.

---

### Paso 3: Inicializar la Base de Datos en Supabase (Opcional)
Si deseas conectar tu propia instancia de Supabase:
1. Accede al **SQL Editor** en tu panel de Supabase.
2. Ejecuta el script de migración ubicado en:
   [`supabase/migrations/01_initial_schema.sql`](supabase/migrations/01_initial_schema.sql)
   [`supabase/migrations/01_initial_schema.sql`](supabase/migrations/02_storage_avatars.sql)
4. Esto configurará las tablas (`ejercicios`, `entrenamientos`, `series_realizadas`, `pesajes`), los índices de rendimiento y las políticas de seguridad (RLS).

---

### Paso 4: Compilación y Ejecución

#### En Android:

**Opción A — Desde Android Studio:**
1. Abre la carpeta del proyecto en Android Studio.
2. Espera a que finalice la sincronización de Gradle.
3. Selecciona la configuración de ejecución `androidApp` en la barra superior.
4. Elige un emulador o dispositivo físico y presiona **Run** (o `Shift + F10`).

**Opción B — Desde la Terminal (Gradle CLI):**
```bash
# Compilar el APK de depuración
./gradlew :androidApp:assembleDebug

# Instalar y ejecutar directamente en un emulador o dispositivo conectado
./gradlew :androidApp:installDebug
```

---

#### En iOS (requiere macOS y Xcode):

**Opción A — Desde Xcode:**
1. Abre el proyecto de Xcode desde la terminal o el Finder:
   ```bash
   open iosApp/iosApp.xcodeproj
   ```
2. Selecciona el esquema `iosApp` y tu simulador de iOS de preferencia (ej. iPhone 15 Pro).
3. Presiona **Cmd + R** o el botón **Run** para compilar y lanzar la aplicación.

**Opción B — Compilación previa del framework compartido desde Gradle:**
```bash
# Compila el framework embebido para el simulador de iOS
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

---

#### Ejecución de Tests Unitarios Multiplataforma:
```bash
./gradlew :shared:testDebugUnitTest
```
