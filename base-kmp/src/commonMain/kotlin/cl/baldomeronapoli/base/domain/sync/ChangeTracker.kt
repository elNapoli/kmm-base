package cl.baldomeronapoli.base.domain.sync

/**
 * Change Tracker - Marks entities as dirty/modified for sync
 *
 * Used to track local changes that need to be synced to remote storage.
 * When an entity is modified locally, it should be marked as "dirty" so the sync
 * system knows it needs to be uploaded to the remote server.
 */
interface ChangeTracker {
    /**
     * Mark an entity as dirty (modified locally)
     * This entity will be included in the next sync operation
     *
     * @param entityType Type of entity (e.g., "UserHabit")
     * @param entityId Unique identifier of the entity
     */
    suspend fun markDirty(entityType: String, entityId: String)

    /**
     * Clear dirty flag for an entity (after successful sync)
     *
     * @param entityType Type of entity
     * @param entityId Unique identifier of the entity
     */
    suspend fun clearDirty(entityType: String, entityId: String)

    /**
     * Check if an entity is dirty
     *
     * @param entityType Type of entity
     * @param entityId Unique identifier of the entity
     * @return true if entity has unsync changes, false otherwise
     */
    suspend fun isDirty(entityType: String, entityId: String): Boolean

    /**
     * Get all dirty entities of a specific type
     *
     * @param entityType Type of entity
     * @return List of entity IDs that are dirty
     */
    suspend fun getDirtyEntities(entityType: String): List<String>

    /**
     * Get count of all dirty entities across all types
     *
     * @return Total number of dirty entities
     */
    suspend fun getDirtyCount(): Int
}
