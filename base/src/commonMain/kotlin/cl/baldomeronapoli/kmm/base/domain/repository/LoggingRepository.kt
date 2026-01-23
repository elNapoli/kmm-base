package cl.baldomeronapoli.kmm.base.domain.repository

interface LoggingRepository {
    suspend fun logException(throwable: Throwable)
}
