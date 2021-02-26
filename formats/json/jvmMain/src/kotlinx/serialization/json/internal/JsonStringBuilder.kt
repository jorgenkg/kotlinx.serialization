package kotlinx.serialization.json.internal

import kotlinx.serialization.internal.*

internal actual class JsonStringBuilder {

    private var array = CharArray(32)
    /*
     * TODO
     */
    private var escapedResult = CharArray(32)

    private var size = 0

    actual fun append(l: Long) {
        // TODO
        append(l.toString())
    }

    actual fun append(ch: Char) {
        ensureAdditionalCapacity(1)
        array[size++] = ch
    }

    actual fun append(s: String) {
        val length = s.length
        ensureAdditionalCapacity(length)
        s.getCharsMpp(0, length, array, size)
        size += length
    }

    actual fun appendQuoted(string: String) {
        ensureAdditionalCapacity(string.length + 2)
        val arr = array
        var sz = size
        arr[sz++] = '"'
        val length = string.length
        string.getCharsMpp(0, length, arr, sz)
        for (i in sz until sz + length) {
            val ch = arr[i].toInt()
            // Do we have unescaped symbols?
            if (ch < ESCAPE_STRINGS.size && ESCAPE_STRINGS[ch] != null) {
                // Go to slow path
                return appendStringSlowPath(i - length, i, string)
            }
        }
        // Update the state
        sz += length
        arr[sz++] = '"'
        size = sz
    }

    private fun appendStringSlowPath(firstEscapedChar: Int, currentSize: Int, string: String) {
        var sz = currentSize
        for (i in firstEscapedChar until string.length) {
            val ch = string[i].toInt()
            // Do we have unescaped symbols?
            if (ch < ESCAPE_STRINGS.size && ESCAPE_STRINGS[ch] != null) {
                val escapedString = ESCAPE_STRINGS[ch]!!
                ensureTotalCapacity(sz + escapedString.length)
                escapedString.getCharsMpp(0, escapedString.length, array, sz)
                sz += escapedString.length
            } else {
                array[sz++] = string[i]
            }
        }
        array[sz++] = '"'
        size = sz
    }

    actual override fun toString(): String {
        return String(array, 0, size).also { size = 0 }
    }

    private fun ensureAdditionalCapacity(expected: Int) {
        ensureTotalCapacity(size + expected)
    }

    private fun ensureTotalCapacity(newSize: Int) {
        if (array.size <= newSize) {
            array = array.copyOf(newSize.coerceAtLeast(size * 2))
        }
    }
}
