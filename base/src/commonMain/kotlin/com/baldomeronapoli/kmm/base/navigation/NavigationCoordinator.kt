package com.baldomeronapoli.kmm.base.navigation

import androidx.navigation.NavHostController

/**
 * Coordinador central de navegación.
 * Esta es la interfaz - la implementación está en el módulo navigation.
 */
interface NavigationCoordinator {

    /**
     * Establece el NavController a usar.
     */
    fun setNavController(navController: NavHostController)

    /**
     * Registra un handler de navegación.
     */
    fun registerHandler(handler: NavigationHandler)

    /**
     * Registra múltiples handlers.
     */
    fun registerHandlers(vararg handlers: NavigationHandler)

    /**
     * Navega usando un comando de navegación.
     */
    fun navigate(command: NavigationCommand): Boolean

    /**
     * Obtiene un handler por nombre de feature.
     */
    fun getHandler(featureName: String): NavigationHandler?

    /**
     * Verifica si existe un handler registrado.
     */
    fun hasHandler(featureName: String): Boolean

    /**
     * Limpia todos los handlers y el NavController.
     */
    fun clear()
}