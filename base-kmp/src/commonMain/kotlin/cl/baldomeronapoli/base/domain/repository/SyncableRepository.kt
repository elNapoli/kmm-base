package cl.baldomeronapoli.base.domain.repository

/**
 * Interface for repositories that support automatic synchronization.
 *
 * Implement this interface to make your repository participate in auto-sync
 * when network connectivity is restored (offline → online transition).
 *
 * Example usage:
 * ```
 * class UserHabitRepositoryImpl(...) :
 *     BaseRepository(...),
 *     UserHabitRepository,
 *     SyncableRepository {
 *
 *     override val entityType = "UserHabit"
 *     override val syncPriority = 50
 *
 *     override suspend fun syncToRemote(): Int {
 *         return syncDirtyHabitsToSupabase(userId)
 *     }
 *
 *     override suspend fun getPendingCount(): Int {
 *         return localDataSource.getDirtyRecords().size
 *     }
 * }
 * ```
 *
 * Then register in Koin:
 * ```
 * factoryOf(::UserHabitRepositoryImpl) {
 *     bind<UserHabitRepository>()
 *     bind<SyncableRepository>()  // Auto-discovered by AutoSyncOrchestrator
 * }
 * ```
 */
interface SyncableRepository {

    /**
     * Unique identifier for this entity type.
     * Used for logging and debugging.
     *
     * Example: "UserHabit", "HabitProof", "UserProfile"
     */
    val entityType: String

    /**
     * Priority for sync order. Lower numbers sync first.
     *
     * Default: 100
     *
     * Recommended priorities:
     * - 10: UserProfile (user data should sync first)
     * - 50: UserHabit, HabitHistory (user-generated content)
     * - 100: HabitProof, Media (large files sync last)
     */
    val syncPriority: Int get() = 100

    /**
     * Synchronize dirty/unsynced records to remote server.
     *
     * This method should:
     * 1. Query local database for records with is_dirty=1 or is_synced=false
     * 2. Upload each record to remote (Supabase)
     * 3. Update local records to mark as synced (is_dirty=0, is_synced=true)
     * 4. Handle errors gracefully (failed records stay dirty for retry)
     *
     * @return Number of successfully synced records, or -1 if operation failed entirely
     */
    suspend fun syncToRemote(): Int

    /**
     * Count of pending records waiting to be synced.
     * Used for UI display (badge showing "3 pending items").
     *
     * @return Number of dirty/unsynced records in local database
     */
    suspend fun getPendingCount(): Int
}
