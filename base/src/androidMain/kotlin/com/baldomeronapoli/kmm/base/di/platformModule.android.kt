package com.baldomeronapoli.kmm.base.di

import com.baldomeronapoli.kmm.base.domain.providers.UserAgentProvider
import com.baldomeronapoli.kmm.base.domain.providers.UserAgentProviderImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<UserAgentProvider> {
        UserAgentProviderImpl(androidContext())
    }
}