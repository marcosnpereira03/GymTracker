# GymTracker — Plataforma Multiplataforma de Registro y Análisis de Fuerza

---

## 1. ¿De qué trata el proyecto?

**GymTracker** es una aplicación móvil multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** (Android & iOS), diseñada para el registro de alta precisión, análisis biométrico y seguimiento riguroso de la sobrecarga progresiva en atletas de fuerza e hipertrofia.

### Problemática Resuelta
Las aplicaciones tradicionales de registro suelen operar como simples blocs de notas digitales o planillas estáticas, careciendo de herramientas analíticas de nivel profesional:
* **Falta de cuantificación del esfuerzo real**: No contemplan la proximidad al fallo muscular (**RIR — Reps In Reserve**).
* **Desconexión analítica**: Dificultad para correlacionar el tonelaje de volumen acumulado con la distribución por grupo muscular o la tendencia del peso corporal.
* **Falta de feedback inteligente**: Ausencia de orientación técnica automatizada basada en el historial del atleta.
* **Fragilidad ante desconexión**: Bloqueo o pérdida de datos ante interrupciones de red.

### Funcionalidades Principales
1. **Registro Granular de Entrenamientos**: Carga de series en tiempo real especificando peso ($\text{kg}$), repeticiones e intensidad subjetiva mediante $\text{RIR}$ ($0 \le \text{RIR} \le 10$).
2. **Cálculo de 1RM Estimado Automatizado**: Proyección matemática de la repetición máxima en base a la fórmula de Epley ponderada por el esfuerzo en reserva, evitando someter al atleta a repeticiones máximas lesivas.
3. **Analítica de Volumen y Composición Corporal**: Gráficos interactivos en tiempo real con filtrado temporal dinámico (Diario, Semanal, Mensual, Histórico) y renderizado de curvas de Bézier mediante Compose Canvas.
4. **Coach de Inteligencia Artificial (Google Gemini)**: Entrenador personal interactivo integrado con la API de Gemini que analiza en tiempo real el catálogo de ejercicios y las sesiones registradas para ofrecer recomendaciones de entrenamiento.
5. **Resiliencia Total y Modo Offline**: Sincronización transparente con PostgreSQL en Supabase protegida por Row-Level Security (RLS) y almacenamiento resiliente en memoria ante caídas de conexión.

---

## 2. ¿Qué arquitectura se eligió y por qué?

El proyecto implementa **Clean Architecture** junto con el patrón de presentación **MVVM (Model-View-ViewModel)** y **Unidirectional Data Flow (UDF)**, compartiendo el **100%** del código de lógica de negocio, datos y UI en `commonMain`.

```text
shared/src/commonMain/kotlin/org/marcosnpereira03/gymtracker/
├── domain/                          # PURO KOTLIN (Sin dependencias externas ni frameworks)
│   ├── model/                       # Modelos inmutables (Workout, Exercise, WorkoutSet, BodyWeightLog)
│   ├── repository/                  # Interfaces de abstracción de datos
│   └── usecase/                     # Lógica de negocio (CalculateOneRepMax, CalculateWorkoutVolume, etc.)
├── data/                            # Implementación de datos y persistencia
│   ├── remote/                      # Clientes HTTP (SupabaseClientFactory, GeminiApiClient, DTOs)
│   ├── mapper/                      # Mapeo bidireccional entre DTOs y modelos de dominio
│   └── repository/                  # Implementaciones de repositorios con caché y fallback offline
├── di/                              # Inyección de dependencias modular con Koin
└── presentation/                    # UI Declarativa con Compose Multiplatform
    ├── theme/                       # Design System Material 3 (Dark Theme deportivo)
    ├── navigation/                  # Enrutamiento tipado (NavHost / Screen routes)
    ├── home/                        # Dashboard principal y resumen diario
    ├── workout/                     # Registro de sesión en vivo y edición de series
    ├── history/                     # Historial de entrenamientos y récords personales (PRs)
    ├── exercises/                   # Catálogo de ejercicios y estimación de 1RM
    ├── profile/                     # Estadísticas temporales, pesajes y perfil
    └── coach/                       # Chatbot interactivo con Google Gemini
```

### Justificación Arquitectónica:
* **Independencia y Testabilidad del Dominio**: La capa `domain` no posee dependencias de Compose, Supabase, Android ni iOS. Todas las fórmulas biomecánicas y reglas de negocio son evaluables mediante tests unitarios rápidos y deterministas en JVM/KMP.
* **Flujo Unidireccional de Datos (UDF)**: Cada `ViewModel` expone un único `StateFlow<UiState>` inmutable. Los componentes Compose reaccionan exclusivamente a cambios en este estado y propagan eventos de usuario hacia el ViewModel, garantizando estabilidad y evitando condiciones de carrera.
* **Separación de Responsabilidades (SoC)**: Los cambios en la capa de datos (como la API de Supabase o la integración de Gemini) no impactan en la lógica de dominio ni en la capa visual.
* **Reutilización Multiplataforma Real**: Al evitar APIs específicas de plataforma (como `java.time.*` o `java.util.UUID`), el código compila de forma idéntica y nativa en Android e iOS.

---

## 3. ¿Qué herramientas de IA se utilizaron y cómo ayudaron a acelerar el desarrollo?

El desarrollo de GymTracker se orquestó adoptando un enfoque de copiloto e ingeniería asistida por Inteligencia Artificial:

### 1. Antigravity IDE & AI Assistant (Google DeepMind)
* **Arquitectura y Estructura KMP**: Asistencia en la configuración del entorno multiplataforma Gradle, inyección de dependencias con **Koin**, y scaffolding de capas según los principios de Clean Architecture.
* **Modelado y Mappers Bidireccionales**: Generación precisa de DTOs serializables con `@SerialName` alineados con el esquema relacional de Supabase y extension functions para conversión de dominio.
* **Implementación de Componentes Gráficos**: Co-diseño de componentes visuales avanzados en **Compose Multiplatform Canvas**, tales como gráficos de volumen con curvas cúbicas de Bézier, sombreado degradado y cuadrículas analíticas.
* **Cobertura y QA Unitario**: Redacción de suites de pruebas unitarias exhaustivas en `commonTest` para validar casos de borde en cálculos de 1RM, tonelaje y manejo de errores.

### 2. Google Gemini API (Coach de IA Integrado en la App)
* **Asesoramiento Personalizado en Tiempo Real**: Se diseñó el cliente `GeminiApiClient` y el caso de uso `BuildAiUserDataContextUseCase`, los cuales estructuran un contexto con los entrenamientos recientes y ejercicios del usuario para alimentar el modelo de lenguaje (`gemini-flash-lite-latest` y fallbacks).
* **Resiliencia ante Sobrecarga de Tráfico**: Detección de límites de tasa (429/503) con reintento automático progresivo mientras la UI mantiene el estado de pensamiento activo, ocultando detalles técnicos al usuario y ofreciendo una experiencia fluida.

---

## 4. Stack Tecnológico y Componentes Clave

| Componente / Tecnología | Propósito | Beneficio Técnico |
| :--- | :--- | :--- |
| **Kotlin Multiplatform (KMP 2.x)** | Core Multiplataforma | 100% de lógica de negocio, datos y modelos compartidos entre plataformas. |
| **Compose Multiplatform** | UI Declarativa | Interfaz nativa compartida para Android e iOS con diseño responsivo. |
| **Supabase Postgrest (`supabase-kt`)** | Backend & Base de Datos | Consultas a PostgreSQL mediante cliente tipado con Row-Level Security (RLS). |
| **Supabase Auth** | Autenticación | Control de sesiones seguras mediante correo y contraseña. |
| **Google Gemini REST API** | Inteligencia Artificial | Chatbot deportivo interactivo con conocimiento del contexto del atleta. |
| **Koin** | Inyección de Dependencias | Framework ligero de DI nativo para Kotlin Multiplatform. |
| **Kotlinx Coroutines & Flow** | Concurrencia Reactiva | Operaciones asíncronas no bloqueantes con flujos reactivos `StateFlow`. |
| **Kotlinx DateTime** | Fechas Multiplataforma | Manejo de tiempos ISO-8601 compatible con PostgreSQL `TIMESTAMPTZ`. |
| **Compose Canvas** | Visualización de Datos | Renderizado de gráficos vectoriales personalizados de volumen y peso. |

---

## 5. Lógica de Negocio y Fórmulas Matemáticas

### 1. Cálculo de 1RM Estimado (`CalculateOneRepMaxUseCase`)
Aplica la fórmula de **Epley** ajustada por las repeticiones en reserva (**RIR**), calculando las repeticiones efectivas al fallo ($r_{\text{eff}} = \text{reps} + \text{rir}$):

$$\text{1RM} = \text{peso\_kg} \times \left(1 + \frac{\text{reps} + \text{rir}}{30.0}\right)$$

* *Caso base*: Si $\text{reps} = 1$ y $\text{rir} = 0$, el 1RM es exactamente $\text{peso\_kg}$.
* *Límites*: Si $\text{peso\_kg} \le 0$ o $\text{reps} \le 0$, retorna $0.0$.

### 2. Cálculo de Tonelaje Total (`CalculateWorkoutVolumeUseCase`)
Calcula el tonelaje acumulado de todas las series válidas de una sesión:

$$\text{Volumen Total (kg)} = \sum_{i=1}^{n} (\text{peso\_kg}_i \times \text{reps}_i)$$

---

## 6. Guía de Compilación y Ejecución Local

### Requisitos Previos
* **Java Development Kit (JDK)**: Versión 17 o superior.
* **Android Studio**: Ladybug / Meerkat o superior con Android SDK configurado.
* **Xcode**: Versión 15+ (necesario únicamente para compilar y ejecutar en el simulador de iOS / macOS).
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
3. Esto configurará las tablas (`ejercicios`, `entrenamientos`, `series_realizadas`, `pesajes`), los índices de rendimiento y las políticas de seguridad (RLS).

---

### Paso 4: Compilar y Ejecutar

#### En Android:
Desde la terminal o desde el botón **Run** en Android Studio:
```bash
# Compilar e instalar en emulador o dispositivo conectado
./gradlew :androidApp:installDebug
```

#### En iOS (requiere macOS y Xcode):
```bash
open iosApp/iosApp.xcworkspace
```
Selecciona el dispositivo/simulador deseado en Xcode y presiona **Cmd + R**.

#### Ejecutar Suite de Tests Unitarios:
```bash
./gradlew :shared:testDebugUnitTest
```