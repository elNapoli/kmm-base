package cl.baldomeronapoli.kmm.base.presentation.model

data class UserMessage(
    val text: UiText,
    val message: UiText,
    val type: MessageType
)