package cl.baldomeronapoli.kmm.base.domain.sync

import kotlinx.coroutines.flow.Flow

/**
 * Sync Manager - Orchestrates synchronization between local and remote storage
 *
 * This interface defines the contract for managing offline-first data synchronization.
 * Implementations should handle:
 * - Coordinating sync operations for different entity types
 * - Tracking sync status and progress
 * - Managing pending operations queue
 * - Handling sync errors and retries
 */
interface SyncManager {
    /**
     * Synchronize entities of a specific type using the provided strategy
     *
     * Process:
     * 1. Check if sync should be performed (via strategy.shouldSync())
     * 2. Fetch remote data (via strategy.fetchRemote())
     * 3. Get local changes (via strategy.getLocalChanges())
     * 4. Resolve conflicts for entities that exist in both
     * 5. Apply merged changes (via strategy.applyChanges())
     * 6. Update sync metadata
     *
     * @param T The domain model type being synced
     * @param entityType Identifier for the entity type (e.g., "UserHabit", "HabitCompletion")
     * @param strategy The sync strategy to use for this entity type
     * @return SyncResult indicating success, error, or skipped
     */
    suspend fun <T> syncEntity(
        entityType: String,
        strategy: SyncStrategy<T>
    ): SyncResult

    /**
     * Observe overall sync status across all entity types
     *
     * @return Flow emitting sync status changes
     */
    fun observeSyncStatus(): Flow<SyncStatus>

    /**
     * Observe sync status for a specific entity type
     *
     * @param entityType The entity type to observe
     * @return Flow emitting sync status changes for this entity
     */
    fun observeEntitySyncStatus(entityType: String): Flow<SyncStatus>

    /**
     * Process pending operations queue
     * Attempts to sync all pending operations that failed previously
     *
     * @return Number of successfully synced operations
     */
    suspend fun processPendingOperations(): Int

    /**
     * Force sync all registered entity types
     * Useful for manual refresh or when app comes online
     */
    suspend fun forceSyncAll()

    /**
     * Get count of pending operations waiting to be synced
     *
     * @return Number of pending operations
     */
    suspend fun getPendingOperationsCount(): Int
}

/**
 * Sync Status enum
 */
enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

/**
 * Sync Result sealed class
 */
sealed class SyncResult {
    data object Success : SyncResult()
    data object Skipped : SyncResult()
    data class Error(val exception: Throwable) : SyncResult()
}
