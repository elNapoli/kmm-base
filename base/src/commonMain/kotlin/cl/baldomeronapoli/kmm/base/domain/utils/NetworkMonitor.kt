package cl.baldomeronapoli.enracha.core.domain.utils

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
