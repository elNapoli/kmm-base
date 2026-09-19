package cl.baldomeronapoli.base.presentation.coordinator

import cl.baldomeronapoli.base.presentation.model.UserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class UserMessageCoordinator {
    private val _userMessage = MutableSharedFlow<UserMessage>(replay = 0, extraBufferCapacity = 1)
    val userMessage: SharedFlow<UserMessage> = _userMessage.asSharedFlow()

    fun showMessage(message: UserMessage) {
        _userMessage.tryEmit(message)
    }
}
