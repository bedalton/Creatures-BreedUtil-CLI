package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.breed.converter.breed.getToGame
import bedalton.creatures.breed.converter.genome.getDefaultGenomeFile
import bedalton.creatures.cli.ConsoleColors
import bedalton.creatures.cli.unescapeCLIPathAndQualify
import bedalton.creatures.common.structs.GameVariant
import bedalton.creatures.common.util.Log
import bedalton.creatures.common.util.nullIfEmpty
import com.bedalton.vfs.*


internal suspend fun readConvertGenome(fs: FileSystem, task: ConvertBreedTask, baseDirectory: String) {
    // Check if we should alter genome
    Log.i {
        "${ConsoleColors.BLACK_BACKGROUND + ConsoleColors.WHITE}Genome files for the target game can be altered with the new breed.\n" +
                "\tNOTE: This does ${ConsoleColors.WHITE + ConsoleColors.BOLD}NOT${ConsoleColors.WHITE} convert a genome, it simply updates one already created for the ${ConsoleColors.WHITE + ConsoleColors.BOLD}target${ConsoleColors.WHITE} game${ConsoleColors.RESET}"
    }
    val convert = yes("${ConsoleColors.BOLD}Would you like to alter a genome?${ConsoleColors.RESET}\n\t- ")
    if (convert) {
        readGenome(fs, true, getDefaultGenomeFile(fs, task.getToGame()), baseDirectory, task::withInputGenome)
        if (task.getInputGenome() == null) {
            return
        }
        readGenome(fs, false, null, baseDirectory, task::withOutputGenome)
        val outputGenomeGenus = readGenus(
            task.getToGame() ?: GameVariant.C3,
            setOf(0, 1, 2, 3),
            "(Optional) Set actual creature genus for genome (not related to appearance)",
            true
        )
        if (outputGenomeGenus != null) {
            task.withOutputGenomeGenus(outputGenomeGenus.second)
        }
    }
}

private suspend fun readGenome(
    fs: FileSystem,
    input: Boolean,
    defaultGenome: Pair<String, String>?,
    baseDirectory: String,
    setGenome: (String) -> Any
) {
    var failures = 0
    var baseMessage =
        "${ConsoleColors.BOLD}Select ${if (input) "input" else "output"} genome file: ${ConsoleColors.RESET}(type or drag ${ConsoleColors.BOLD}.GEN${ConsoleColors.RESET} file into window);"
    if (defaultGenome != null) {
        baseMessage += "${ConsoleColors.BOLD} or hit enter to use the default: GOG Norn Genome: [${defaultGenome.second}]"
    }
    while (true) {
        var temp =
            readLineCancellable("$baseMessage\n\t- ")
                ?.trim()
                ?.unescapePath()
                ?.nullIfEmpty()
                ?: defaultGenome
                    ?.first
        if (temp == null) {
            if (input) {
                if (yes("Cancel converting genome? [default:true]", true)) {
                    Log.i { "..Cancelled converting genome" }
                    return
                }
            }
            if (failures++ > 2) {
                failures *= -1
                Log.i { wishToExit }
            }
            continue
        }
        if (!input && !temp.trim().endsWith(".gen", true)) {
            temp += ".gen"
        }
        val genomeFile = if (input) {
            try {
                listOf(temp).unpackPathsSafe(
                    fs,
                    setOf("gen"),
                    root = baseDirectory
                ).firstOrNull()
            } catch (e: MissingFilesException) {
                Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Failed to locate genome matching: \"$temp\"${ConsoleColors.RESET}" }
                continue
            }
        } else {
            temp
        }

        if (genomeFile == null) {
            Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Failed to locate genome matching: \"$temp\"${ConsoleColors.RESET}" }
            continue
        }
        try {
            setGenome(genomeFile)
        } catch (e: Exception) {
            Log.e {
                ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + (e.message
                    ?: "Failed to locate genome file") + ConsoleColors.RESET
            }
            continue
        }
        break
    }
}
