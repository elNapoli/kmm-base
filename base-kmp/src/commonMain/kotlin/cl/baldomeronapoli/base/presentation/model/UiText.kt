package cl.baldomeronapoli.base.presentation.model

import org.jetbrains.compose.resources.StringResource

sealed class UiText {
    /**
     * Texto estático que no necesita traducción
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Referencia a un recurso de string que será traducido en la UI
     */
    data class StringResourceText(
        val resource: StringResource,
        val formatArgs: Array<out Any> = emptyArray()
    ) : UiText() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as StringResourceText

            if (resource != other.resource) return false
            if (!formatArgs.contentEquals(other.formatArgs)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resource.hashCode()
            result = 31 * result + formatArgs.contentHashCode()
            return result
        }
    }

    companion object {
        /**
         * Helper para crear un UiText desde un String dinámico
         */
        fun from(text: String): UiText = DynamicString(text)

        /**
         * Helper para crear un UiText desde un StringResource
         */
        fun from(resource: StringResource, vararg formatArgs: Any): UiText =
            StringResourceText(resource, formatArgs)
    }
}