/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.json.internal

import kotlinx.serialization.internal.*
import kotlin.native.concurrent.SharedImmutable

private fun toHexChar(i: Int) : Char {
    val d = i and 0xf
    return if (d < 10) (d + '0'.toInt()).toChar()
    else (d - 10 + 'a'.toInt()).toChar()
}

/*
 * Even though the actual size of this array is 92, it has to be the power of two, otherwise
 * JVM cannot perform advanced range-check elimination and vectorization in printQuoted
 */
@SharedImmutable
internal val ESCAPE_STRINGS: Array<String?> = arrayOfNulls<String>(92).apply {
    for (c in 0..0x1f) {
        val c1 = toHexChar(c shr 12)
        val c2 = toHexChar(c shr 8)
        val c3 = toHexChar(c shr 4)
        val c4 = toHexChar(c)
        this[c] = "\\u$c1$c2$c3$c4"
    }
    this['"'.toInt()] = "\\\""
    this['\\'.toInt()] = "\\\\"
    this['\t'.toInt()] = "\\t"
    this['\b'.toInt()] = "\\b"
    this['\n'.toInt()] = "\\n"
    this['\r'.toInt()] = "\\r"
    this[0x0c] = "\\f"
}


@SharedImmutable
public val ESCAPE_MARKERS: BooleanArray = BooleanArray(92).apply {
    // Control chars need generic escape sequence
    for (i in 0..31) {
       set(i, true)
    }
    this['"'.toInt()] = true
    this['\\'.toInt()] = true
    this[0x08] = true
    this[0x09] = true
    this[0x0C] = true
    this[0x0A] = true
    this[0x0D] = true
}

internal fun StringBuilder.printQuoted(value: String) {
    append(STRING)
    var lastPos = 0
    for (i in value.indices) {
        val c = value[i].toInt()
        // Do not replace this constant with C2ESC_MAX (which is smaller than ESCAPE_CHARS size),
        // otherwise JIT won't eliminate range check and won't vectorize this loop
        if (c < ESCAPE_STRINGS.size && ESCAPE_STRINGS[c] != null) {
            append(value, lastPos, i) // flush prev
            append(ESCAPE_STRINGS[c])
            lastPos = i + 1
        }
    }

    if (lastPos != 0) append(value, lastPos, value.length)
    else append(value)
    append(STRING)
}

/**
 * Returns `true` if the contents of this string is equal to the word "true", ignoring case, `false` if content equals "false",
 * and returns `null` otherwise.
 */
internal fun String.toBooleanStrictOrNull(): Boolean? = when {
    this.equals("true", ignoreCase = true) -> true
    this.equals("false", ignoreCase = true) -> false
    else -> null
}
