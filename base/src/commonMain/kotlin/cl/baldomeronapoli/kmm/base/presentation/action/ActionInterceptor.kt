package cl.baldomeronapoli.kmm.base.presentation.action

import cl.baldomeronapoli.kmm.base.presentation.ViewAction

interface ActionInterceptor<A : ViewAction> {
    suspend fun onIntercept(action: A)
}