package cl.baldomeronapoli.kmm.base.presentation.model

data class ScaffoldUiState(
    val topBarTitle: UiText,
    val topBarSubTitle: UiText,
    val showTopBar: Boolean,
    val showBottomBar: Boolean,
    val showDivider: Boolean,
)