package cl.baldomeronapoli.base.feature

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import cl.baldomeronapoli.logger.Trace
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module

/**
 * Gestor centralizado de features.
 * Maneja el ciclo de vida, inicialización y registro de todos los features.
 */
class FeatureManager {

    private val features = mutableListOf<Feature>()
    private val initializedFeatures = mutableSetOf<String>()
    private val loadedModules = mutableSetOf<String>()
    private val routeToFeatureMap = mutableMapOf<String, String>()

    companion object {
        private const val TAG = "FeatureManager"
    }

    /**
     * Registra un feature en el sistema.
     */
    fun register(feature: Feature) {
        if (features.any { it.featureName == feature.featureName }) {
            throw IllegalStateException("Feature '${feature.featureName}' already registered")
        }
        features.add(feature)
    }

    /**
     * Registra múltiples features.
     */
    fun registerAll(vararg features: Feature) {
        features.forEach { register(it) }
    }

    /**
     * Obtiene todos los features ordenados por prioridad.
     */
    fun getAllFeatures(): List<Feature> {
        return features.sortedBy { it.priority }
    }

    /**
     * Obtiene todos los módulos de DI de todos los features.
     * Usar solo si necesitas que todos los features estén disponibles desde el inicio.
     */
    fun getAllDependencyModules(): List<Module> {
        return features.flatMap { it.provideDependencies() }
    }

    /**
     * Verifica si un feature ya tiene sus módulos cargados.
     *
     * @param featureName Nombre del feature
     * @return true si los módulos del feature ya están cargados
     */
    fun isFeatureLoaded(featureName: String): Boolean {
        return featureName in loadedModules
    }

    /**
     * Verifica si un feature ya está inicializado.
     *
     * @param featureName Nombre del feature
     * @return true si el feature ya está inicializado
     */
    fun isFeatureInitialized(featureName: String): Boolean {
        return featureName in initializedFeatures
    }

    /**
     * Carga los módulos de DI de un feature específico de forma lazy.
     * Lazy loading: Solo carga cuando el feature se necesita.
     *
     * @param featureName Nombre del feature
     * @return true si se cargaron los módulos, false si ya estaban cargados
     * @throws IllegalStateException si el feature no está registrado
     */
    fun loadFeatureModules(featureName: String): Boolean {
        if (featureName in loadedModules) {
            Trace.d(TAG, "Feature '$featureName' modules already loaded")

            return false  // Ya estaba cargado
        }

        val feature = features.find { it.featureName == featureName }
            ?: throw IllegalStateException("Feature '$featureName' not registered")

        return try {
            Trace.d(TAG, "Loading modules for feature: $featureName")

            val modules = feature.provideDependencies()
            if (modules.isEmpty()) {
                Trace.d(TAG, "Feature '$featureName' has no modules to load")
                loadedModules.add(featureName)
                return false
            }

            // Cargar módulos de Koin dinámicamente (compatible con KMM)
            loadKoinModules(modules)

            loadedModules.add(featureName)
            Trace.d("Feature '$featureName' modules loaded successfully (${modules.size} modules)")
            true
        } catch (e: Exception) {
            Trace.e("Error loading modules for feature '$featureName'")
            throw e
        }
    }

    /**
     * Obtiene solo los módulos de features críticos (alta prioridad).
     * Útil para cargar solo lo esencial al inicio.
     */
    fun getCriticalDependencyModules(maxPriority: Int = 50): List<Module> {
        return features
            .filter { it.priority <= maxPriority }
            .flatMap { it.provideDependencies() }
    }

    /**
     * Inicializa todos los features de forma síncrona.
     */
    fun initializeAll() {
        getAllFeatures().forEach { feature ->
            if (feature.featureName !in initializedFeatures) {
                feature.initialize()
                initializedFeatures.add(feature.featureName)
            }
        }
    }

    /**
     * Inicializa un feature específico.
     */
    fun initializeFeature(featureName: String) {
        if (featureName in initializedFeatures) {
            return  // Ya inicializado
        }

        val feature = features.find { it.featureName == featureName }
            ?: throw IllegalStateException("Feature '$featureName' not registered")

        feature.initialize()
        initializedFeatures.add(featureName)
    }

    /**
     * Inicializa features asíncronos en paralelo.
     */
    suspend fun initializeAllAsync() {
        getAllFeatures()
            .filterIsInstance<AsyncFeature>()
            .forEach { feature ->
                if (feature.featureName !in initializedFeatures) {
                    feature.initializeAsync()
                    initializedFeatures.add(feature.featureName)
                }
            }
    }

    /**
     * Notifica a features con navegación cuando el NavController está listo.
     */
    fun notifyNavigationReady(navController: NavHostController) {
        features.filterIsInstance<NavigableFeature>()
            .forEach { it.onNavigationReady(navController) }
    }

    /**
     * Mapea una ruta de navegación a un feature específico.
     * Útil para lazy loading automático basado en la navegación.
     *
     * @param route Ruta de navegación (puede contener comodines)
     * @param featureName Nombre del feature asociado
     */
    fun mapRouteToFeature(route: String, featureName: String) {
        routeToFeatureMap[route] = featureName
        Trace.d("Mapped route '$route' to feature '$featureName'")
    }

    /**
     * Mapea múltiples rutas a un mismo feature.
     *
     * @param routes Lista de rutas
     * @param featureName Nombre del feature asociado
     */
    fun mapRoutesToFeature(routes: List<String>, featureName: String) {
        routes.forEach { route ->
            mapRouteToFeature(route, featureName)
        }
    }

    /**
     * Obtiene el nombre del feature asociado a una ruta.
     *
     * @param route Ruta de navegación
     * @return Nombre del feature o null si no está mapeado
     */
    fun getFeatureForRoute(route: String): String? {
        // Búsqueda exacta
        routeToFeatureMap[route]?.let { return it }

        // Búsqueda por prefijo (para rutas con parámetros)
        return routeToFeatureMap.entries
            .firstOrNull { (mappedRoute, _) ->
                route.startsWith(mappedRoute.removeSuffix("/*"))
            }
            ?.value
    }

    /**
     * Registra todas las rutas de navegación en el NavGraphBuilder.
     */
    fun NavGraphBuilder.registerAllNavigationRoutes() {
        features
            .filterIsInstance<NavigableFeature>()  // ← Solo features con navegación
            .forEach { feature ->
                with(feature) {
                    registerNavigation()
                }
            }
    }

    /**
     * Limpia todos los features.
     */
    fun disposeAll() {
        features.forEach { it.dispose() }
        initializedFeatures.clear()
        loadedModules.clear()
    }
}