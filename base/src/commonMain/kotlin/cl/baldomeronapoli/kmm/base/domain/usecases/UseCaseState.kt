package cl.baldomeronapoli.kmm.base.domain.usecases

sealed class UseCaseState<T, E : UseCaseError> {
    class Loading<T, E : UseCaseError> : UseCaseState<T, E>()
    data class Success<T, E : UseCaseError>(val data: T) : UseCaseState<T, E>()
    data class Error<T, E : UseCaseError>(val error: E?) : UseCaseState<T, E>()
}