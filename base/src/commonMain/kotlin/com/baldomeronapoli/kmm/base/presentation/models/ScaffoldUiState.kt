package com.baldomeronapoli.kmm.base.presentation.models

data class ScaffoldUiState(
    val topBarTitle: String,
    val topBarSubTitle: String,
    val isTopBarShown: Boolean,
    val isBottomBarShown: Boolean,
    val isCloseIconShown: Boolean,
    val isTopBarExpanded: Boolean,
)