package cl.baldomeronapoli.base.di

import cl.baldomeronapoli.base.domain.models.NetworkMonitor
import cl.baldomeronapoli.base.utils.NetworkMonitorImpl
import org.koin.dsl.module

actual fun platformModule() = module {
    single<NetworkMonitor> {
        NetworkMonitorImpl(context = get())
    }
}