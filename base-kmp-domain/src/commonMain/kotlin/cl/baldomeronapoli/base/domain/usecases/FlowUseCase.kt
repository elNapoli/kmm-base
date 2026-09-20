package cl.baldomeronapoli.base.domain.usecases

import cl.baldomeronapoli.logger.Trace
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
                // WARN, no ERROR: este catch atrapa tanto errores de negocio
                // esperados (credenciales invalidas, validacion) como fallos
                // tecnicos inesperados. ERROR llega a Crashlytics como
                // non-fatal y con errores de negocio eso satura el dashboard.
                // Un crash real (uncaught exception) lo captura Crashlytics
                // directo, sin pasar por aca.
                Trace.w("Error in FlowUseCase", throwable)
                val error = exceptionHandler?.crash(throwable)
                emit(UseCaseState.Error(error))
            }
    }
}