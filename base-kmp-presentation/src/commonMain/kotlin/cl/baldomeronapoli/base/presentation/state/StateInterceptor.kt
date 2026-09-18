package cl.baldomeronapoli.base.presentation.state

import cl.baldomeronapoli.base.presentation.ViewState

interface StateInterceptor<S : ViewState> {
    suspend fun onIntercept(state: S)
}