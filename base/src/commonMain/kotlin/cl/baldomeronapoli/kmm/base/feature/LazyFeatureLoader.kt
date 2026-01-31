package cl.baldomeronapoli.kmm.base.feature

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.baldomeronapoli.kmm.logger.Trace
import org.koin.mp.KoinPlatformTools

/**
 * Composable que carga de forma lazy los módulos de un feature antes de renderizar su contenido.
 *
 * Uso:
 * ```kotlin
 * // En tu Feature implementation
 * override fun NavGraphBuilder.registerNavigation() {
 *     composable("login") {
 *         LazyFeatureLoader(featureName = "login") {
 *             LoginScreen()
 *         }
 *     }
 * }
 * ```
 *
 * @param featureName Nombre del feature a cargar
 * @param showLoadingIndicator Si se debe mostrar un indicador de carga (default: true)
 * @param loadingContent Composable personalizado para mostrar mientras carga (opcional)
 * @param onLoadComplete Callback que se ejecuta cuando termina la carga
 * @param onLoadError Callback que se ejecuta si hay un error al cargar
 * @param content Contenido a mostrar una vez cargado el feature
 */
@Composable
fun LazyFeatureLoader(
    featureName: String,
    showLoadingIndicator: Boolean = true,
    loadingContent: (@Composable () -> Unit)? = null,
    onLoadComplete: (() -> Unit)? = null,
    onLoadError: ((Throwable) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val featureManager = remember {
        KoinPlatformTools.defaultContext().get().get<FeatureManager>()
    }

    var loadState by remember(featureName) {
        mutableStateOf<LoadState>(LoadState.Loading)
    }

    LaunchedEffect(featureName) {
        try {
            // Verificar si ya está cargado
            if (featureManager.isFeatureLoaded(featureName)) {
                Trace.d("Feature '$featureName' already loaded")
                loadState = LoadState.Loaded
                onLoadComplete?.invoke()
                return@LaunchedEffect
            }

            Trace.d("LazyFeatureLoader: Loading feature: $featureName")

            // Cargar módulos de Koin del feature
            val modulesLoaded = featureManager.loadFeatureModules(featureName)

            if (modulesLoaded) {
                Trace.d("LazyFeatureLoader: Modules loaded for feature: $featureName")
            }

            // Inicializar el feature (sin Context, usa DI en su lugar)
            featureManager.initializeFeature(featureName)

            Trace.d("LazyFeatureLoader: Feature '$featureName' loaded successfully")
            loadState = LoadState.Loaded
            onLoadComplete?.invoke()

        } catch (e: Exception) {
            Trace.d("LazyFeatureLoader: Error loading feature '$featureName': ${e.message}")
            e.printStackTrace()
            loadState = LoadState.Error(e)
            onLoadError?.invoke(e)
        }
    }

    when (val state = loadState) {
        is LoadState.Loading -> {
            if (showLoadingIndicator) {
                loadingContent?.invoke() ?: DefaultLoadingContent()
            }
        }

        is LoadState.Loaded -> {
            content()
        }

        is LoadState.Error -> {
            // En caso de error, mostrar el contenido de todas formas
            // (los módulos críticos ya deberían estar cargados)
            Trace.d("LazyFeatureLoader WARNING: Showing content despite error for feature: $featureName")
            content()
        }
    }
}

/**
 * Indicador de carga por defecto.
 */
@Composable
private fun DefaultLoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Estados posibles de carga del feature.
 */
private sealed interface LoadState {
    data object Loading : LoadState
    data object Loaded : LoadState
    data class Error(val throwable: Throwable) : LoadState
}