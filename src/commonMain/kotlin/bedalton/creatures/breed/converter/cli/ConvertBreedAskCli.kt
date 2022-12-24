@file:Suppress("SpellCheckingInspection")

package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.breed.*
import bedalton.creatures.breed.converter.cli.internal.*
import bedalton.creatures.cli.*
import bedalton.creatures.cli.ConsoleColors.BLACK
import bedalton.creatures.cli.ConsoleColors.BOLD
import bedalton.creatures.cli.ConsoleColors.RED
import bedalton.creatures.cli.ConsoleColors.RESET
import bedalton.creatures.cli.ConsoleColors.WHITE_BACKGROUND
import bedalton.creatures.common.structs.GameVariant
import bedalton.creatures.common.structs.isC2e
import bedalton.creatures.common.util.*
import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.vfs.*
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

const val ASK_CLI_NAME = "ask"

class ConvertBreedAskCli(private val coroutineContext: CoroutineContext, private val jobs: MutableList<Deferred<Int>>) :
    Subcommand(ASK_CLI_NAME, "Step by step breed conversion walk-through") {


    private val overwriteExisting by option(
        Flag,
        "force",
        shortName = "f",
        description = "Force overwrite of existing files",
    ).default(false)

    private val overwriteNone by option(
        Flag,
        "skip-existing",
        shortName = "x",
        description = "Skip existing files"
    ).default(false)

    private val noAsync by option(
        Flag,
        "no-async",
        shortName = null,
        description = "Do not convert sprites asynchronously"

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
            "\n${WHITE_BACKGROUND}${BLACK}This program will walk you through your breed conversion\n" +
                    "\t- When asked for files, you may drag them into the command window\n" +
                    "\t- Multiple files should be separated by spaces\n" +
                    "\t- Paths will be relative to: $baseDirectory\n" +
                    "After answering each question press ${BLACK + BOLD}enter$BLACK on your keyboard to continue\n" +
                    "**Type $BLACK$BOLD\"exit\"$BLACK or $BLACK$BOLD\"cancel\"$BLACK at any time to cancel this conversion$RESET\n\n"
        }

        // Target game
        val toGame = readGame(task)

        // Get breed sprite files
        val breedFiles = readBreedFiles(task.getFromGame() ?: GameVariant.C3, fs, task, baseDirectory)
        

        // Determine from game
        val fromGame = inferVariant(fs, breedFiles)
        task.withFromGame(fromGame)

        // Get Output directory
        readOutputDirectory(task, baseDirectory)


        // Read output genus breed
        readOutputBreedGenus(task)
        readOutputBreed(task, toGame)

        // Convert ATTs
        readAttDirectory(fs, task, baseDirectory)
        // Convert Genome
        readConvertGenome(fs, task, baseDirectory)
        // Progressive Arms
        readProgressiveArms(task, toGame, fromGame)
        // Generate Tails
        readGenerateTails(task, toGame, breedFiles)
        // Progress ages between C1e and C2e
        readProgressAges(task, toGame, fromGame)

        // Should make all sprites the same size
        if (toGame.isC2e) {
            val sameSize = yes("${BOLD}Would you like to make all images within a breed file the same size$RESET")
            task.withSameSize(sameSize)
        }

        // Show progress
        val progress = yes("${BOLD}Show conversion progress?$RESET", true)
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
        val code = convertBreed(task)
        if (code != 0) {
            Log.e { "$WHITE_BACKGROUND$RED${BOLD}Conversion failed with exit code: ${code}$RESET" }
        }
        return code
    }
}






