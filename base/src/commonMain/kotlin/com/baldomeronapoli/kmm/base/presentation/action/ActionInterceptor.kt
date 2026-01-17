package com.baldomeronapoli.kmm.base.presentation.action

import com.baldomeronapoli.kmm.base.presentation.ViewAction

interface ActionInterceptor<A : ViewAction> {
    suspend fun onIntercept(action: A)
}