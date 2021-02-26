package kotlinx.serialization.json.internal

internal expect class JsonStringBuilder constructor() {
    fun append(l: Long)
    fun append(ch: Char)
    fun append(s: String)
    fun appendQuoted(string: String)
    override fun toString(): String
}
