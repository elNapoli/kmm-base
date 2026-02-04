package cl.baldomeronapoli.kmm.base.di

import cl.baldomeronapoli.kmm.base.domain.models.NetworkMonitor
import cl.baldomeronapoli.kmm.base.utils.NetworkMonitorImpl
import org.koin.dsl.module

actual fun platformModule() = module {
    single<NetworkMonitor> {
        NetworkMonitorImpl(context = get())
    }
}