@file:Suppress("SpellCheckingInspection")

package bedalton.creatures.breed.converter.cli.internal

import com.bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.breed.converter.cli.ConvertBreedSubcommandBase
import com.bedalton.creatures.common.structs.GameVariant
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.common.util.stripSurroundingQuotes
import com.bedalton.log.ConsoleColors
import com.bedalton.log.Log


internal suspend fun readOutputBreedGenus(task: ConvertBreedTask, subcommand: ConvertBreedSubcommandBase) {
    var hits = 0
    // Read in if passed directly to CLI
    subcommand.outputGenus
        ?.trim()
        ?.stripSurroundingQuotes(2, true)
        ?.nullIfEmpty()
        ?.let {
            task.withOutputBreedGenus(it)
            return
        }

    while (true) {
        val temp =
            readLineCancellable("${ConsoleColors.BOLD}Enter output breed genus (the one used for sprites and ATTs)${ConsoleColors.RESET}: [${ConsoleColors.BOLD}N${ConsoleColors.RESET}]orn, [${ConsoleColors.BOLD}G${ConsoleColors.RESET}]rendel, [${ConsoleColors.BOLD}E${ConsoleColors.RESET}]ttin, [${ConsoleColors.BOLD}S${ConsoleColors.RESET}]hee, Geat${ConsoleColors.RESET}")
                ?.trim()
                ?.stripSurroundingQuotes(2, true)
                ?.nullIfEmpty()
                ?: continue
        try {
            task.withOutputBreedGenus(temp)
            return
        } catch (e: Exception) {
            Log.e {
                ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + (e.message
                    ?: "Invalid genus value") + ConsoleColors.RESET
            }
            if (hits++ > 2) {
                hits *= -1
                Log.i { wishToExit }
            }
        }
    }
}

internal suspend fun readOutputBreed(
    task: ConvertBreedTask,
    toGame: GameVariant,
    subcommand: ConvertBreedSubcommandBase
) {
    var hits = 0
    val range = if (toGame == GameVariant.C1) Pair('0', '9') else Pair('a', 'z')
    val character = if (toGame == GameVariant.C1) "digit" else "character"
    subcommand.outputBreed
        ?.trim()
        ?.stripSurroundingQuotes(2, true)
        ?.nullIfEmpty()
        ?.let { breed ->
            try {
                task.withOutputBreed(breed)
                return
            } catch (_: Exception) {
            }
        }
    while (true) {
        val temp =
            readLineCancellable("${ConsoleColors.BOLD}Enter breed $character: ${range.first}-${range.second}${ConsoleColors.RESET}")
                ?.trim()
                ?.stripSurroundingQuotes(2, true)
                ?.nullIfEmpty()
                ?: continue
        try {
            task.withOutputBreed(temp)
            if (temp.length != 1) {
                Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Breed should be a single $character...${ConsoleColors.RESET}" }
            }
            return
        } catch (e: Exception) {
            Log.e {
                ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + (e.message
                    ?: "Invalid breed value. A single character ${range.first}-${range.second} is expected.") + ConsoleColors.RESET
            }
            if (hits++ > 2) {
                hits *= -1
                Log.i { "\t${ConsoleColors.BOLD}Wish to exit? Type \"exit\" to cancel conversion${ConsoleColors.RESET}" }
            }
        }
    }
}