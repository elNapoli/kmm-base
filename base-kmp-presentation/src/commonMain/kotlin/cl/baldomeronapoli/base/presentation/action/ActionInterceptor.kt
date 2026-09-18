package cl.baldomeronapoli.base.presentation.action

import cl.baldomeronapoli.base.presentation.ViewAction

interface ActionInterceptor<A : ViewAction> {
    suspend fun onIntercept(action: A)
}