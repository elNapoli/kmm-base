package cl.baldomeronapoli.kmm.base.di

import cl.baldomeronapoli.kmm.base.data.datasource.device.UserAgentDataSource
import cl.baldomeronapoli.kmm.base.data.datasource.device.UserAgentDataSourceImpl
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::UserAgentDataSourceImpl) { bind<UserAgentDataSource>() }
}