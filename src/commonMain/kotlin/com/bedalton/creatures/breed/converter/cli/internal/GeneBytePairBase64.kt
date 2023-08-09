package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.genetics.genome.GeneBytePair
import com.bedalton.io.bytes.toBase64
import kotlinx.serialization.Serializable

@Serializable
data class GeneBytePairs(
    val geneBytePairs: List<GeneBytePair>
)

data class GeneBytePairBase64(
    val type: Int,
    val subtype: Int,
    val indexOfType: Int,
    val headerBytes: String,
    val geneBytes: String,
) {
    constructor(geneBytePair: GeneBytePair) : this(
        geneBytePair.geneType,
        geneBytePair.geneSubtype,
        geneBytePair.indexOfType,
        geneBytePair.headerBytes.toBase64(),
        geneBytePair.geneBytes.toBase64()
    )
}
