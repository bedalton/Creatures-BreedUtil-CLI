package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.common.util.getGenusInt
import com.bedalton.log.LOG_DEBUG
import com.bedalton.log.Log
import com.bedalton.log.iIf

fun getBreedGenusPair(value: String?, name: String): Pair<String?, Char>? {
    if (value == null) {
        return null
    }
    if (value.trim().length == 1) {
        val breed = value.trim().single().lowercaseChar()
        return Pair(null, breed)
    }
    val parts = value
        .lowercase()
        .split("[:\\-|]".toRegex())

    if (parts.size != 2) {
        val partsText = if (Log.hasMode(LOG_DEBUG)) {
            "Parts: " + parts.joinToString(",") { it }
        } else {
            ": $value"
        }
        throw Exception("Invalid $name {breed and genus} parameter. Expected {genus}:{breed}; i.e. norn:w; Found $partsText")
    }
    val genusString = parts[0].trim()
    try {
        getGenusInt(genusString)
    } catch (_: Exception) {
        throw Exception("Invalid $name {genus} parameter. Expected {genus}:{breed}; i.e. norn:w; Found: $value")
    }

    if (parts[1].trim().length != 1) {
        throw Exception("Invalid $name {breed} parameter. Expected {genus}:{breed}; i.e. norn:w; Found: $value")
    }
    val breed = parts[1].trim().single()
    return Pair(genusString, breed)
}