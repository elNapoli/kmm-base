package cl.baldomeronapoli.base.domain.usecases

import org.jetbrains.compose.resources.StringResource

interface UseCaseError {
    val title: StringResource
    val message: StringResource
}