package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.genetics.gene.Gene
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("GenePrintResults")
internal data class PrintResults(
    val version: Int,
    val genes: List<Gene>,
    val colors: Map<String, List<ColorInfo>>?
)

@SerialName("GenomeColors")
@Serializable
internal data class ColorInfo(
    val red: Int,
    val green: Int,
    val blue: Int,
    val swap: Int,
    val rotate: Int,
    val gender: Int,
    val ages: List<Int>
)