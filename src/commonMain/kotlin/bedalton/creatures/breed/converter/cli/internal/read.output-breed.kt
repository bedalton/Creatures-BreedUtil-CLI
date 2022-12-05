@file:Suppress("SpellCheckingInspection")

package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.cli.ConsoleColors
import bedalton.creatures.common.structs.GameVariant
import bedalton.creatures.common.util.Log
import bedalton.creatures.common.util.nullIfEmpty
import bedalton.creatures.common.util.stripSurroundingQuotes


internal suspend fun readOutputBreedGenus(task: ConvertBreedTask) {
    var hits = 0
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
            Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + (e.message ?: "Invalid genus value") + ConsoleColors.RESET }
            if (hits++ > 2) {
                hits *= -1
                Log.i { wishToExit }
            }
        }
    }
}

internal suspend fun readOutputBreed(task: ConvertBreedTask, toGame: GameVariant) {
    var hits = 0
    val range = if (toGame == GameVariant.C1) Pair('0', '9') else Pair('a', 'z')
    val character = if (toGame == GameVariant.C1) "digit" else "character"
    while (true) {
        val temp = readLineCancellable("${ConsoleColors.BOLD}Enter breed $character: ${range.first}-${range.second}${ConsoleColors.RESET}")
            ?.trim()
            ?.stripSurroundingQuotes(2, true)
            ?.nullIfEmpty()
            ?: continue
        try {
            task.withOutputBreed(temp)
            if (temp.length != 1) {
                Log.e { ConsoleColors.WHITE_BACKGROUND + ConsoleColors.RED + "Breed should be a single $character...${ConsoleColors.RESET}" }
            }
//            if (temp[0].lowercaseChar() in range.first..range.second) {
//                return
//            } else {
//                Log.e { WHITE_BACKGROUND + RED + "Invalid breed $character...$RESET" }
//            }
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