package cl.baldomeronapoli.kmm.base.domain.models

import kotlinx.coroutines.flow.Flow

/**
 * Monitors network connectivity status across platforms.
 */
interface NetworkMonitor {
    /**
     * Checks if the device currently has network connectivity.
     */
    suspend fun isConnected(): Boolean

    /**
     * Observes network connectivity changes over time.
     */
    fun observeConnectivity(): Flow<Boolean>
}