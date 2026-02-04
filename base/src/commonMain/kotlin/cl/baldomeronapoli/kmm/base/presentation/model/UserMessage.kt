package cl.baldomeronapoli.kmm.base.presentation.model

data class UserMessage(
    val title: UiText,
    val message: UiText,
    val actionLabel: UiText? = null,
    val type: MessageType,
    val isSnackBar: Boolean = false,
    val onActionClick: (() -> Unit)? = null
)