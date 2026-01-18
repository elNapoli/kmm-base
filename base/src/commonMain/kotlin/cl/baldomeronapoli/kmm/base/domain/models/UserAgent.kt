package cl.baldomeronapoli.kmm.base.domain.models

class UserAgent private constructor(
    private val value: HashMap<String, String>
) {

    override fun toString(): String {
        return value.entries.joinToString(separator = ";") { (name, value) ->
            "$name=$value"
        }
    }

    class Builder {
        private val value: HashMap<String, String> = hashMapOf()

        fun addValue(name: String, value: String) = apply {
            this.value[name] = value
        }

        fun build(): UserAgent {
            return UserAgent(value)
        }
    }
}