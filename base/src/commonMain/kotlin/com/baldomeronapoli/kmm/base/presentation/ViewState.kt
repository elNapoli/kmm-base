package com.baldomeronapoli.kmm.base.presentation

import com.baldomeronapoli.kmm.base.presentation.models.ScaffoldUiState

abstract class ViewState(
    open val topBarTitle: String = "",
    open val topBarSubTitle: String = "",
    open val isTopBarShown: Boolean = false,
    open val isBottomBarShown: Boolean = false,
    open val isCloseIconShown: Boolean = false,
    open val isTopBarExpanded: Boolean = false,
) {
    /**
     * Extrae solo las propiedades UI necesarias para el MainScreen.
     */
    fun toUiScreenState(): ScaffoldUiState = ScaffoldUiState(
        topBarTitle = topBarTitle,
        topBarSubTitle = topBarSubTitle,
        isTopBarShown = isTopBarShown,
        isBottomBarShown = isBottomBarShown,
        isCloseIconShown = isCloseIconShown,
        isTopBarExpanded = isTopBarExpanded
    )
}

