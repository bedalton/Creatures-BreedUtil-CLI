@file:Suppress("SpellCheckingInspection")

package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.breed.BreedRegexUtil.BREED_SPRITE_FILE_REGEX
import bedalton.creatures.breed.converter.breed.BreedRegexUtil.getBreedSpriteFileRegex
import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.breed.converter.breed.withFiles
import bedalton.creatures.cli.*
import bedalton.creatures.common.structs.*
import bedalton.creatures.common.util.*
import com.bedalton.app.exitNativeWithError
import com.bedalton.cli.unescapeCLIPathAndQualify
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.common.util.stripSurroundingQuotes
import com.bedalton.log.*
import com.bedalton.log.ConsoleColors.BOLD
import com.bedalton.log.ConsoleColors.RED
import com.bedalton.log.ConsoleColors.RESET
import com.bedalton.log.ConsoleColors.WHITE_BACKGROUND
import com.bedalton.log.ConsoleColors.YELLOW
import com.bedalton.vfs.*


/**
 * Read and filter breed sprite files
 */
internal suspend fun readBreedFiles(fromVariant: GameVariant, fs: FileSystem, task: ConvertBreedTask, baseDirectory: String): List<String> {

    val spriteFiles: List<String> = readRawFiles(fs, task, baseDirectory)

    // Create options object
    val breedOptions = fileBreedOptions(spriteFiles)

    // Ask about breed to convert
    Log.i { "${BOLD}Which breed would you like to convert?$RESET" }

    // Genus
    val possibleGenera = breedOptions
        .map { it.second.first }
        .toSet()
    var genus = readGenus(fromVariant, possibleGenera, "Select input breed genus", false)
    while (genus == null) {
        Log.e { "Input breed genus is blank or invalid" }
        genus = readGenus(fromVariant, possibleGenera, "Select input breed genus", false)
    }
    task.withInputBreedGenus(genus.second)

    val selectedGenusInt = genus.first
    // Filter available sprite breeds by selected genus
    val possibleBreeds = breedOptions
        .filter { it.second.first == selectedGenusInt }
    val breed = readInputBreed(task, genus.second,  possibleBreeds.map { it.third }.toSet())
    val regex = getBreedSpriteFileRegex(
        Pair(genus.first.digitToChar(), (genus.first + 4).digitToChar()),
        breed,
        setOf("spr", "s16", "c16")
    )

    // Filter available sprites by genus and breed char
    return spriteFiles
        .filter { regex.matches(PathUtil.getFileNameWithoutExtension(it.trim()) ?: "") }
}

/**
 * Ask for sprite files
 */
private suspend fun readRawFiles(fs: FileSystem, task: ConvertBreedTask, baseDirectory: String): List<String> {
    // Try and get sprite files
    val spriteFiles: List<String>
    while (true) {
        val filesString =
            readLineCancellable(BOLD + "Input files: $RESET\n\t(type or drag the folder into window where your ${BOLD}input$RESET sprite files are located, then press enter)\n\t- ")
                ?: ""
        val rawFileStrings = splitMultiFileString(filesString)
        spriteFiles = try {
            rawFileStrings
                .mapNotNull {
                    unescapeCLIPathAndQualify(it, baseDirectory)
                }
                .unpackPathsSafe(fs, setOf("spr", "s16", "c16"), BREED_SPRITE_FILE_REGEX)
                .nullIfEmpty()
                ?: throw MissingFilesException(rawFileStrings, emptyList())
        } catch (e: MissingFilesException) {
            Log.e { WHITE_BACKGROUND + RED + (e.message ?: "No sprite files found in $filesString") + RESET }
            continue
        }
        break
    }
    if (spriteFiles.any { !PathUtil.isAbsolute(it) }) {
        exitNativeWithError(ERROR_CODE__BAD_INPUT_FILE) {
            "Sprite paths must be absolute"
        }
    }
    task.withFiles(spriteFiles)
    return spriteFiles
}



internal suspend fun readGenus(
    variant: GameVariant,
    possibleGenera: Set<Int>,
    message: String,
    optional: Boolean = false
): Pair<Int, String>? {
    var genusInt: Int?
    var genus: String?
    if (possibleGenera.size == 1) {
        genusInt = possibleGenera.first()
        genus = when (genusInt) {
            0 -> "norn"
            1 -> "grendel"
            2 -> "ettin"
            3 -> "geat"
            else -> {
                exitNativeWithError(1) { "Invalid genus int found in files $genusInt" }
            }
        }
        Log.i { "${YELLOW + BOLD}Only 1 genus found in images... Using '${genus}'...${RESET}" }
    } else {
        val prompt = "$message: " + possibleGenera
            .sorted()
            .mapNotNull {
                when (it) {
                    0 -> "[${BOLD}N${RESET}]orn"
                    1 -> "[${BOLD}G${RESET}]rendel"
                    2 -> "[${BOLD}E${RESET}]ttin"
                    3 -> "[${BOLD}S${RESET}]hee, Geat"
                    else -> null
                }
            }
            .joinToString(", ") +
                (if (optional) {
                    "(default none)"
                } else {
                    ""
                })
        var hits = 0
        while (true) {
            genus = readLineCancellable(prompt)
                ?.trim()
                ?.stripSurroundingQuotes(2)
                ?.nullIfEmpty()
                ?: (
                        if (optional) {
                            return null
                        } else {
                            Log.e { WHITE_BACKGROUND + RED + "Genus cannot be blank...$RESET" }
                            null
                        }
                    )
            genusInt = try {
                genus?.let { getGenusInt(genus) }
            } catch (e: Exception) {
                Log.e { WHITE_BACKGROUND + RED + "Failed to understand genus...$RESET" }
                null
            }
            if (genusInt == null) {
                if (hits++ > 2) {
                    hits *= -1
                    Log.i { "\t${BOLD}Wish to exit? Type \"exit\" to cancel conversion${RESET}" }
                }
                continue
            }
            if (genusInt !in possibleGenera) {
                Log.e { WHITE_BACKGROUND + RED + "There are no breed sprites matching that genus$RESET" }
                continue
            }
            break
        }
    }
    return Pair(genusInt!!, getGenusString(genusInt, variant) ?: genus!!)
}

internal suspend fun readInputBreed(task: ConvertBreedTask, genus: String, possibleBreedChars: Set<Char>): Char {
    if (possibleBreedChars.size == 1) {
        Log.i { "${YELLOW + ConsoleColors.BOLD}Only 1 $genus breed found in breed files... Using '${possibleBreedChars.first()}'...${RESET}" }
        task.withInputBreed(possibleBreedChars.first().toString())
        return possibleBreedChars.first().uppercaseChar()
    }
    var hits = 0
    while (true) {
        val breedString = readLineCancellable("${BOLD}Enter input ${task.getInputBreedGenus()} breed slot?$RESET\n\t- ")
            ?.trim()
            ?.stripSurroundingQuotes(2)
            ?.nullIfEmpty()
            ?: continue
        if (breedString.length != 1) {
            Log.e { WHITE_BACKGROUND + RED + "Input breed should be a single character$RESET" }
            if (hits++ > 2) {
                hits *= -1
                Log.i { wishToExit }
            }
            continue
        }
        if (breedString[0].uppercaseChar() !in possibleBreedChars) {
            Log.e { WHITE_BACKGROUND + RED + "No files found that match ${task.getInputBreed()} ${breedString.uppercase()}$RESET" }
        }
        task.withInputBreed(breedString)
        return breedString[0].uppercaseChar()
    }
}



private fun intToGenusString(genus: Int, geat: String = "Geat"): String? {
    return when (genus) {
        0, 4 -> "Norn"
        1, 5 -> "Grendel"
        2, 6 -> "Ettin"
        3, 7 -> geat
        else -> null
    }
}



/**
 * Splits a file into a triple of Genus/GenderOffset/Breed
 * I.e. Triple(1, Pair(0,4), a) => Norn (1), GenderOffset(0,4), Breed)
 */
private fun fileBreedOptions(spriteFiles: List<String>): List<Triple<String, Pair<Int, Int>, Char>> {
    val fileNameWithoutExtension = spriteFiles
        .mapNotNull(PathUtil::getFileNameWithoutExtension)
        .toSet()
    return fileNameWithoutExtension.mapNotNull map@{
        var genus = it[1].digitToIntOrNull()
            ?: return@map null
        if (genus >= 4) {
            genus -= 4
        }
        if (genus !in 0..3) {
            null
        } else {
            val breed = it[3].uppercaseChar()
            Triple(intToGenusString(genus) + ' ' + breed, Pair(genus, genus + 4), breed)
        }
    }
        .distinctBy { it.first }
        .sortedBy { "${it.second.second}${it.third}" }
}
