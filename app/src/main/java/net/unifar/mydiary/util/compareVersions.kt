package net.unifar.mydiary.util

fun compareVersions(current: String, minimum: String): Int {
    val currentParts = current.split(".")
    val minimumParts = minimum.split(".")

    for (i in 0 until maxOf(currentParts.size, minimumParts.size)) {
        val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
        val m = minimumParts.getOrNull(i)?.toIntOrNull() ?: 0
        if (c != m) return c - m
    }
    return 0
}
