package cl.baldomeronapoli.base.feature

/**
 * Subconjunto de [FeatureManager] sin referencia a los tipos de navegación
 * concretos, para que [LazyFeatureLoader] pueda resolverlo vía Koin sin
 * conocer `NavController`/`NavGraphBuilder`.
 */
interface FeatureLoader {
    fun isFeatureLoaded(featureName: String): Boolean

    fun loadFeatureModules(featureName: String): Boolean

    fun initializeFeature(featureName: String)
}
