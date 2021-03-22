package com.faendir.acra.util

object H2Helper {
    @JvmStatic
    fun subStringIndex(text: String, delim: String, count: Int): String {
        var newDelim = delim
        if (delim.contains(".")) {
            newDelim = delim.replace(".", "\\.")
        }
        val parts = text.split(newDelim.toRegex())
        return if (count > 0) {
            parts.take(count).joinToString(delim)
        } else {
            parts.takeLast(-count).joinToString(delim)
        }
    }
}