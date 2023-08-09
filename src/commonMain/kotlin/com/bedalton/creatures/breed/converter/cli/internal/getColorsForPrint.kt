package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.genetics.genome.Genome
import com.bedalton.creatures.breed.render.renderer.getColorTransform

private val genders
    get() = listOf(
        Pair("male", 1),
        Pair("female", 2)
    )

internal fun getColorsForPrint(genome: Genome, geneVariant: Int?): Map<String, List<ColorInfo>> {
    return genders.associate { (genderString, genderInt) ->
        genderString.lowercase() to getColorsForGender(genome, genderInt, geneVariant)
    }
}


private fun getColorsForGender(genome: Genome, gender: Int, geneVariant: Int?): List<ColorInfo> {
    val colorsRaw = (0..7).associateWith { age ->
        genome.getColorTransform(gender, age, geneVariant = geneVariant)
    }
    val uniqueColorTransforms = colorsRaw.values.distinct()
    return uniqueColorTransforms.map { thePalette ->
        val ages = colorsRaw.entries.mapNotNull { (age, thisPalette) ->
            if (thePalette == thisPalette) {
                age
            } else {
                null
            }
        }
        ColorInfo(
            red = thePalette.red ?: 128,
            green = thePalette.green ?: 128,
            blue = thePalette.blue ?: 128,
            swap = thePalette.swap ?: 128,
            rotate = thePalette.rotation ?: 128,
            gender = gender,
            ages = ages,
        )
    }
}