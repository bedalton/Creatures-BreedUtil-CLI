package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.app.exitNativeWithError
import kotlinx.cli.ArgType

object SizeModArg: ArgType<Map<Int, Double>>(true) {

    private val argsDelimiterRegex by lazy {
        "[,;|]+".toRegex()
    }
    private val equalsRegex by lazy {
        "[=:]+".toRegex()
    }


    private val formatErrorMessage get() = "Size modifier arguments must be in format {age}={scale}; Example: `0=0.5`"
    override val description: kotlin.String
        get() = "Age scale settings {age:int}={scale:Double}; i.e `0=0.7`"

    override fun convert(value: kotlin.String, name: kotlin.String): Map<kotlin.Int, kotlin.Double> {
        val out: MutableMap<kotlin.Int, kotlin.Double> = mutableMapOf()
        val args = value.split(argsDelimiterRegex)
        for (arg in args) {
            val parts =  arg.split(equalsRegex)

            if (parts.size != 2) {
                exitNativeWithError(1, formatErrorMessage)
            }
            val scale = parts[1].toDoubleOrNull()
                ?: exitNativeWithError(1, formatErrorMessage)

            // Fill ages array with wildcard if not already set
            if (parts[0].trim().singleOrNull() == '*') {
                for (age in 0..7) {
                    if (!out.containsKey(age)) {
                        out[age] = scale
                    }
                }
                continue
            }

            val age = parts[0].toIntOrNull()
                ?: exitNativeWithError(1, formatErrorMessage)
            out[age] = scale
        }
        return out
    }
}