package cl.baldomeronapoli.kmm.base.presentation

import cl.baldomeronapoli.kmm.base.presentation.model.UserMessage

abstract class ViewState(
    open val showBottomBar: Boolean = false,
    open val userMessage: UserMessage? = null,
)
