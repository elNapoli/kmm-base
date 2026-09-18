package cl.baldomeronapoli.base.domain.usecases

/**
 * Marker interface para los errores de dominio de un [FlowUseCase]. Sin
 * campos: el mapeo de un error a lo que la UI muestra (titulo, mensaje,
 * etc.) es responsabilidad de la capa de presentacion, no de domain.
 */
interface UseCaseError
