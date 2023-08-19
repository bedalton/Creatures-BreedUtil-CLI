@file:Suppress("SpellCheckingInspection")

package com.bedalton.creatures.breed.converter.cli

import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.readInt
import com.bedalton.common.util.className
import com.bedalton.creatures.breed.converter.breed.*
import com.bedalton.creatures.breed.converter.cli.internal.*
import com.bedalton.creatures.cli.GameArgType
import com.bedalton.creatures.common.structs.GameVariant
import com.bedalton.creatures.common.structs.isC2e
import com.bedalton.log.ConsoleColors.BLACK
import com.bedalton.log.ConsoleColors.BOLD
import com.bedalton.log.ConsoleColors.RED
import com.bedalton.log.ConsoleColors.RESET
import com.bedalton.log.ConsoleColors.WHITE_BACKGROUND
import com.bedalton.log.Log
import com.bedalton.log.eIf
import com.bedalton.vfs.ERROR_CODE__BAD_INPUT_FILE
import com.bedalton.vfs.ShouldOverwrite
import kotlinx.cli.ArgType
import kotlinx.cli.optional
import kotlinx.cli.vararg
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext


class ConvertBreedAskCli(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
) : ConvertBreedSubcommandBase(ASK_CLI_NAME, "Step by step breed conversion walk-through") {

    private val toGame by argument(
        GameArgType,
        "target-game",
        description = "The target game for conversion"
    ).optional()


    internal val files: List<String> by argument(
        type = ArgType.String,
        fullName = "images",
        description = "Image files or folders"
    ).optional().vararg()

    @Suppress("SpellCheckingInspection")
    override val outputGenus: String? by option(
        GenusArg,
        "genus",
        shortName = "g",
        description = "The output genus: [n]orn, [g]rendel, [e]ttin, [s]hee, geat"
    )

    override val outputBreed: String? by option(
        ArgType.String,
        "breed",
        shortName = "b",
        description = "The output breed slot for these body parts"
    )


    override fun execute() {
        val job = GlobalScope.async(coroutineContext) {
            try {
                run()
            } catch (e: Exception) {
//                if (Log.hasMode("DEBUG")) {
//                    throw e;
//                }
                Log.e { "Failed to run ASK CLI. ${e.className}${e.message?.let { ":$it" } ?: ""}" }
                Log.eIf("DEBUG") {
                    e.stackTraceToString()
                }
                1
            }
        }
        jobs.add(job)
    }

    private suspend fun run(): Int {
        val task = ConvertBreedTask()
        val baseDirectory = getCurrentWorkingDirectory()
            ?: exitNativeWithError(
                ERROR_CODE__BAD_INPUT_FILE,
                "Failed to obtain current working directory"
            )
        val fs = task.getVfs()

        task.withOverwriteExisting(overwriteExisting)
        task.withOverwriteNone(overwriteNone)

        // Explain task
        Log.i {
            "\n${whiteBackgroundBlackText}This program will walk you through your breed conversion\n" +
                    "\t- When asked for files, you may drag them into the command window\n" +
                    "\t- Multiple files should be separated by spaces\n" +
                    "\t- Paths will be relative to: $baseDirectory\n" +
                    "After answering each question press ${BLACK+BOLD}enter$RESET${whiteBackgroundBlackText} on your keyboard to continue\n" +
                    "**Type ${BLACK+BOLD}\"exit\"$RESET${whiteBackgroundBlackText} or ${BLACK+BOLD}\"cancel\"$RESET$whiteBackgroundBlackText at any time to cancel this conversion$RESET\n\n"
        }

        // Target game
        val toGame = toGame ?: readGame(task)

        // Get breed sprite files
        val breedFiles = readBreedFiles(task.getFromGame() ?: GameVariant.C3, fs, task, baseDirectory, files)


        // Determine from game
        val fromGame = inferVariant(fs, breedFiles)
        task.withFromGame(fromGame)

        // Get Output directory
        readOutputDirectory(task, baseDirectory, this)


        // Read output genus breed
        readOutputBreedGenus(task, this)
        readOutputBreed(task, toGame, this)


        // Convert ATTs
        readAttDirectory(fs, task, baseDirectory, files)
        // Convert Genome
        readConvertGenome(fs, task, baseDirectory, this)

        // Progressive Arms
        readProgressiveArms(task, toGame, fromGame, this)
        // Generate Tails
        readGenerateTails(task, toGame, breedFiles, this)
        // Progress ages between C1e and C2e
        readProgressAges(task, toGame, fromGame, this)

        // Should make all sprites the same size
        if (toGame.isC2e) {
            val sameSize =
                this.sameSize || yes("${BOLD}Would you like to make all images within a breed file the same size$RESET")
            task.withSameSize(sameSize)
            if (sameSize) {
                val padding = this.sameSizePadding ?: readInt(
                    "${BOLD}Padding to add around same-size images$RESET; [Default = ${BOLD}0$RESET]",
                    true,
                    null
                )
                ?: 0
                task.withSameSizePadding(padding)
            }
        }

        // Show progress
        val progress = this.progress || yes("${BOLD}Show conversion progress?$RESET", true)
        task.withProgress(progress)


        if (!overwriteNone && !overwriteExisting) {
            val overwrite: Boolean? = yesNullable(
                "${BOLD}Overwrite breed files if they exist?$RESET [nothing = ask]",
                null
            ) {
                true
            }

            if (overwrite == true) {
                task.withOverwriteExisting(true)
            } else if (overwrite == false) {
                task.withOverwriteNone(true)
            }
        }

        task.withShouldOverwriteCallback should@{ _ ->
            if (task.getOverwriteExisting() == true) {
                return@should ShouldOverwrite.ALWAYS
            } else if (task.getOverwriteNone() == true) {
                return@should ShouldOverwrite.NEVER
            }
            val should = yes(
                "${BOLD}Overwrite existing breed files?$RESET",
                true,
            ) {
                false
            }
            if (should) {
                ShouldOverwrite.ALWAYS
            } else {
                ShouldOverwrite.NEVER
            }
        }

        if (noAsync == true) {
            task.withAsync(false)
        }

        // Run and get result code
        val code = convertBreed(task, coroutineContext)
        if (code != 0) {
            Log.e { "$WHITE_BACKGROUND$RED${BOLD}Conversion failed with exit code: ${code}$RESET" }
        }
        return code
    }

    companion object {
        const val ASK_CLI_NAME = "ask"
    }
}






