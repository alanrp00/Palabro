package com.example.palabro

/**
 * Normaliza un carácter, eliminando su acento si lo tiene.
 */
fun Char.normalize(): Char {
    return when (this) {
        'Á' -> 'A'
        'É' -> 'E'
        'Í' -> 'I'
        'Ó' -> 'O'
        'Ú' -> 'U'
        else -> this
    }
}

/**
 * Normaliza un String completo, eliminando todos los acentos.
 */
fun String.normalize(): String {
    return this.map { it.normalize() }.joinToString("")
}