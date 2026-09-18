package cl.baldomeronapoli.base.domain.usecases

interface ParamsValidator<P> {
    fun validate(params: P)
}