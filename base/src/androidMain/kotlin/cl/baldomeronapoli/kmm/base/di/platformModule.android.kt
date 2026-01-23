package cl.baldomeronapoli.kmm.base.di

import cl.baldomeronapoli.kmm.base.data.datasource.device.UserAgentDataSource
import cl.baldomeronapoli.kmm.base.data.datasource.device.UserAgentDataSourceImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<UserAgentDataSource> {
        UserAgentDataSourceImpl(
            androidContext()
        )
    }
}