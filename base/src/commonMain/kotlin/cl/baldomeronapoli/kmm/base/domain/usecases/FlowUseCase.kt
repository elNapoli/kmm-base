package cl.baldomeronapoli.kmm.base.domain.usecases

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

abstract class FlowUseCase<PARAM, RESULT, ERROR : UseCaseError>(
    protected open val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    protected open val paramsValidator: ParamsValidator<PARAM>? = null
    protected open val exceptionHandler: ExceptionHandler<ERROR>? = null
    abstract suspend fun executeOnBackground(params: PARAM): Flow<RESULT>

    fun execute(params: PARAM): Flow<UseCaseState<RESULT, ERROR>> {
        return flow {
            try {
                emitAll(executeOnBackground(params))
            } catch (e: Exception) {
                throw e
            }
        }.flowOn(coroutineDispatcher)
            .map { data ->
                UseCaseState.Success<RESULT, ERROR>(data) as UseCaseState<RESULT, ERROR>
            }
            .onStart {
                paramsValidator?.validate(params)
                emit(UseCaseState.Loading())
            }
            .catch { throwable ->
                val error = exceptionHandler?.loggingException(throwable)
                emit(UseCaseState.Error(error))
            }
    }
}