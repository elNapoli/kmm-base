package cl.baldomeronapoli.kmm.base.domain.repository.strategy

import cl.baldomeronapoli.kmm.base.domain.repository.BaseRepository
import cl.baldomeronapoli.kmm.logger.Trace
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Configuration for fetch strategies with optional sync support.
 *
 * @param enableSync Whether to use sync manager for offline operations
 * @param entityType Identifier for the entity type (used in sync operations)
 */
data class FetchConfig(
    val enableSync: Boolean = false,
    val entityType: String = ""
)

/**
 * Strategy: First Local
 *
 * Attempts to fetch data from local storage first.
 * If local storage is empty, fetches from remote and saves to local.
 * Integrates with SyncManager when offline (if enableSync = true).
 *
 * Flow:
 * 1. Try local storage
 * 2. If empty → fetch remote → save local → return local
 * 3. If offline & enableSync → queue operation via SyncManager
 *
 * @param Remote Remote data model type
 * @param Entity Database entity type
 * @param Domain Domain model type
 *
 * @param fetchLocal Lambda to fetch data from local storage
 * @param fetchRemote Lambda to fetch data from remote source
 * @param saveLocal Lambda to save data to local storage
 * @param mapRemoteToEntity Lambda to map remote model to entity
 * @param mapEntityToDomain Lambda to map entity to domain model
 * @param config Fetch configuration with sync settings
 *
 * @return List of domain models
 */
suspend fun <Remote, Entity, Domain> BaseRepository.fetchFirstLocal(
    fetchLocal: suspend () -> List<Entity>,
    fetchRemote: suspend () -> List<Remote>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig()
): List<Domain> {
    val entityName = config.entityType.ifEmpty { "Entity" }

    Trace.d("[$entityName] FIRST_LOCAL strategy started")

    // Step 1: Try local first
    Trace.d("[$entityName] Searching in LOCAL storage...")
    val localData = fetchLocal()

    if (localData.isNotEmpty()) {
        Trace.d("[$entityName] Found ${localData.size} items in LOCAL storage - returning cached data")
        return localData.map(mapEntityToDomain)
    }

    // Step 2: Local is empty, check network
    Trace.d("[$entityName] LOCAL storage is EMPTY")

    if (!isNetworkAvailable()) {
        Trace.d("[$entityName] No internet connection available")
        if (config.enableSync && syncManager != null) {
            Trace.d("[$entityName] Sync ENABLED - operation will sync when online")
            // In a real scenario, you might want to queue a sync operation here
            // For now, return empty as data will sync when online
        } else {
            Trace.d("⏸[$entityName] Sync DISABLED - returning empty list")
        }
        return emptyList()
    }

    // Step 3: Fetch from remote and save
    Trace.d("[$entityName] Internet available - fetching from REMOTE...")

    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")

    val entities = remoteData.map(mapRemoteToEntity)

    Trace.d("[$entityName] Saving ${entities.size} items to LOCAL cache...")
    saveLocal(entities)
    Trace.d("[$entityName] Successfully cached in LOCAL storage")

    // Return from local to ensure consistency
    val result = fetchLocal().map(mapEntityToDomain)
    Trace.d("[$entityName] FIRST_LOCAL completed: ${result.size} items returned")
    return result

}

/**
 * Strategy: First Remote, Save Local
 *
 * Always fetches from remote first, then saves to local storage.
 * Returns data from local storage after saving to ensure consistency.
 *
 * Flow:
 * 1. Fetch from remote
 * 2. Save to local
 * 3. Return from local
 *
 * @param Remote Remote data model type
 * @param Entity Database entity type
 * @param Domain Domain model type
 *
 * @param fetchRemote Lambda to fetch data from remote source
 * @param fetchLocal Lambda to fetch data from local storage (after save)
 * @param saveLocal Lambda to save data to local storage
 * @param mapRemoteToEntity Lambda to map remote model to entity
 * @param mapEntityToDomain Lambda to map entity to domain model
 * @param config Fetch configuration (sync not applicable for remote-first)
 *
 * @return List of domain models
 */
suspend fun <Remote, Entity, Domain> BaseRepository.fetchFirstRemoteSaveLocal(
    fetchRemote: suspend () -> List<Remote>,
    fetchLocal: suspend () -> List<Entity>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig()
): List<Domain> {
    val entityName = config.entityType.ifEmpty { "Entity" }

    Trace.d("🔍 [$entityName] FIRST_REMOTE_SAVE_LOCAL strategy started")

    if (!isNetworkAvailable()) {
        Trace.d("[$entityName] No internet connection - FALLBACK to LOCAL cache")
        val localData = fetchLocal()
        Trace.d("[$entityName] Retrieved ${localData.size} items from LOCAL fallback")
        return localData.map(mapEntityToDomain)
    }

    Trace.d("[$entityName] Internet available - fetching from REMOTE (always fresh)...")
    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")

    val entities = remoteData.map(mapRemoteToEntity)

    Trace.d("[$entityName] Updating LOCAL cache with fresh data...")
    saveLocal(entities)
    Trace.d("[$entityName] LOCAL cache updated successfully")

    // Return from local to ensure consistency with saved data
    val result = fetchLocal().map(mapEntityToDomain)
    Trace.d("[$entityName] FIRST_REMOTE_SAVE_LOCAL completed: ${result.size} items returned")
    return result

}

/**
 * Strategy: First Remote, No Save
 *
 * Fetches data directly from remote without caching to local storage.
 * Useful for data that doesn't need persistence (temporary, one-time use).
 *
 * Flow:
 * 1. Fetch from remote
 * 2. Map and return directly
 *
 * @param Remote Remote data model type
 * @param Domain Domain model type
 *
 * @param fetchRemote Lambda to fetch data from remote source
 * @param mapRemoteToDomain Lambda to map remote model directly to domain
 * @param config Fetch configuration (sync not applicable)
 *
 * @return List of domain models
 */
suspend fun <Remote, Domain> BaseRepository.fetchFirstRemoteNoSave(
    fetchRemote: suspend () -> List<Remote>,
    mapRemoteToDomain: (Remote) -> Domain,
    config: FetchConfig = FetchConfig()
): List<Domain> {
    val entityName = config.entityType.ifEmpty { "Entity" }

    Trace.d("[$entityName] FIRST_REMOTE_NO_SAVE strategy started (no cache)")

    if (!isNetworkAvailable()) {
        Trace.d("[$entityName] No internet connection available")
        Trace.d("[$entityName] No LOCAL cache for this strategy - returning empty")
        return emptyList()
    }

    Trace.d("[$entityName] Fetching from REMOTE (temporary data, won't be cached)...")
    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} temporary items from REMOTE")

    val result = remoteData.map(mapRemoteToDomain)
    Trace.d("[$entityName] FIRST_REMOTE_NO_SAVE completed: ${result.size} items returned")
    return result
}

/**
 * Strategy: Sync Remote to Local (No Return)
 *
 * Sincroniza datos desde remoto a local sin retornar nada.
 * Ideal para operaciones de sincronización donde no necesitas el resultado.
 *
 * Flow:
 * 1. Fetch from remote
 * 2. Save to local
 * 3. Done (no return, no fetchLocal needed)
 *
 * @param Remote Remote data model type
 * @param Entity Database entity type
 *
 * @param fetchRemote Lambda to fetch data from remote source
 * @param saveLocal Lambda to save data to local storage
 * @param mapRemoteToEntity Lambda to map remote model to entity
 * @param config Fetch configuration
 */
suspend fun <Remote, Entity> BaseRepository.syncRemoteToLocal(
    fetchRemote: suspend () -> List<Remote>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    config: FetchConfig = FetchConfig()
) {
    val entityName = config.entityType.ifEmpty { "Entity" }

    Trace.d("[$entityName] SYNC_REMOTE_TO_LOCAL started (no return)")

    if (!isNetworkAvailable()) {
        Trace.d("[$entityName] No internet - sync skipped")
        return
    }

    Trace.d("[$entityName] Fetching from REMOTE for sync...")

    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")

    val entities = remoteData.map(mapRemoteToEntity)

    Trace.d("[$entityName] Saving ${entities.size} items to LOCAL...")
    saveLocal(entities)
    Trace.d("[$entityName] SYNC completed successfully - ${entities.size} items cached")

}

/**
 * Flow-based version of First Local strategy.
 * Emits data from local storage and automatically updates when remote data is fetched.
 *
 * @param observeLocal Lambda to observe local data as a Flow
 * @param fetchRemote Lambda to fetch data from remote source
 * @param saveLocal Lambda to save data to local storage
 * @param mapRemoteToEntity Lambda to map remote model to entity
 * @param mapEntityToDomain Lambda to map entity to domain model
 * @param config Fetch configuration with sync settings
 *
 * @return Flow emitting lists of domain models
 */
fun <Remote, Entity, Domain> BaseRepository.observeFirstLocal(
    observeLocal: () -> Flow<List<Entity>>,
    fetchRemote: suspend () -> List<Remote>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig()
): Flow<List<Domain>> = flow {
    val entityName = config.entityType.ifEmpty { "Entity" }

    Trace.d("[$entityName] OBSERVE_FIRST_LOCAL strategy started (reactive)")

    // Observe local data
    observeLocal().collect { localData ->
        Trace.d("[$entityName] LOCAL storage emitted ${localData.size} items")

        // Emit current local data
        emit(localData.map(mapEntityToDomain))

        // If local is empty and we have network, fetch remote
        if (localData.isEmpty()) {
            Trace.d("[$entityName] LOCAL storage is EMPTY - checking network...")
            if (isNetworkAvailable()) {
                Trace.d("[$entityName] Internet available - fetching from REMOTE...")
                val remoteData = fetchRemote()
                Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")

                val entities = remoteData.map(mapRemoteToEntity)
                Trace.d("[$entityName] Saving to LOCAL cache...")
                saveLocal(entities)
                Trace.d("[$entityName] Successfully cached - LOCAL will emit new data")

            } else {
                Trace.d("[$entityName] No internet - waiting for data or connection")
            }
        }
    }
}
