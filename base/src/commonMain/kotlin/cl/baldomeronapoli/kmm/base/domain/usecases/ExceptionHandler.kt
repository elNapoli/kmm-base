package cl.baldomeronapoli.kmm.base.domain.usecases

import cl.baldomeronapoli.kmm.base.domain.repository.LoggingRepository

abstract class ExceptionHandler<E : UseCaseError> {
    protected abstract val loggingRepository: LoggingRepository
    protected open fun parseException(throwable: Throwable): E? = null

    suspend fun loggingException(throwable: Throwable): E? {
        val error = parseException(throwable)
        loggingRepository.logException(throwable)
        return error
    }
}