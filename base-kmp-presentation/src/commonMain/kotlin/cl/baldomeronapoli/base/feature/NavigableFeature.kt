package cl.baldomeronapoli.base.feature

import cl.baldomeronapoli.navigation.data.repository.NavigationCoordinator

/**
 * Feature que necesita acceso directo al NavController.
 *
 * Útil para features que necesitan navegar programáticamente
 * o responder a eventos de navegación.
 *
 * [NavController] y [NavGraphBuilder] son los tipos concretos de la
 * plataforma UI (ej. `androidx.navigation.NavHostController` y
 * `androidx.navigation.NavGraphBuilder` en Compose) que exige
 * [NavigationCoordinator], de `napoli-kmm-navigation`.
 */
interface NavigableFeature<NavController, NavGraphBuilder> : Feature {

    var navigationCoordinator: NavigationCoordinator<NavController>?

    /**
     * Registra el grafo de navegación del feature.
     */
    fun NavGraphBuilder.registerNavigation()

    /**
     * Llamado cuando el NavController está listo.
     */
    fun onNavigationReady(navController: NavController) {
        // Default: no hace nada
    }
}
