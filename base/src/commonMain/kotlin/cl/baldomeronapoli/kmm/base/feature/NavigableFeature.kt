package cl.baldomeronapoli.kmm.base.feature

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import cl.baldomeronapoli.kmm.base.navigation.NavigationCoordinator


/**
 * Feature que necesita acceso directo al NavController.
 *
 * Útil para features que necesitan navegar programáticamente
 * o responder a eventos de navegación.
 */
interface NavigableFeature : Feature {

    var navigationCoordinator: NavigationCoordinator?

    /**
     * Registra el grafo de navegación del feature.
     */
    fun NavGraphBuilder.registerNavigation()

    /**
     * Llamado cuando el NavController está listo.
     */
    fun onNavigationReady(navController: NavHostController) {
        // Default: no hace nada
    }
}