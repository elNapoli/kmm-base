package cl.baldomeronapoli.kmm.base.di

import cl.baldomeronapoli.kmm.base.domain.providers.UserAgentProvider
import cl.baldomeronapoli.kmm.base.domain.providers.UserAgentProviderImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::UserAgentProviderImpl) { bind<UserAgentProvider>() }
}