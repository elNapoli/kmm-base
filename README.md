# Napoli KMM Base Library

**Librería base Kotlin Multiplatform Mobile (KMM) con Clean Architecture, MVI y MVVM**

Una librería completa y lista para producción que proporciona una arquitectura robusta para
aplicaciones Android e iOS, con gestión de estado moderna, navegación modular y carga perezosa de
features.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Arquitectura](#arquitectura)
- [Características Principales](#características-principales)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Capas de la Arquitectura](#capas-de-la-arquitectura)
- [Patrones de Diseño](#patrones-de-diseño)
- [Sistema de Navegación](#sistema-de-navegación)
- [Gestión de Features](#gestión-de-features)
- [Inyección de Dependencias](#inyección-de-dependencias)
- [ViewModels y Estado](#viewmodels-y-estado)
- [Código Específico de Plataforma](#código-específico-de-plataforma)
- [Utilidades y Extensiones](#utilidades-y-extensiones)
- [Ejemplos de Uso](#ejemplos-de-uso)
- [Dependencias](#dependencias)
- [Configuración](#configuración)

---

## Descripción General

**NapoliKmmBase** es una librería base KMM que implementa:

- **Clean Architecture** con separación clara de capas
- **MVI (Model-View-Intent)** para gestión de estado unidireccional
- **MVVM (Model-View-ViewModel)** con binding reactivo
- **Feature modular** con carga perezosa
- **Navegación centralizada** multi-feature
- **Inyección de dependencias** con Koin
- **Código compartido** entre Android e iOS

### Plataformas Soportadas

- **Android**: API 24+ (Android 7.0)
- **iOS**: ARM64 y Simulador

---

## Arquitectura

```
┌─────────────────────────────────────────┐
│      PRESENTATION LAYER (UI)            │
│  - ViewModels (BaseViewModel)           │
│  - ViewState, ViewAction, ViewEffect    │
│  - Interceptores de Estado              │
│  - Procesadores de Acciones             │
└─────────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│      DOMAIN LAYER (Business Logic)      │
│  - Use Cases (FlowUseCase)              │
│  - Interfaces de Repositorios           │
│  - Modelos de Dominio                   │
│  - Providers (UserAgentProvider)        │
└─────────────────────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│        DATA LAYER (Data Sources)        │
│  - Implementaciones de Repositorios     │
│  - Data Sources                         │
│  - Models & Mappers                     │
└─────────────────────────────────────────┘
```

---

## Características Principales

### 1. Gestión de Estado Moderna (MVI)

```kotlin
// Flujo unidireccional de estado
User Action → ViewAction → Processor → Mutation → ViewState → UI
```

**Componentes clave:**

- `ViewState`: Estado inmutable de la UI
- `ViewAction`: Intenciones/acciones del usuario
- `ViewEffect`: Efectos de un solo uso (navegación, toasts)
- `Mutation`: Función pura de transformación de estado

### 2. BaseViewModel Genérico

```kotlin
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
    initialState: S,
    initialAction: A? = null
)
```

**Características:**

- StateFlow para estado reactivo
- Channel para acciones
- Flow para efectos
- Throttling automático (500ms)
- Interceptores para logging/analytics
- Lifecycle-aware

### 3. Sistema de Features Modular

```kotlin
interface Feature {
    val featureName: String
    val priority: Int
    fun provideDependencies(): List<Module>
    fun initialize()
    fun dispose()
}
```

**Tipos de Features:**

- `Feature`: Feature básico
- `NavigationFeature`: Con navegación
- `AsyncFeature`: Inicialización async
- `ConfigurableFeature`: Con configuración

### 4. Carga Perezosa de Features

```kotlin
@Composable
fun LazyFeatureLoader(
    featureName: String,
    content: @Composable () -> Unit
)
```

**Beneficios:**

- Reducción del tamaño inicial de la app
- Inicio más rápido
- Carga bajo demanda
- Auto-carga basada en rutas

### 5. Use Cases Reactivos

```kotlin
abstract class FlowUseCase<P, T, E : UseCaseError>(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    abstract suspend fun executeOnBackground(params: P): Flow<T>
    fun execute(params: P): Flow<UseCaseState<T, E>>
}
```

**Estados:**

- `Loading`: Cargando
- `Success<T>`: Éxito con datos
- `Error<E>`: Error con tipo específico

---

## Estructura del Proyecto

```
napoli-kmm-base/
├── base/                                    # Módulo KMM principal
│   ├── src/
│   │   ├── commonMain/                     # Código compartido
│   │   │   └── kotlin/com/baldomeronapoli/kmm/base/
│   │   │       ├── di/                     # Inyección de dependencias
│   │   │       ├── domain/                 # Capa de dominio
│   │   │       │   ├── models/            # Modelos de dominio
│   │   │       │   ├── providers/         # Providers
│   │   │       │   ├── repositories/      # Interfaces de repositorios
│   │   │       │   └── usecases/          # Use cases
│   │   │       ├── feature/               # Sistema de features
│   │   │       ├── navigation/            # Sistema de navegación
│   │   │       ├── presentation/          # Capa de presentación
│   │   │       │   ├── viewmodel/        # BaseViewModel
│   │   │       │   ├── action/           # Procesadores de acciones
│   │   │       │   ├── state/            # Interceptores de estado
│   │   │       │   └── models/           # Modelos de UI
│   │   │       └── utils/                # Utilidades y extensiones
│   │   ├── androidMain/                   # Implementaciones Android
│   │   ├── iosMain/                       # Implementaciones iOS
│   │   └── commonTest/                    # Tests compartidos
│   └── build.gradle.kts
├── iosApp/                                 # App de ejemplo iOS
├── build.gradle.kts                        # Configuración raíz
└── settings.gradle.kts
```

---

## Capas de la Arquitectura

### PRESENTATION LAYER

**Ubicación**: `base/src/commonMain/kotlin/com/baldomeronapoli/kmm/base/presentation/`

#### Componentes Core

**1. ViewState** (`presentation/ViewState.kt`)

- Estado base para todas las pantallas
- Propiedades comunes: topBar, bottomBar, etc.
- Método `toUiScreenState()` para UI

**2. ViewAction** (`presentation/ViewAction.kt`)

- Clase marcadora para acciones de usuario
- Patrón sealed class

**3. ViewEffect** (`presentation/ViewEffect.kt`)

- Efectos de un solo uso
- Navegación, toasts, diálogos

**4. BaseViewModel** (`presentation/viewmodel/BaseViewModel.kt`)

- ViewModel genérico para todas las pantallas
- Gestión de estado con StateFlow
- Procesamiento de acciones
- Emisión de efectos

```kotlin
abstract class BaseViewModel<S : ViewState, A : ViewAction, E : ViewEffect>(
    initialState: S,
    initialAction: A? = null
) : ViewModel() {

    val state: StateFlow<S>
    val effect: Flow<E>

    protected abstract fun processAction(
        action: A,
        sendEffect: (E) -> Unit
    ): Flow<Mutation<S>>

    protected fun sendAction(action: A)
    protected fun sendEffect(effect: E)
}
```

**5. ActionProcessor** (`presentation/action/ActionProcessor.kt`)

- Procesa acciones en mutaciones
- Método: `process(action, sendEffect): Flow<Mutation<S>>`

**6. StateInterceptor & ActionInterceptor**

- `presentation/state/StateInterceptor.kt`
- `presentation/action/ActionInterceptor.kt`
- Logging, analytics, persistencia

**7. ResourceResolver** (`presentation/action/ResourceResolver.kt`)

- Resolución de recursos multiplataforma
- Acceso a strings fuera de Composables

---

### DOMAIN LAYER

**Ubicación**: `base/src/commonMain/kotlin/com/baldomeronapoli/kmm/base/domain/`

#### Use Cases

**FlowUseCase** (`domain/usecases/FlowUseCase.kt`)

- Base para todos los use cases
- Retorna `Flow<UseCaseState<T, E>>`
- Dispatcher configurable (default: IO)
- Validación de parámetros opcional
- Manejo de excepciones

```kotlin
abstract class FlowUseCase<P, T, E : UseCaseError>(
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    protected open val paramsValidator: ParamsValidator<P>? = null
    protected open val exceptionHandler: ExceptionHandler<E>? = null

    abstract suspend fun executeOnBackground(params: P): Flow<T>

    fun execute(params: P): Flow<UseCaseState<T, E>> {
        // Validación → Loading → Ejecución → Success/Error
    }
}
```

**UseCaseState** (`domain/usecases/UseCaseState.kt`)

- `Loading`: Cargando
- `Success<T>`: Éxito
- `Error<E>`: Error tipado

**ExceptionHandler** (`domain/usecases/ExceptionHandler.kt`)

- Mapeo de excepciones a errores de dominio
- Logging automático

**ParamsValidator** (`domain/usecases/ParamsValidator.kt`)

- Validación de entrada

#### Modelos de Dominio

**UserAgent** (`domain/models/UserAgent.kt`)

- Información del user agent
- Pattern Builder
- Serialización con delimitadores

**ConnectionState & ConnectionType**

- `domain/models/ConnectionState.kt`
- `domain/models/ConnectionType.kt`
- Estado de conexión: CONNECTED, DISCONNECTED
- Tipo: WIFI, MOBILE, ETHERNET, VPN, etc.

#### Providers

**UserAgentProvider**

- `domain/providers/UserAgentProvider.kt` (interface)
- Implementaciones específicas por plataforma
- **Android**: Requiere Context, OS, versión, modelo, fabricante
- **iOS**: UIDevice, NSBundle, detección de simulador/tablet/iPhone

#### Repositorios

**LoggingRepository** (`domain/repositories/LoggingRepository.kt`)

- Interface para logging
- Implementaciones específicas por plataforma

---

### FEATURE MANAGEMENT LAYER

**Ubicación**: `base/src/commonMain/kotlin/com/baldomeronapoli/kmm/base/feature/`

#### Interfaces Core

**Feature** (`feature/Feature.kt`)

- Contrato base para todas las features

```kotlin
interface Feature {
    val featureName: String
    val priority: Int get() = 100
    fun provideDependencies(): List<Module>
    fun initialize()
    fun dispose()
}
```

**ConfigurableFeature** (`feature/ConfigurableFeature.kt`)

- Feature con configuración dinámica
- `configure(config: FeatureConfig)`

**AsyncFeature** (`feature/AsyncFeature.kt`)

- Inicialización asíncrona
- `suspend fun initializeAsync()`

**NavigationFeature** (`feature/NavigationFeature.kt`)

- Feature con navegación

```kotlin
interface NavigationFeature : Feature {
    var navigationCoordinator: NavigationCoordinator?
    fun NavGraphBuilder.registerNavigation()
    fun onNavigationReady(navController: NavHostController)
}
```

#### FeatureManager

**FeatureManager** (`feature/FeatureManager.kt`)

- Gestión centralizada del ciclo de vida de features

**Métodos principales:**

**Registro:**

- `register(feature: Feature)`
- `registerAll(vararg features: Feature)`

**Inyección de Dependencias:**

- `getAllDependencyModules(): List<Module>`
- `getCriticalDependencyModules(maxPriority: Int): List<Module>`
- `loadFeatureModules(featureName: String): Boolean`
- `isFeatureLoaded(featureName: String): Boolean`

**Inicialización:**

- `initializeAll()`
- `initializeFeature(featureName: String)`
- `suspend fun initializeAllAsync()`
- `isFeatureInitialized(featureName: String): Boolean`

**Mapeo de Navegación:**

- `mapRouteToFeature(route: String, featureName: String)`
- `mapRoutesToFeature(routes: List<String>, featureName: String)`
- `getFeatureForRoute(route: String): String?`
- `NavGraphBuilder.registerAllNavigationRoutes()`

**Coordinación:**

- `notifyNavigationReady(navController: NavHostController)`
- `disposeAll()`

#### Carga Perezosa

**LazyFeatureLoader** (`feature/LazyFeatureLoader.kt`)

- Composable que carga features bajo demanda

```kotlin
@Composable
fun LazyFeatureLoader(
    featureName: String,
    showLoadingIndicator: Boolean = true,
    loadingContent: (@Composable () -> Unit)? = null,
    onLoadComplete: (() -> Unit)? = null,
    onLoadError: ((Throwable) -> Unit)? = null,
    content: @Composable () -> Unit
)
```

**LazyNavigation** (`feature/LazyNavigation.kt`)

- Helpers para navegación perezosa
- `lazyNavigation()`, `lazyComposable()`, `lazyComposableWithLoader()`

#### FeatureBuilder DSL

**FeatureBuilder** (`feature/FeatureBuilder.kt`)

- DSL fluido para crear features

```kotlin
val myFeature = feature("my_feature") {
    priority(50)
    dependencies { listOf(myModule) }
    navigation {
        composable("route") { MyScreen() }
    }
    initialize { /* init code */ }
    dispose { /* cleanup */ }
}
```

---

## Patrones de Diseño

### MVI (Model-View-Intent)

**Flujo unidireccional:**

```
User Action → ViewAction → ActionProcessor → Mutation → ViewState → UI
     ↓                                                                  ↓
  sendAction()                                                  collectAsState()
```

**Componentes:**

1. **Model** = ViewState (inmutable, single source of truth)
2. **View** = Composables (observan estado, emiten acciones)
3. **Intent** = ViewAction (intención del usuario)

### MVVM (Model-View-ViewModel)

**Implementación:**

- `BaseViewModel<S, A, E>` extiende ViewModel
- StateFlow para estado
- Flow para efectos
- Lifecycle-aware

### Clean Architecture

**Independencia de capas:**

- **Presentation**: Sin código específico de plataforma
- **Domain**: Lógica de negocio pura
- **Data**: Implementaciones de repositorios

**Dirección de dependencias**: Presentation → Domain → Data

### Repository Pattern

Interfaces en Domain, implementaciones en Data

### Dependency Injection

Koin-based, carga perezosa, módulos por feature

### Lazy Loading / Arquitectura Modular

Features cargadas bajo demanda, reducción de tamaño inicial

---

## Sistema de Navegación

**Ubicación**: `base/src/commonMain/kotlin/com/baldomeronapoli/kmm/base/navigation/`

### Componentes Core

**NavigationCommand** (`navigation/NavigationCommand.kt`)

- Interface marcadora para eventos de navegación

**NavigationCoordinator** (`navigation/NavigationCoordinator.kt`)

- Gestión centralizada de navegación

```kotlin
interface NavigationCoordinator {
    fun setNavController(navController: NavHostController)
    fun registerHandler(handler: NavigationHandler)
    fun navigate(command: NavigationCommand): Boolean
    fun getHandler(featureName: String): NavigationHandler?
    fun hasHandler(featureName: String): Boolean
    fun clear()
}
```

**NavigationHandler** (`navigation/NavigationHandler.kt`)

- Handler específico por feature

```kotlin
interface NavigationHandler {
    val featureName: String
    fun handle(command: NavigationCommand, navController: NavHostController): Boolean
}
```

### Integración de Navegación

1. Feature implementa `NavigationFeature`
2. Registra grafo de navegación en `registerNavigation()`
3. Manager construye grafo completo con `registerAllNavigationRoutes()`
4. Manager notifica cuando NavController está listo

---

## Gestión de Features

### Ciclo de Vida de Features

```
1. REGISTRO
   └── featureManager.register(Feature)

2. CARGA DE DEPENDENCIAS (Perezosa)
   ├── featureManager.loadFeatureModules(featureName)
   └── Módulos Koin cargados

3. INICIALIZACIÓN
   ├── featureManager.initializeFeature(featureName)
   └── feature.initialize()

4. INICIALIZACIÓN ASYNC (Opcional)
   ├── featureManager.initializeAllAsync()
   └── AsyncFeature.initializeAsync()

5. NAVEGACIÓN LISTA
   ├── featureManager.notifyNavigationReady()
   └── NavigationFeature.onNavigationReady()

6. DISPOSE
   ├── featureManager.disposeAll()
   └── Todas las features disposed
```

### Tipos de Features

1. **Simple Feature**: DI + inicialización
2. **Navigation Feature**: DI + grafo de navegación
3. **Async Feature**: Inicialización async
4. **Configurable Feature**: Soporte de configuración

### Sistema de Prioridades

```kotlin
feature.priority  // Default: 100
```

- Número más bajo = mayor prioridad
- Controla orden de inicialización
- Features críticas: `getCriticalDependencyModules(maxPriority: 50)`

### Mapeo de Rutas

```kotlin
featureManager.mapRouteToFeature("login/*", "login_feature")
featureManager.mapRouteToFeature("home", "home_feature")
```

Auto-carga de features al navegar a sus rutas

---

## Inyección de Dependencias

**Framework**: Koin 4.1.1 (compatible con KMM)

### Estructura DI

**BaseModule** (`di/BaseModule.kt`)

```kotlin
fun getModules(): List<Module> = listOf(
    platformModule(),          // Específico de plataforma (expect/actual)
    coreModule()              // Módulos base de la librería
)
```

**Core Module:**

- `ResourceResolver` como singleton

**platformModule() - Expect/Actual**

- **Expect** (`di/platformModule.kt`)
- **Actual Android** (`androidMain/kotlin/.../platformModule.android.kt`)
  ```kotlin
  single<UserAgentProvider> {
      UserAgentProviderImpl(androidContext())
  }
  ```
- **Actual iOS** (`iosMain/kotlin/.../platformModule.ios.kt`)
  ```kotlin
  singleOf(::UserAgentProviderImpl) { bind<UserAgentProvider>() }
  ```

### Uso de DI

**En BaseViewModel:**

```kotlin
class MyViewModel(
    private val useCase: MyUseCase,
    private val repository: MyRepository
) : BaseViewModel<State, Action, Effect>()
```

**En Composables:**

```kotlin
val viewModel = koinViewModel<MyViewModel>()
```

**En Features:**

```kotlin
override fun provideDependencies(): List<Module> = listOf(
    myDomainModule,
    myDataModule
)
```

---

## ViewModels y Estado

### Arquitectura de Gestión de Estado

**Patrón Triple**: State → Action → Effect

```
┌─────────────────────────────────────────────────────┐
│                   STATE MANAGEMENT                  │
├─────────────────────────────────────────────────────┤
│  UI Event → Channel → Mutation → StateFlow         │
│  sendAction → processAction → scan() → state       │
│  One-time effects → effectChannel → effect         │
└─────────────────────────────────────────────────────┘
```

### Flujo de Estado en BaseViewModel

```kotlin
val state: StateFlow<S> = actionChannel
    .receiveAsFlow()
    .onStart { /* initial action */ }
    .throttle(500)                          // Anti-spam
    .onEach { actionInterceptor?.onIntercept(it) }  // Log
    .flatMapMerge { processAction(it, ::sendEffect) }  // Ejecutar
    .scan(initialState) { current, mutation -> mutation(current) }  // Aplicar
    .distinctUntilChanged()                 // Solo cambios
    .onEach { stateInterceptor?.onIntercept(it) }  // Log
    .stateIn(viewModelScope, SharingStarted.Lazily, initialState)
```

### Métodos

**Abstractos:**

```kotlin
protected abstract fun processAction(
    action: A,
    sendEffect: (E) -> Unit
): Flow<Mutation<S>>
```

**Protegidos:**

```kotlin
protected fun sendAction(action: A)
protected fun sendEffect(effect: E)
```

### Uso en Composable

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MyEffect.ShowToast -> showToast(effect.message)
                is MyEffect.Navigate -> navigate(effect.route)
            }
        }
    }

    Button(onClick = { viewModel.sendAction(MyAction.Click) }) {
        Text("Click Me")
    }
}
```

### Throttling

**Propósito**: Prevenir spam de acciones (500ms entre acciones)

**Bypass**: Marca la acción con `@DoNotThrottle`

```kotlin
class UrgentAction : ViewAction, DoNotThrottle
```

---

## Código Específico de Plataforma

### Estrategia Multiplataforma

**Patrón**: Declaraciones Expect/Actual

### Android-Specific

**Ubicación**: `base/src/androidMain/`

**DI Module** (`di/platformModule.android.kt`):

```kotlin
actual fun platformModule(): Module = module {
    single<UserAgentProvider> {
        UserAgentProviderImpl(androidContext())
    }
}
```

**UserAgentProvider** (`domain/providers/UserAgentProvider.android.kt`):

**Datos recopilados:**

- OS: "android" o "huawei"
- Versión de OS: Build.VERSION.RELEASE
- Modelo: Build.MODEL
- Fabricante: Build.MANUFACTURER
- Tipo: Emulador/Tablet/Smartphone
- ID de App: packageName
- Versión de App: versionName

### iOS-Specific

**Ubicación**: `base/src/iosMain/`

**DI Module** (`di/platformModule.ios.kt`):

```kotlin
actual fun platformModule(): Module = module {
    singleOf(::UserAgentProviderImpl) { bind<UserAgentProvider>() }
}
```

**UserAgentProvider** (`domain/providers/UserAgentProvider.ios.kt`):

**Datos recopilados:**

- OS: "ios"
- Versión: UIDevice.systemVersion
- Modelo: UIDevice.model
- Fabricante: "Apple"
- Tipo: Simulador/Tablet/iPhone
- ID de App: bundleIdentifier
- Versión: CFBundleShortVersionString

**Frameworks:**

- UIKit (UIDevice)
- Foundation (NSBundle, NSProcessInfo)

---

## Utilidades y Extensiones

**Ubicación**: `base/src/commonMain/kotlin/com/baldomeronapoli/kmm/base/utils/extensions/`

### Flow Extensions

**File**: `utils/extensions/Flow.kt`

**1. throttle(durationMillis: Long)**

```kotlin
fun <T> Flow<T>.throttle(durationMillis: Long): Flow<T>
```

- Limita frecuencia de emisión
- Default: 500ms
- Bypass con `DoNotThrottle`

**2. collectAsStateWithLifecycle()**

```kotlin
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T>
```

- Recolección consciente del ciclo de vida
- Pausa cuando la app está en background

**3. CollectAsEffectWithLifecycle()**

```kotlin
@Composable
fun <T> Flow<T>.CollectAsEffectWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
)
```

- Para efectos de un solo uso
- Navegación, toasts, diálogos

### Navigation Extensions

**File**: `utils/extensions/NavControllerExtensions.kt`

**viewModelFlow()**

```kotlin
fun NavController.viewModelFlow(): Flow<BaseViewModel<*, *, *>>
```

- Obtiene el BaseViewModel de la pantalla actual
- Filtra diálogos
- Retry en NPE
- Usa APIs internas de Android

---

## Ejemplos de Uso

### 1. Crear un Feature con Navegación

```kotlin
val authFeature = feature("auth") {
    priority(10)  // Alta prioridad

    dependencies {
        listOf(authDomainModule, authDataModule, authPresentationModule)
    }

    navigation {
        lazyComposable("auth", "login") {
            LoginScreen()
        }
        composable("register") {
            RegisterScreen()
        }
        composable("forgot_password") {
            ForgotPasswordScreen()
        }
    }

    initialize {
        Napier.d("Auth feature initialized")
    }

    dispose {
        Napier.d("Auth feature disposed")
    }
}

// Registrar feature
featureManager.register(authFeature)

// Mapear rutas
featureManager.mapRoutesToFeature(
    routes = listOf("login", "register", "forgot_password"),
    featureName = "auth"
)
```

### 2. Crear un Use Case

```kotlin
// Error de dominio
sealed interface LoginError : UseCaseError {
    data object InvalidCredentials : LoginError
    data object NetworkError : LoginError
    data class Unknown(val message: String) : LoginError
}

// Parámetros
data class LoginParams(
    val email: String,
    val password: String
)

// Validador de parámetros
class LoginParamsValidator : ParamsValidator<LoginParams> {
    override fun validate(params: LoginParams) {
        require(params.email.isNotBlank()) { "Email cannot be blank" }
        require(params.password.length >= 6) { "Password too short" }
    }
}

// Exception Handler
class LoginExceptionHandler(
    override val loggingRepository: LoggingRepository
) : ExceptionHandler<LoginError>() {

    override fun parseException(throwable: Throwable): LoginError? {
        return when (throwable) {
            is HttpException -> when (throwable.code) {
                401 -> LoginError.InvalidCredentials
                else -> LoginError.NetworkError
            }
            is IOException -> LoginError.NetworkError
            else -> LoginError.Unknown(throwable.message ?: "Unknown error")
        }
    }
}

// Use Case
class LoginUseCase(
    private val authRepository: AuthRepository,
    exceptionHandler: LoginExceptionHandler,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : FlowUseCase<LoginParams, User, LoginError>(coroutineDispatcher) {

    override val paramsValidator = LoginParamsValidator()
    override val exceptionHandler = exceptionHandler

    override suspend fun executeOnBackground(params: LoginParams): Flow<User> {
        return authRepository.login(params.email, params.password)
    }
}
```

### 3. Crear un ViewModel

```kotlin
// State
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState()

// Actions
sealed interface LoginAction : ViewAction {
    data class EmailChanged(val email: String) : LoginAction
    data class PasswordChanged(val password: String) : LoginAction
    data object LoginClicked : LoginAction
    data object ErrorDismissed : LoginAction
}

// Effects
sealed interface LoginEffect : ViewEffect {
    data object NavigateToHome : LoginEffect
    data class ShowError(val message: String) : LoginEffect
}

// ViewModel
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val resourceResolver: ResourceResolver
) : BaseViewModel<LoginState, LoginAction, LoginEffect>(
    initialState = LoginState()
) {

    // Interceptores opcionales
    override val stateInterceptor = object : StateInterceptor<LoginState> {
        override suspend fun onIntercept(state: LoginState) {
            Napier.d("New state: $state")
        }
    }

    override fun processAction(
        action: LoginAction,
        sendEffect: (LoginEffect) -> Unit
    ): Flow<Mutation<LoginState>> {
        return when (action) {
            is LoginAction.EmailChanged -> flowOf { state ->
                state.copy(email = action.email, error = null)
            }

            is LoginAction.PasswordChanged -> flowOf { state ->
                state.copy(password = action.password, error = null)
            }

            LoginAction.LoginClicked -> {
                loginUseCase.execute(LoginParams(state.value.email, state.value.password))
                    .map { result ->
                        when (result) {
                            is UseCaseState.Loading -> { state ->
                                state.copy(isLoading = true, error = null)
                            }

                            is UseCaseState.Success -> { state ->
                                sendEffect(LoginEffect.NavigateToHome)
                                state.copy(isLoading = false)
                            }

                            is UseCaseState.Error -> { state ->
                                val errorMessage = when (result.error) {
                                    LoginError.InvalidCredentials -> "Invalid credentials"
                                    LoginError.NetworkError -> "Network error"
                                    is LoginError.Unknown -> result.error.message
                                    null -> "Unknown error"
                                }
                                sendEffect(LoginEffect.ShowError(errorMessage))
                                state.copy(isLoading = false, error = errorMessage)
                            }
                        }
                    }
            }

            LoginAction.ErrorDismissed -> flowOf { state ->
                state.copy(error = null)
            }
        }
    }
}
```

### 4. Crear un Composable

```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    navigateToHome: () -> Unit,
    navigateToRegister: () -> Unit
) {
    // Observar estado
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Manejar efectos
    viewModel.effect.CollectAsEffectWithLifecycle { effect ->
        when (effect) {
            LoginEffect.NavigateToHome -> navigateToHome()
            is LoginEffect.ShowError -> {
                // Mostrar SnackBar o Toast
            }
        }
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = state.email,
            onValueChange = { viewModel.sendAction(LoginAction.EmailChanged(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { viewModel.sendAction(LoginAction.PasswordChanged(it)) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.error != null) {
            Text(
                text = state.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = { viewModel.sendAction(LoginAction.LoginClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        TextButton(
            onClick = navigateToRegister,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Don't have an account? Register")
        }
    }
}
```

### 5. Registrar Navigation Feature en App

```kotlin
@Composable
fun App() {
    val navController = rememberNavController()

    // Inicializar FeatureManager
    LaunchedEffect(Unit) {
        featureManager.registerAll(authFeature, homeFeature, profileFeature)
        featureManager.initializeAll()
        featureManager.initializeAllAsync()
        featureManager.notifyNavigationReady(navController)
    }

    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        // Registrar todas las navegaciones de features
        featureManager.registerAllNavigationRoutes()
    }
}
```

### 6. Lazy Loading de Feature

```kotlin
@Composable
fun AppNavigation() {
    NavHost(startDestination = "splash") {
        composable("splash") {
            SplashScreen()
        }

        // Feature cargado perezosamente
        lazyComposableWithLoader(
            featureName = "home_feature",
            route = "home"
        ) {
            HomeScreen()
        }

        // Navegación anidada con carga perezosa
        lazyNavigation(
            route = "profile",
            startDestination = "profile/main"
        ) {
            LazyFeatureLoader(featureName = "profile_feature") {
                composable("profile/main") { ProfileMainScreen() }
                composable("profile/edit") { ProfileEditScreen() }
                composable("profile/settings") { ProfileSettingsScreen() }
            }
        }
    }
}
```

---

## Dependencias

### Core KMM

```kotlin
// Kotlin & Coroutines
kotlin = "2.3.0"
kotlinx - datetime = "0.6.1"

// Compose Multiplatform
compose = "1.10.0"
compose - navigation = "2.9.1"

// Android
androidx - activity - compose = "1.12.2"
androidx - lifecycle - viewmodel - compose = "2.9.6"
androidx - lifecycle - runtime - compose = "2.9.6"
androidx - core - ktx = "1.17.0"

// DI
koin = "4.1.1"

// Logging
napier = "2.7.1"

// Testing
junit = "4.13.2"
androidx - test - ext - junit = "1.2.1"
```

### Build System

```kotlin
android - gradle - plugin = "8.13.2"
kotlin - gradle - plugin = "2.3.0"
compose - compiler = "2.3.0"
```

---

## Configuración

### 1. Gradle (Project Level)

```kotlin
// build.gradle.kts (root)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}
```

### 2. Gradle (Module Level)

```kotlin
// base/build.gradle.kts
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "base"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.kotlinx.datetime)
            implementation(libs.napier)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }
    }
}

android {
    namespace = "cl.baldomeronapoli.kmm.base"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
```

### 3. Inicialización en la App

**Android:**

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(BaseModule.getModules())
        }

        // Registrar features
        featureManager.registerAll(
            authFeature,
            homeFeature,
            profileFeature
        )

        // Inicializar features críticas
        featureManager.initializeAll()
    }
}
```

**iOS:**

```swift
@main
struct MyApp: App {
    init() {
        KoinKt.doInitKoin()

        // Registrar features
        FeatureManager.shared.registerAll(
            features: [authFeature, homeFeature, profileFeature]
        )

        // Inicializar
        FeatureManager.shared.initializeAll()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

---

## Publicación

### Maven Local

```bash
./gradlew publishToMavenLocal
```

### GitHub Packages

```kotlin
// Configurar en build.gradle.kts
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/USERNAME/REPO")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

```bash
export GITHUB_ACTOR=your_username
export GITHUB_TOKEN=your_token
./gradlew publish
```

---

## Mejores Prácticas

### 1. Feature Modularization

- Una feature = un módulo independiente
- Dependencias claras entre features
- Comunicación vía NavigationCoordinator

### 2. State Management

- Estado inmutable
- Mutaciones puras
- Efectos de un solo uso
- No lógica de negocio en UI

### 3. Use Cases

- Un caso de uso = una responsabilidad
- Validación de parámetros
- Manejo de errores tipados
- Testing fácil

### 4. Testing

```kotlin
class LoginViewModelTest {
    @Test
    fun `when login succeeds, navigate to home`() = runTest {
        // Given
        val useCase = FakeLoginUseCase(success = true)
        val viewModel = LoginViewModel(useCase, resourceResolver)

        // When
        viewModel.sendAction(LoginAction.LoginClicked)

        // Then
        viewModel.effect.test {
            assertEquals(LoginEffect.NavigateToHome, awaitItem())
        }
    }
}
```

### 5. Resource Management

- Usa ResourceResolver para acceso a recursos
- No hardcodear strings
- Recursos multiplataforma

### 6. Error Handling

- Errores tipados (sealed class)
- Mapeo de excepciones
- Logging automático
- UI user-friendly

---

## Roadmap

- [ ] Soporte para watchOS
- [ ] Soporte para macOS/Desktop
- [ ] Testing utilities
- [ ] Sample app completa
- [ ] CI/CD pipeline
- [ ] Documentation website

---

## Licencia

MIT License

---

## Autor

**Baldomero Napoli**

---

## Contribuir

Las contribuciones son bienvenidas. Por favor:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

---

## Soporte

Para preguntas o problemas, abre un issue en el repositorio.

---

**Happy Coding with Clean Architecture! 🚀**
