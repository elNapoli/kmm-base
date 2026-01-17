package com.baldomeronapoli.kmm.base.navigation

import androidx.navigation.NavHostController

/**
 * Handler que procesa comandos de navegación.
 * Esta es la interfaz - cada feature implementa su propio handler.
 */
interface NavigationHandler {

    /**
     * Nombre del feature que este handler maneja.
     */
    val featureName: String

    /**
     * Procesa un comando de navegación.
     */
    fun handle(command: NavigationCommand, navController: NavHostController): Boolean
}
