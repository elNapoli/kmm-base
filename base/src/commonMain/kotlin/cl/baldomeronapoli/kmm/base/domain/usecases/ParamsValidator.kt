package cl.baldomeronapoli.kmm.base.domain.usecases

interface ParamsValidator<P> {
    fun validate(params: P)
}