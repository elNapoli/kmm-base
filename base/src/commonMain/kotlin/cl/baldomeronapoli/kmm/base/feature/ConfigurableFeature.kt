package cl.baldomeronapoli.kmm.base.feature


/**
 * Feature con configuración dinámica.
 */
interface ConfigurableFeature : Feature {
    /**
     * Configura el feature con parámetros externos.
     * Se llama antes de initialize().
     */
    fun configure(config: FeatureConfig)
}