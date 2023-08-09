package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.app.exitNativeWithError
import kotlinx.cli.ArgType

object MapOptionArg: ArgType<Pair<String, String>>(true) {
    override val description: kotlin.String
        get() = ""

    private val equalsRegex = "[=:]+".toRegex()

    override fun convert(value: kotlin.String, name: kotlin.String): Pair<kotlin.String, kotlin.String> {
        val parts = value.split(equalsRegex)
        if (parts.count() != 2) {
            exitNativeWithError(1, "invalid `--option` value <$value>; Expected {key}:{value}")
        }
        return parts[0] to parts[1]
    }

}