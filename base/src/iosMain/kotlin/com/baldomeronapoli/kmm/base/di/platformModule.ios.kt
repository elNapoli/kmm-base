package com.baldomeronapoli.kmm.base.di

import com.baldomeronapoli.kmm.base.domain.providers.UserAgentProvider
import com.baldomeronapoli.kmm.base.domain.providers.UserAgentProviderImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // iOS-specific UserAgentProvider (no context needed)
    singleOf(::UserAgentProviderImpl) { bind<UserAgentProvider>() }
}