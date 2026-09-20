package cl.baldomeronapoli.base.presentation.model

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiText.asString(): String =
    when (this) {
        is UiText.DynamicString -> {
            value
        }

        is UiText.StringResourceText -> {
            if (formatArgs.isEmpty()) {
                stringResource(resource)
            } else {
                stringResource(resource, *formatArgs)
            }
        }
    }

suspend fun UiText.resolve(): String =
    when (this) {
        is UiText.DynamicString -> {
            value
        }

        is UiText.StringResourceText -> {
            if (formatArgs.isEmpty()) {
                getString(resource)
            } else {
                getString(resource, *formatArgs)
            }
        }
    }
