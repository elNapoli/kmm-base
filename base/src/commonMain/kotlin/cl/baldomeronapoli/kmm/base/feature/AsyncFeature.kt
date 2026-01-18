package cl.baldomeronapoli.kmm.base.feature

/**
 * Feature que requiere inicialización asíncrona.
 *
 * Ejemplo: Features que necesitan cargar datos, configurar SDKs, etc.
 */
interface AsyncFeature : Feature {

    /**
     * Inicialización asíncrona del feature.
     * Se ejecuta en un coroutine scope de la aplicación.
     *
     * Ejemplo:
     * ```kotlin
     * class MyFeature(
     *     private val configRepository: ConfigRepository // Inyectado por Koin
     * ) : AsyncFeature {
     *
     *     override suspend fun initializeAsync() {
     *         // Cargar configuración desde red
     *         val config = configRepository.fetchConfig()
     *         // Inicializar SDK
     *         AnalyticsSDK.initialize(config)
     *     }
     * }
     * ```
     *
     * NOTA: Si necesitas Context (Android) u otros recursos de plataforma,
     * inyéctalos a través de Koin en el constructor de tu implementación.
     */
    suspend fun initializeAsync()
}