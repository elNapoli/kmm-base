package cl.baldomeronapoli.kmm.base.presentation.action

import cl.baldomeronapoli.kmm.base.presentation.Mutation
import cl.baldomeronapoli.kmm.base.presentation.ViewAction
import cl.baldomeronapoli.kmm.base.presentation.ViewEffect
import cl.baldomeronapoli.kmm.base.presentation.ViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

abstract class ActionProcessor<S : ViewState, A : ViewAction, E : ViewEffect> {
    open fun process(
        action: A,
        sendEffect: (E) -> Unit
    ): Flow<Mutation<S>> = flowOf { state -> state }
}