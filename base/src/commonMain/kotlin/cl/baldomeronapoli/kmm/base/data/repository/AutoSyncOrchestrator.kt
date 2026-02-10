package cl.baldomeronapoli.kmm.base.data.repository

import cl.baldomeronapoli.kmm.base.domain.models.NetworkMonitor
import cl.baldomeronapoli.kmm.base.domain.repository.SyncableRepository
import cl.baldomeronapoli.kmm.logger.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Orchestrates automatic synchronization for all registered SyncableRepositories.
 *
 * This class:
 * - Listens to network status changes via NetworkMonitor
 * - Automatically triggers sync when connection is restored (offline → online)
 * - Syncs repositories in priority order (lower priority number = sync first)
 * - Handles partial failures (continues even if one repository fails)
 * - Exposes sync state for UI feedback
 *
 * Usage:
 * The orchestrator is registered as a singleton in Koin and auto-starts in init {}.
 * No manual invocation required - it works automatically!
 *
 * ```
 * // In SyncModule.kt
 * single {
 *     AutoSyncOrchestrator(
 *         networkMonitor = get(),
 *         syncableRepositories = getAll(), // Koin auto-discovers all SyncableRepository
 *         coroutineScope = get()
 *     )
 * }
 * ```
 *
 * For manual sync (e.g., pull-to-refresh):
 * ```
 * viewModelScope.launch {
 *     autoSyncOrchestrator.triggerSync()
 * }
 * ```
 */
class AutoSyncOrchestrator(
    private val networkMonitor: NetworkMonitor,
    private val syncableRepositories: List<SyncableRepository>,
    private val coroutineScope: CoroutineScope
) {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var wasOffline = false
    private var isFirstEmission = true

    init {
        Trace.d("AutoSyncOrchestrator initialized with ${syncableRepositories.size} repositories")
        syncableRepositories.forEach {
            Trace.d("  - ${it.entityType} (priority: ${it.syncPriority})")
        }
        observeNetworkChanges()
    }

    /**
     * Observes network connectivity changes and triggers sync on reconnection.
     * Also triggers sync on app startup if online and there are pending dirty records
     * (covers the case: user closes app offline, reopens with internet).
     */
    private fun observeNetworkChanges() {
        coroutineScope.launch {
            networkMonitor.observeConnectivity()
                .distinctUntilChanged()
                .collect { isConnected ->
                    Trace.d("Network status changed: ${if (isConnected) "ONLINE" else "OFFLINE"}")

                    if (isConnected && wasOffline) {
                        Trace.d("Network restored! Triggering auto-sync...")
                        triggerSync()
                    } else if (isConnected && isFirstEmission) {
                        // App started online — check if there are pending dirty records
                        val pendingCount = getTotalPendingCount()
                        if (pendingCount > 0) {
                            Trace.d("App started online with $pendingCount pending records. Triggering sync...")
                            triggerSync()
                        }
                    }

                    isFirstEmission = false
                    wasOffline = !isConnected
                }
        }
    }

    /**
     * Manually trigger sync for all entities.
     * Can be called from pull-to-refresh or other UI actions.
     *
     * @return SyncResult indicating overall success/failure
     */
    suspend fun triggerSync(): SyncResult {
        if (!networkMonitor.isConnected()) {
            Trace.d("No network connection. Skipping sync.")
            return SyncResult.Skipped
        }

        if (syncableRepositories.isEmpty()) {
            Trace.d("No syncable repositories registered. Skipping sync.")
            return SyncResult.Skipped
        }

        Trace.d("Starting sync for ${syncableRepositories.size} repositories...")
        _syncState.value = SyncState.Syncing(0, syncableRepositories.size)

        val results = mutableMapOf<String, Int>()
        val sortedRepos = syncableRepositories.sortedBy { it.syncPriority }

        sortedRepos.forEachIndexed { index, repo ->
            try {
                Trace.d("Syncing ${repo.entityType} (${index + 1}/${sortedRepos.size})...")

                val count = repo.syncToRemote()
                results[repo.entityType] = count

                _syncState.value = SyncState.Syncing(index + 1, sortedRepos.size)

                if (count > 0) {
                    Trace.d("✓ ${repo.entityType}: synced $count records")
                } else if (count == 0) {
                    Trace.d("○ ${repo.entityType}: no records to sync")
                } else {
                    Trace.e("✗ ${repo.entityType}: sync failed")
                }

            } catch (e: Exception) {
                Trace.e("✗ ${repo.entityType}: exception during sync - ${e.message}", e)
                results[repo.entityType] = -1
            }
        }

        val totalSynced = results.values.filter { it > 0 }.sum()
        val failed = results.values.count { it < 0 }

        val finalState = if (failed == 0) {
            Trace.d("✓ Sync completed successfully! Total synced: $totalSynced records")
            SyncState.Success(totalSynced, results)
        } else {
            Trace.e("⚠ Sync completed with failures. Synced: $totalSynced, Failed: $failed")
            SyncState.PartialFailure(totalSynced, failed, results)
        }

        _syncState.value = finalState

        return if (failed == 0) SyncResult.Success else SyncResult.PartialSuccess
    }

    /**
     * Get total pending count across all entities.
     * Useful for showing "3 items pending sync" badge in UI.
     */
    suspend fun getTotalPendingCount(): Int {
        return syncableRepositories.sumOf { repo ->
            try {
                repo.getPendingCount()
            } catch (e: Exception) {
                Trace.e("Error getting pending count for ${repo.entityType}: ${e.message}")
                0
            }
        }
    }
}

/**
 * Represents the current state of the sync operation
 */
sealed class SyncState {
    /**
     * No sync operation in progress
     */
    object Idle : SyncState()

    /**
     * Sync operation in progress
     * @param current Number of repositories synced so far
     * @param total Total number of repositories to sync
     */
    data class Syncing(val current: Int, val total: Int) : SyncState()

    /**
     * Sync completed successfully with no failures
     * @param totalSynced Total number of records synced across all repositories
     * @param details Map of entity type to number of records synced
     */
    data class Success(
        val totalSynced: Int,
        val details: Map<String, Int>
    ) : SyncState()

    /**
     * Sync completed but some repositories failed
     * @param synced Number of records successfully synced
     * @param failed Number of repositories that failed
     * @param details Map of entity type to sync result (positive = success, -1 = failure)
     */
    data class PartialFailure(
        val synced: Int,
        val failed: Int,
        val details: Map<String, Int>
    ) : SyncState()
}

/**
 * Result of a sync operation
 */
sealed class SyncResult {
    /**
     * All repositories synced successfully
     */
    object Success : SyncResult()

    /**
     * Some repositories synced, some failed
     */
    object PartialSuccess : SyncResult()

    /**
     * Sync was skipped (no network, no repositories, etc.)
     */
    object Skipped : SyncResult()

    /**
     * Sync failed with error
     */
    data class Error(val message: String) : SyncResult()
}
