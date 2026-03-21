package cl.baldomeronapoli.base.di

import cl.baldomeronapoli.base.data.repository.AutoSyncOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


object BaseModule {

    fun getModules(): List<Module> {
        return listOf(
            commonModule(),
            platformModule(),
        )
    }

    private fun commonModule() = module {
        // Application-level CoroutineScope with qualifier for global use
        single(named("applicationScope")) {
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        // AutoSyncOrchestrator uses the applicationScope
        single(createdAtStart = true) {
            AutoSyncOrchestrator(
                networkMonitor = get(),
                syncableRepositories = getAll(),
                coroutineScope = get(named("applicationScope"))
            )
        }
    }
}
