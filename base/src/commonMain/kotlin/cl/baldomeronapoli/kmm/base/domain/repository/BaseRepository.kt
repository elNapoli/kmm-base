package cl.baldomeronapoli.kmm.base.domain.repository

import cl.baldomeronapoli.enracha.core.domain.utils.NetworkMonitor
import cl.baldomeronapoli.kmm.base.domain.sync.SyncManager
import cl.baldomeronapoli.kmm.logger.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/**
 * Base repository providing common functionality for data operations.
 *
 * This abstract class reduces boilerplate by providing:
 * - Network connectivity checking
 * - Optional sync manager integration
 * - Coroutine context management
 * - Error handling helpers
 *
 * @param networkMonitor Monitors network connectivity status
 * @param syncManager Optional sync manager for offline-first operations
 */
abstract class BaseRepository(
    val networkMonitor: NetworkMonitor,
    val syncManager: SyncManager? = null
) {

    /**
     * Executes a suspend block in IO context with error handling.
     *
     * @param block The suspend operation to execute
     * @return Result of the operation
     */
    protected suspend fun <T> executeIO(block: suspend () -> T): Result<T> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(block())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Checks if the device is currently connected to the network.
     *
     * @return true if connected, false otherwise
     */
    suspend fun isNetworkAvailable(): Boolean =
        networkMonitor.isConnected()

    /**
     * Determines if sync should be used based on network availability.
     * Sync is useful when offline to queue operations for later.
     *
     * @return true if sync should be enabled (offline or explicitly requested)
     */
    protected suspend fun shouldUseSync(): Boolean =
        syncManager != null && !isNetworkAvailable()

    /**
     * Helper to map a list of items using a transform function.
     *
     * @param transform Function to transform each item
     * @return Transformed list
     */
    protected inline fun <T, R> List<T>.mapItems(transform: (T) -> R): List<R> =
        this.map(transform)

    /**
     * Safely executes a block and returns null on exception.
     * Useful for optional operations that shouldn't fail the entire flow.
     *
     * @param block The operation to execute
     * @return Result of block or null if exception occurs
     */
    protected suspend fun <T> safeExecute(block: suspend () -> T): T? =
        try {
            block()
        } catch (e: Exception) {
            Trace.e("Safe execution failed", e)
            null
        }
}
