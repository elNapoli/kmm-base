package cl.baldomeronapoli.kmm.base.presentation.state

import cl.baldomeronapoli.kmm.base.presentation.ViewState

interface StateInterceptor<S : ViewState> {
    suspend fun onIntercept(state: S)
}