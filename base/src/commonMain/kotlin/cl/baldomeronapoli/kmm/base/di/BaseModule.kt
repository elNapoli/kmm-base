package cl.baldomeronapoli.kmm.base.di

import org.koin.core.module.Module
import org.koin.dsl.module

object BaseModule {

    fun getModules(): List<Module> {
        return listOf(
            platformModule(),
            coreModule(),
        )
    }

    private fun coreModule() = module {

    }
}