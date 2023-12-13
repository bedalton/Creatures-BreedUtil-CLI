package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.creature.common.c2.egg.Egg
import com.bedalton.creatures.creature.common.c2.egg.EggFileParser
import com.bedalton.creatures.exports.minimal.ExportRenderData
import com.bedalton.creatures.genetics.genome.Genome
import com.bedalton.creatures.genetics.parser.GenomeParseException
import com.bedalton.creatures.genetics.parser.GenomeParser
import com.bedalton.common.util.PathUtil
import com.bedalton.creatures.exports.minimal.ExportParser
import com.bedalton.io.bytes.ByteStreamReader
import com.bedalton.vfs.*

suspend fun getGenomeFromFile(file: String, genomeIndex: Int = 0): Genome {
    if (!PathUtil.isAbsolute(file)) {
        throw PathNotAbsoluteException(file, "Genome path must be absolute")
    }
    val fs = UnscopedFileSystem()
    if (!fs.fileExists(file)) {
        throw FileNotFoundException(file, "Genome file does not exist")
    }
    val bytes = LocalFileSystem!!.read(file)
    if (bytes.size < 4) {
        throw GenomeParseException("Not enough bytes in file for genome")
    }
    val extension = PathUtil.getExtension(file)?.lowercase()

    return when (extension) {
        "creature", "exp" -> parseExport(bytes, genomeIndex)
        "gen" -> parseGenome(bytes)
        "egg" -> parseC2Egg(bytes, genomeIndex)
        else -> {
            val magic = bytes.copyOfRange(0, 3).decodeToString().lowercase()
            if (magic == "gene" || magic == "gen2" || magic == "gen3") {
                return parseGenome(bytes)
            }
            try {
                parseExport(bytes, genomeIndex)
            } catch (_: Exception) {
                try {
                    parseC2Egg(bytes, genomeIndex)
                } catch (_: Exception) {
                    throw GenomeParseException("Genome file $file does not seem to be a genome, creature or c2 egg file")
                }
            }
        }
    }

}

/**
 * Parse a genome from genome bytes
 */
private suspend fun parseGenome(bytes: ByteArray): Genome {
    return GenomeParser.parseGenome(bytes)
}

/**
 * Parse a genome from genome bytes
 * @param genomeIndex the index of the export to get genome from
 */
private suspend fun parseExport(bytes: ByteArray, genomeIndex: Int): Genome {

    // Get export data
    val data = ExportParser.parseExport(bytes)

    // Ensure has export data in list
    // Pray files can technically have more than one creature in it
    if (data.isEmpty()) {
        throw GenomeParseException("No creatures found in export")
    }

    if (data.size <= genomeIndex) {
        throw GenomeParseException("Genome index $genomeIndex does not exist in exports with size ${data.size}")
    }

    // Return actual genome
    return try {
        data[genomeIndex].genome
    } catch(_: Exception) {
        throw GenomeParseException("Failed to get genome from creature export")
    }
}

/**
 * Parse a genome from genome bytes
 * @param genomeIndex the index of the egg genome slot to get genome from
 */
private suspend fun parseC2Egg(bytes: ByteArray, genomeIndex: Int): Genome {
    if (genomeIndex !in 0..1) {
        throw GenomeParseException("Invalid egg index. Must be value 0..1")
    }
    val egg: Egg = try {
        ByteStreamReader.read(bytes) {
            EggFileParser.parse(this)
        }
    } catch (e: Exception) {
        throw GenomeParseException("Failed to read egg; ${e.formatted()}", e)
    }

    if (egg.size <= genomeIndex) {
        throw GenomeParseException("Genome index $genomeIndex in egg does not exist")
    }
    return egg[genomeIndex]?.genome()
        ?: throw GenomeParseException("Genome index $genomeIndex in egg does not exist")
}
