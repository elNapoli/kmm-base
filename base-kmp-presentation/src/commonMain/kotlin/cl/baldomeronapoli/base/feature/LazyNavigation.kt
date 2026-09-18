package cl.baldomeronapoli.base.feature

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlin.reflect.KType

/**
 * Wrapper de navigation() que carga los módulos del feature de forma lazy
 * solo en la primera ruta (startDestination).
 *
 * IMPORTANTE: Debes wrappear manualmente el startDestination con LazyFeatureLoader.
 *
 * Uso:
 * ```kotlin
 * lazyNavigation(
 *     featureName = "login",
 *     startDestination = "login_form",
 *     route = "login_flow"
 * ) {
 *     // Primera ruta: wrappear con LazyFeatureLoader
 *     composable("login_form") {
 *         LazyFeatureLoader("login") { LoginFormScreen() }
 *     }
 *     // Las demás sin wrapper
 *     composable("login_otp") { LoginOtpScreen() }
 *     composable("login_success") { LoginSuccessScreen() }
 * }
 * ```
 */
fun NavGraphBuilder.lazyNavigation(
    featureName: String,
    startDestination: String,
    route: String,
    builder: NavGraphBuilder.() -> Unit
) {
    navigation(
        startDestination = startDestination,
        route = route
    ) {
        builder()
    }
}

/**
 * Helper para crear un composable con lazy loading integrado.
 * Usa esto para el startDestination de tu navigation.
 *
 * Uso:
 * ```kotlin
 * navigation(...) {
 *     lazyComposableWithLoader("login", "login_form") {
 *         LoginFormScreen()
 *     }
 *     composable("login_otp") { LoginOtpScreen() }
 * }
 * ```
 */
fun NavGraphBuilder.lazyComposableWithLoader(
    featureName: String,
    route: String,
    arguments: List<androidx.navigation.NamedNavArgument> = emptyList(),
    deepLinks: List<androidx.navigation.NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route, arguments, deepLinks) { backStackEntry ->
        LazyFeatureLoader(featureName = featureName) {
            content(backStackEntry)
        }
    }
}

/**
 * Versión simplificada sin nested navigation.
 * Para features con una sola pantalla o sin navigation graph anidado.
 *
 * Uso:
 * ```kotlin
 * lazyComposable(
 *     featureName = "profile",
 *     route = "profile"
 * ) {
 *     ProfileScreen()
 * }
 * ```
 */
fun NavGraphBuilder.lazyComposable(
    featureName: String,
    route: String,
    arguments: List<androidx.navigation.NamedNavArgument> = emptyList(),
    deepLinks: List<androidx.navigation.NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route, arguments, deepLinks) { backStackEntry ->
        LazyFeatureLoader(featureName = featureName) {
            content(backStackEntry)
        }
    }
}