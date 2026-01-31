package cl.baldomeronapoli.kmm.base.domain.models

import cl.baldomeronapoli.kmm.base.presentation.model.ComposableProvider

interface CustomComposable {
    /**
     * Lambda composable que se renderiza en el TopBar.
     * Null si no hay contenido custom para este estado.
     */
    fun getProvider(): ComposableProvider
}