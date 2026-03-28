package cl.baldomeronapoli.base.utils

import cl.baldomeronapoli.base.domain.models.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_get_status
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_queue_create

class NetworkMonitorImpl : NetworkMonitor {

    private val _connectivity = MutableStateFlow(true)
    private val monitor = nw_path_monitor_create()
    private val queue = dispatch_queue_create("NetworkMonitor", null)

    init {
        nw_path_monitor_set_update_handler(monitor) { path ->
            val isConnected = nw_path_get_status(path) == nw_path_status_satisfied
            _connectivity.value = isConnected
        }
        nw_path_monitor_set_queue(monitor, queue)
        nw_path_monitor_start(monitor)
    }

    override suspend fun isConnected(): Boolean = _connectivity.value

    override fun observeConnectivity(): Flow<Boolean> = _connectivity
}
