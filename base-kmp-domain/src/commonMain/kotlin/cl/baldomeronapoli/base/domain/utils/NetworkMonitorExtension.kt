package cl.baldomeronapoli.base.domain.utils

import cl.baldomeronapoli.logger.Trace
import cl.baldomeronapoli.base.domain.models.NetworkMonitor
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
 *
 * Flow:
 * 1. Try local storage
 * 2. If empty → fetch remote → save local → return local
 * 3. If offline & enableSync → log and return empty
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
suspend fun <Remote, Entity, Domain> NetworkMonitor.fetchFirstLocal(
    fetchLocal: suspend () -> List<Entity>,
    fetchRemote: suspend () -> List<Remote>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig()
): List<Domain> {
    val entityName = config.entityType.ifEmpty { "Entity" }

    // Step 1: Try local first
    val localData = fetchLocal()

    if (localData.isNotEmpty()) {
        Trace.d("[$entityName] Found ${localData.size} items in LOCAL storage - returning cached data")
        return localData.map(mapEntityToDomain)
    }

    // Step 2: Local is empty, check network

    if (!this.isConnected()) {
        return emptyList()
    }


    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")

    val entities = remoteData.map(mapRemoteToEntity)

    saveLocal(entities)

    // Return from local to ensure consistency
    val result = fetchLocal().map(mapEntityToDomain)
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
suspend fun <Remote, Entity, Domain> NetworkMonitor.fetchFirstRemoteSaveLocal(
    fetchRemote: suspend () -> List<Remote>,
    fetchLocal: suspend () -> List<Entity>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig()
): List<Domain> {
    val entityName = config.entityType.ifEmpty { "Entity" }


    if (!this.isConnected()) {
        val localData = fetchLocal()
        Trace.d("[$entityName] Retrieved ${localData.size} items from LOCAL fallback")
        return localData.map(mapEntityToDomain)
    }

    val remoteData = fetchRemote()

    val entities = remoteData.map(mapRemoteToEntity)

    saveLocal(entities)
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
suspend fun <Remote, Domain> NetworkMonitor.fetchFirstRemoteNoSave(
    fetchRemote: suspend () -> List<Remote>,
    mapRemoteToDomain: (Remote) -> Domain,
    config: FetchConfig = FetchConfig()
): List<Domain> {
    val entityName = config.entityType.ifEmpty { "Entity" }

    if (!this.isConnected()) {
        return emptyList()
    }
    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} temporary items from REMOTE")

    val result = remoteData.map(mapRemoteToDomain)
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
suspend fun <Remote, Entity> NetworkMonitor.pullRemoteToLocal(
    fetchRemote: suspend () -> List<Remote>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    config: FetchConfig = FetchConfig()
) {
    val entityName = config.entityType.ifEmpty { "Entity" }

    if (!this.isConnected()) {
        return
    }

    val remoteData = fetchRemote()
    Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")
    val entities = remoteData.map(mapRemoteToEntity)
    saveLocal(entities)

}

/**
 * Strategy: Save Local Then Remote
 *
 * Saves data to local storage first (marks as dirty), then attempts to sync to remote.
 * If remote sync fails, data remains marked as dirty for later sync.
 *
 * Flow:
 * 1. Save to local storage (with dirty flag)
 * 2. If online → sync to remote → clear dirty flag
 * 3. If offline or sync fails → keep dirty flag for later retry
 * 4. Return updated local data
 *
 * @param Entity Database entity type
 * @param Domain Domain model type
 *
 * @param saveLocalDirty Lambda to save data to local storage and mark as dirty
 * @param syncToRemote Lambda to sync data to remote source
 * @param clearDirtyFlag Lambda to clear dirty flag after successful sync
 * @param fetchLocal Lambda to fetch updated data from local storage
 * @param mapEntityToDomain Lambda to map entity to domain model
 * @param config Fetch configuration with sync settings
 *
 * @return Domain model after save (may be dirty if sync failed)
 */
suspend fun <Entity, Domain> NetworkMonitor.saveLocalThenRemote(
    saveLocalDirty: suspend () -> Entity,
    syncToRemote: suspend () -> Unit,
    clearDirtyFlag: suspend () -> Unit,
    fetchLocal: suspend () -> Entity?,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig(),
    onNonRetriableError: suspend (Entity, Exception) -> Unit = { _, _ -> }
): Domain {
    val entityName = config.entityType.ifEmpty { "Entity" }

    val savedEntity = saveLocalDirty()
    Trace.d("[$entityName] Saved to LOCAL storage (marked as dirty)")

    if (this.isConnected()) {
        try {
            syncToRemote()
            clearDirtyFlag()
            Trace.d("[$entityName] Synced to REMOTE and cleared dirty flag")
        } catch (e: Exception) {
            Trace.e("[$entityName] Non-retriable REMOTE error - rolling back local", e)
            onNonRetriableError(savedEntity, e)
            throw e  // propaga al UseCase
        }
    } else {
        Trace.d("[$entityName] OFFLINE - data will sync when online")
    }

    val result = fetchLocal() ?: savedEntity
    return mapEntityToDomain(result)
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
fun <Remote, Entity, Domain> NetworkMonitor.observeFirstLocal(
    observeLocal: () -> Flow<List<Entity>>,
    fetchRemote: suspend () -> List<Remote>,
    saveLocal: suspend (List<Entity>) -> Unit,
    mapRemoteToEntity: (Remote) -> Entity,
    mapEntityToDomain: (Entity) -> Domain,
    config: FetchConfig = FetchConfig()
): Flow<List<Domain>> = flow {
    val entityName = config.entityType.ifEmpty { "Entity" }
    // Observe local data
    observeLocal().collect { localData ->
        Trace.d("[$entityName] LOCAL storage emitted ${localData.size} items")
        // Emit current local data
        emit(localData.map(mapEntityToDomain))

        // If local is empty and we have network, fetch remote
        if (localData.isEmpty()) {
            if (this@observeFirstLocal.isConnected()) {
                val remoteData = fetchRemote()
                Trace.d("[$entityName] Received ${remoteData.size} items from REMOTE")

                val entities = remoteData.map(mapRemoteToEntity)
                saveLocal(entities)

            }
        }
    }
}