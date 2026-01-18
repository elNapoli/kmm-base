package cl.baldomeronapoli.kmm.base.di

import cl.baldomeronapoli.kmm.base.presentation.action.ResourceResolver
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

object BaseModule {

    fun getModules(): List<Module> {
        return listOf(
            platformModule(),
            coreModule(),
        )
    }

    private fun coreModule() = module {
        singleOf(::ResourceResolver)
    }
}