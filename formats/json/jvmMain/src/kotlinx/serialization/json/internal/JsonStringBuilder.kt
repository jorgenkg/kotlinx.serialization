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
        ensureCapacity(1)
        array[size++] = ch
    }

    actual fun append(s: String) {
        val length = s.length
        ensureCapacity(length)
        s.getCharsMpp(0, length, array, size)
        size += length
    }

    actual fun appendQuoted(s: String) {
        ensureCapacity(s.length + 2)
        val arr = array
        var sz = size
        arr[sz++] = '"'
        val length = s.length
        s.getCharsMpp(0, length, arr, sz)
        for (i in sz until sz + length) {
            val ch = arr[i].toInt()
            // Do we have unescaped symbols?
            if (ch < ESCAPE_MARKERS.size && ESCAPE_MARKERS[ch] != 0) {
                val marker = ESCAPE_MARKERS[ch]
                if (marker == 1) {

                } else {
                    // Nope, slow path, let's reprocess string again
                    TODO()
                }
            }
        }

        sz += length
        arr[sz++] = '"'
        size = sz
    }

    actual override fun toString(): String {
        return String(array, 0, size).also { size = 0 }
    }

    private fun ensureCapacity(expected: Int) {
        val newSize = size + expected
        if (array.size <= newSize) {
            array = array.copyOf(newSize.coerceAtLeast(size * 2))
        }
    }
}
