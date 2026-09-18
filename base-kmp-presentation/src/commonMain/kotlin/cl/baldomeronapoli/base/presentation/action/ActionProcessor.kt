package cl.baldomeronapoli.base.presentation.action

import cl.baldomeronapoli.base.presentation.Mutation
import cl.baldomeronapoli.base.presentation.ViewAction
import cl.baldomeronapoli.base.presentation.ViewEffect
import cl.baldomeronapoli.base.presentation.ViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

abstract class ActionProcessor<S : ViewState, A : ViewAction, E : ViewEffect> {
    open fun process(
        action: A,
        sendEffect: (E) -> Unit
    ): Flow<Mutation<S>> = flowOf { state -> state }
}