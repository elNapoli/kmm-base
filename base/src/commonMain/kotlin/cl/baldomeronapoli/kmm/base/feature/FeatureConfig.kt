package cl.baldomeronapoli.kmm.base.feature

/**
 * Datos de configuración de un feature.
 * Útil para features que necesitan configuración externa.
 */
data class FeatureConfig(
    val isEnabled: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)