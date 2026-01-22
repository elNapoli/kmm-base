package cl.baldomeronapoli.kmm.base.presentation

import cl.baldomeronapoli.kmm.base.presentation.models.ScaffoldUiState

abstract class ViewState(
    open val topBarTitle: String = "",
    open val topBarSubTitle: String = "",
    open val showTopBar: Boolean = false,
    open val showBottomBar: Boolean = false,
    open val showDivider: Boolean = false,
) {
    /**
     * Extrae solo las propiedades UI necesarias para el MainScreen.
     */
    fun toUiScreenState(): ScaffoldUiState = ScaffoldUiState(
        topBarTitle = topBarTitle,
        topBarSubTitle = topBarSubTitle,
        showDivider = showDivider,
        showTopBar = showTopBar,
        showBottomBar = showBottomBar
    )
}

