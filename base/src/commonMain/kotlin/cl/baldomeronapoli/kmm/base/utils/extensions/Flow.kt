package cl.baldomeronapoli.kmm.base.utils.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import cl.baldomeronapoli.kmm.base.presentation.models.DoNotThrottle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/**
 * Throttles the flow emissions to ensure at least [durationMillis] milliseconds between emissions.
 * Uses kotlinx-datetime for multiplatform time tracking (works on Android and iOS).
 *
 * Classes annotated with @DoNotThrottle will bypass the throttling mechanism.
 */
internal fun <T> Flow<T>.throttle(durationMillis: Long): Flow<T> = flow {
    var lastEmissionTime = 0L

    collect { value ->
        val kClass = value?.let { it::class }

        if (kClass != null && kClass is DoNotThrottle) {
            emit(value)
        } else {
            val currentTime = Clock.System.now().toEpochMilliseconds()
            if (currentTime - lastEmissionTime >= durationMillis) {
                lastEmissionTime = currentTime
                emit(value)
            }
        }
    }
}

/**
 * Colecta un Flow como State con lifecycle awareness
 * Se pausa la colección cuando el lifecycle está en STOPPED
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED
): State<T> {
    return remember(this, lifecycle) {
        flowWithLifecycle(lifecycle, minActiveState)
    }.collectAsState(initial = initialValue)
}

@Composable
fun <T> Flow<T>.CollectAsEffectWithLifecycle(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    action: suspend (T) -> Unit
) {
    LaunchedEffect(this, lifecycleOwner.lifecycle) {
        withContext(Dispatchers.Main.immediate) {
            this@CollectAsEffectWithLifecycle
                .flowWithLifecycle(lifecycleOwner.lifecycle, minActiveState)
                .collect(action)
        }
    }
}