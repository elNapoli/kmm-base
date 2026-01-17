package com.baldomeronapoli.kmm.base.presentation.state

import com.baldomeronapoli.kmm.base.presentation.ViewState

interface StateInterceptor<S : ViewState> {
    suspend fun onIntercept(state: S)
}