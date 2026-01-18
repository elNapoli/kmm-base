package cl.baldomeronapoli.kmm.base.feature

import org.koin.core.module.Module


/**
 * Contrato base para todos los módulos de features de la aplicación.
 *
 * Cada feature debe implementar esta interfaz para:
 * - Proveer sus dependencias (DI)
 * - Registrar su navegación
 * - Inicializarse si es necesario
 *
 * Arquitectura:
 * ```
 * app/
 * ├── :feature:login
 * │   └── LoginFeature implements Feature
 * ├── :feature:home
 * │   └── HomeFeature implements Feature
 * └── :base
 *     └── Feature (interface)
 * ```
 */
interface Feature {

    /**
     * Nombre único del feature para identificación y logs.
     * Ejemplo: "login", "home", "profile"
     */
    val featureName: String

    /**
     * Prioridad de inicialización (menor número = mayor prioridad).
     * Útil cuando un feature depende de otro.
     * Por defecto: 100
     */
    val priority: Int get() = 100

    /**
     * Módulos de Koin para inyección de dependencias del feature.
     *
     * Ejemplo:
     * ```kotlin
     * override fun provideDependencies(): List<Module> = listOf(
     *     loginDataModule,
     *     loginDomainModule,
     *     loginPresentationModule
     * )
     * ```
     */
    fun provideDependencies(): List<Module> = emptyList()

    /**
     * Inicialización del feature (opcional).
     * Se llama una vez cuando la app arranca.
     *
     * Útil para:
     * - Configuraciones iniciales
     * - Registro de listeners
     * - Inicialización de SDKs específicos del feature
     *
     * NOTA: Si necesitas Context (Android) u otros recursos de plataforma,
     * inyéctalos a través de Koin en las implementaciones específicas del feature.
     */
    fun initialize() {}

    /**
     * Limpieza de recursos del feature (opcional).
     * Se llama cuando el feature ya no es necesario o la app se cierra.
     */
    fun dispose() {}
}