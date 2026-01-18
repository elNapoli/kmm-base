package cl.baldomeronapoli.kmm.base.presentation.action

import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString as getStringResource

/**
 * Multiplatform resource resolver for accessing string and plural resources
 * outside of Composable functions (e.g., in ViewModels, UseCases).
 *
 * Note: All functions are suspend since Jetbrains Compose Resources uses
 * coroutines for resource loading. Call from a CoroutineScope in your ViewModel.
 *
 * Example usage in ViewModel:
 * ```
 * viewModelScope.launch {
 *     val message = resourceResolver.getString(Res.string.app_name)
 *     val itemsText = resourceResolver.getQuantityString(Res.plurals.items, count, count)
 * }
 * ```
 */
class ResourceResolver {
    /**
     * Get a string resource with optional format arguments.
     *
     * @param resource The StringResource from the generated Res object
     * @param formatArgs Optional format arguments for string formatting
     * @return The resolved string
     */
    suspend fun getString(resource: StringResource, vararg formatArgs: Any): String {
        return if (formatArgs.isEmpty()) {
            getStringResource(resource)
        } else {
            getStringResource(resource, *formatArgs)
        }
    }

    /**
     * Get a plural string resource with quantity and optional format arguments.
     *
     * @param resource The PluralStringResource from the generated Res object
     * @param quantity The quantity to determine which plural form to use
     * @param formatArgs Optional format arguments for string formatting
     * @return The resolved plural string
     */
    suspend fun getQuantityString(
        resource: PluralStringResource,
        quantity: Int,
        vararg formatArgs: Any
    ): String {
        return if (formatArgs.isEmpty()) {
            getPluralString(resource, quantity)
        } else {
            getPluralString(resource, quantity, *formatArgs)
        }
    }
}