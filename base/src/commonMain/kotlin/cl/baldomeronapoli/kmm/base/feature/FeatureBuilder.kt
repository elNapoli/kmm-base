package cl.baldomeronapoli.kmm.base.feature

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import cl.baldomeronapoli.kmm.base.navigation.NavigationCoordinator
import org.koin.core.module.Module


// ============================================================================
// DSL PARA FACILITAR LA CREACIÓN DE FEATURES
// ============================================================================

/**
 * Builder DSL para crear features de forma declarativa.
 *
 * El builder decide automáticamente qué tipo de Feature crear:
 * - Si defines `.navigation { }` → crea NavigationAwareFeature
 * - Si NO defines navegación → crea Feature simple
 *
 * Ejemplos:
 * ```kotlin
 * // Feature SIN navegación (ej: Analytics, Network)
 * val analyticsFeature = feature("analytics") {
 *     priority(10)
 *     dependencies { listOf(analyticsModule) }
 *     initialize { context ->
 *         Analytics.init(context)
 *     }
 * }
 *
 * // Feature CON navegación (ej: Login, Home)
 * val loginFeature = feature("login") {
 *     priority(100)
 *     dependencies { listOf(loginModule) }
 *     navigation {
 *         navigation(
 *             startDestination = "login_form",
 *             route = "login_flow"
 *         ) {
 *             composable("login_form") { LoginScreen() }
 *         }
 *     }
 * }
 * ```
 */
class FeatureBuilder(private val name: String) {

    private var priorityValue: Int = 100
    private var dependenciesProvider: () -> List<Module> = { emptyList() }
    private var navigationRegistration: (NavGraphBuilder.() -> Unit)? = null
    private var initializationBlock: () -> Unit = {}
    private var disposeBlock: () -> Unit = {}

    fun priority(value: Int) {
        priorityValue = value
    }

    fun dependencies(provider: () -> List<Module>) {
        dependenciesProvider = provider
    }

    fun navigation(block: NavGraphBuilder.() -> Unit) {
        navigationRegistration = block
    }

    fun initialize(block: () -> Unit) {
        initializationBlock = block
    }

    fun dispose(block: () -> Unit) {
        disposeBlock = block
    }

    fun build(): Feature {
        return if (navigationRegistration != null) {
            object : NavigationFeature {
                override val featureName: String = name
                override val priority: Int = priorityValue
                override var navigationCoordinator: NavigationCoordinator? = null

                override fun provideDependencies(): List<Module> = dependenciesProvider()

                override fun NavGraphBuilder.registerNavigation() {
                    navigationRegistration?.invoke(this)!!
                }

                override fun initialize() {
                    initializationBlock()
                }

                override fun onNavigationReady(navController: NavHostController) {
                    // Default: no hace nada
                }

                override fun dispose() {
                    disposeBlock()
                }
            }
        } else {
            object : Feature {
                override val featureName: String = name
                override val priority: Int = priorityValue

                override fun provideDependencies(): List<Module> = dependenciesProvider()

                override fun initialize() {
                    initializationBlock()
                }

                override fun dispose() {
                    disposeBlock()
                }
            }
        }
    }
}

/**
 * DSL function para crear features fácilmente.
 */
fun feature(name: String, block: FeatureBuilder.() -> Unit): Feature {
    return FeatureBuilder(name).apply(block).build()
}



