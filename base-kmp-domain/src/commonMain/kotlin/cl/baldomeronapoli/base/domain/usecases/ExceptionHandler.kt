package cl.baldomeronapoli.base.domain.usecases

import cl.baldomeronapoli.logger.domain.repository.LoggingRepository


abstract class ExceptionHandler<E : UseCaseError> {
    protected abstract val loggingRepository: LoggingRepository
    protected open fun parseException(throwable: Throwable): E? = null

    suspend fun crash(throwable: Throwable): E? {
        val error = parseException(throwable)
        loggingRepository.crash(throwable)
        return error
    }
}