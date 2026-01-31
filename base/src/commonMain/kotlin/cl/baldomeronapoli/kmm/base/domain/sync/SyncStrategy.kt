package cl.baldomeronapoli.kmm.base.domain.sync

/**
 * Generic sync strategy for syncing entities between local and remote storage
 *
 * Implementations should define how to:
 * - Determine if sync is needed
 * - Fetch remote data
 * - Get local changes that need to be synced
 * - Resolve conflicts between local and remote versions
 * - Apply merged changes to local storage
 *
 * @param T The domain model type being synced
 */
interface SyncStrategy<T> {
    /**
     * Determine if sync should be performed
     * Can be based on time since last sync, network availability, etc.
     *
     * @return true if sync should be performed, false otherwise
     */
    suspend fun shouldSync(): Boolean

    /**
     * Fetch all entities from remote storage
     *
     * @return List of entities from remote
     * @throws Exception if fetch fails (e.g., network error)
     */
    suspend fun fetchRemote(): List<T>

    /**
     * Get local entities that have been modified and need to be synced
     * Typically entities marked as "dirty"
     *
     * @return List of locally modified entities
     */
    suspend fun getLocalChanges(): List<T>

    /**
     * Resolve conflict when same entity exists in both local and remote with different versions
     *
     * Common strategies:
     * - Last write wins (compare timestamps)
     * - Local wins
     * - Remote wins
     * - Custom merge logic
     *
     * @param local The local version of the entity
     * @param remote The remote version of the entity
     * @return The resolved/merged entity
     */
    suspend fun resolveConflict(local: T, remote: T): T

    /**
     * Apply the merged/resolved entities to local storage
     * Should also update sync metadata (isDirty flags, lastSyncedAt, etc.)
     *
     * @param merged List of entities to save locally
     */
    suspend fun applyChanges(merged: List<T>)

    /**
     * Optional: Push local changes to remote
     * Override this if you want to customize the remote push logic
     *
     * @param localChanges Entities to push to remote
     */
    suspend fun pushToRemote(localChanges: List<T>) {
        // Default implementation does nothing
        // Subclasses can override to implement custom push logic
    }
}
