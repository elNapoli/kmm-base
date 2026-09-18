package cl.baldomeronapoli.base.presentation

import cl.baldomeronapoli.base.presentation.model.UserMessage

abstract class ViewState(
    open val showBottomBar: Boolean = false,
    open val userMessage: UserMessage? = null,
)
