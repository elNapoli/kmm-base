package com.baldomeronapoli.kmm.base.domain.repositories

interface LoggingRepository {
    suspend fun logException(throwable: Throwable)
}
