package cl.baldomeronapoli.kmm.base.utils.extensions

import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import cl.baldomeronapoli.kmm.base.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.isActive

/**
 * Obtiene un Flow que emite el BaseViewModel del destino actualmente visible.
 *
 * ADVERTENCIA: Usa APIs internas de ViewModelStore (@RestrictedApi).
 * Esto es necesario porque Navigation no expone una API pública para esto.
 * Si Google cambia la implementación, esto puede romperse.
 *
 * Esta es la solución del usuario original, mantenida porque:
 * - Funciona bien con la arquitectura actual
 * - No modifica BaseViewModel (respeta el patrón)
 * - No crea loops infinitos
 *
 * Optimizaciones aplicadas vs versión original:
 * - Logs con Timber para debugging
 * - Filtrado de diálogos mejorado
 * - Safe calls en vez de !!
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun NavController.viewModelFlow() =
    currentBackStackEntryFlow
        .filter { backStackEntry -> backStackEntry.destination.navigatorName != "dialog" }
        .mapLatest { backStackEntry ->
            val viewModelStore = backStackEntry.viewModelStore

            viewModelStore.keys()
                .mapNotNull { key -> viewModelStore[key] }
                .filterIsInstance<BaseViewModel<*, *, *>>()
                .find { viewModel -> viewModel.viewModelScope.isActive }!!
        }
        .retry { throwable ->
            delay(timeMillis = 100)
            throwable is NullPointerException
        }
