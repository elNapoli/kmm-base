package cl.baldomeronapoli.kmm.base.presentation

import cl.baldomeronapoli.kmm.base.presentation.model.ScaffoldUiState
import cl.baldomeronapoli.kmm.base.presentation.model.UiText
import cl.baldomeronapoli.kmm.base.presentation.model.UserMessage

abstract class ViewState(
    open val topBarTitle: UiText = UiText.DynamicString(""),
    open val topBarSubTitle: UiText = UiText.DynamicString(""),
    open val showTopBar: Boolean = false,
    open val showBottomBar: Boolean = false,
    open val showDivider: Boolean = false,
    open val userMessage: UserMessage? = null,
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

