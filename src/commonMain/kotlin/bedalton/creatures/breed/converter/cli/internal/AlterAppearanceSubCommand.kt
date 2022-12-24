@file:Suppress("unused")

package bedalton.creatures.breed.converter.cli.internal

import bedalton.creatures.breed.converter.cli.genusArg
import bedalton.creatures.breed.converter.genome.AlterGenomeOptions
import bedalton.creatures.breed.converter.genome.alterGenome
import bedalton.creatures.breed.converter.internal.Breed
import bedalton.creatures.cli.*
import bedalton.creatures.common.util.FileNameUtil
import bedalton.creatures.common.util.getGenusInt
import bedalton.creatures.common.util.nullIfEmpty
import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.vfs.*
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext


@ExperimentalCli
internal class AlterAppearanceSubCommand(private val coroutineContext: CoroutineContext, private val jobs: MutableList<Deferred<Int>>) : Subcommand("alter-genome", "Alters the breed data of a genome") {

    private val inputGenomeFile by argument(
        ArgType.String,
        "input-genome",
        "The genome to alter"
    )

    private val outputGenomeFile: String? by option(
        type = ArgType.String,
        fullName = "output-genome",
        shortName = "o",
        description = "Altered genome output file path"
    )


    @Suppress("SpellCheckingInspection")
    private val defaultPartGenus by option(
        genusArg,
        "part-genus",
        description = "The default part genus: [n]orn, [g]rendel, [e]ttin, [s]hee, geat"
    )

    private val breed by option(
        ArgType.String,
        "breed",
        shortName = "b",
        description = "The default breed to use for all body parts. "
    )

    @Suppress("SpellCheckingInspection")
    private val outputGenomeGenus by option(
        genusArg,
        "genome-genus",
        description = "The genus of the creature, separate from appearance. Values: [n]orn, [g]rendel, [e]ttin, [s]hee, geat"
    )

    private val head by option(
        PartBreedArg(),
        "head",
        "Breed for head"
    )

    private val body by option(
        PartBreedArg(),
        "body",
        "Breed for body"
    )

    private val legs by option(
        PartBreedArg(),
        "legs",
        "Breed for legs"
    )


    private val arms by option(
        PartBreedArg(),
        "arms",
        "Breed for arms"
    )

    private val tail by option(
        PartBreedArg(),
        "tail",
        "Breed for tail"
    )

    private val hair by option(
        PartBreedArg(),
        "hair",
        "Breed for hair"
    )

    private val alterSleepPose: Boolean by option(
        type = Flag,
        fullName = "alter-sleep",
        shortName = "s",
        description = "Alter sleep/death pose to support C1e to C2e conversions."
    ).default(false)

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


    override fun execute() {
        val job = GlobalScope.async (coroutineContext) {
            executeSuspending()
        }
        jobs.add(job)
    }

    private suspend fun executeSuspending(): Int {
        val currentWorkingDirectory = getCurrentWorkingDirectory()
            ?: exitNativeWithError(
                ERROR_CODE__BAD_INPUT_FILE,
                "Failed to obtain current working directory"
            )
        val genus = defaultPartGenus
        val breedString = breed
        if (breedString != null && breedString.length != 1) {
            exitNativeWithError(1) { "Invalid breed value. Expected a single digit or single letter" }
        }
        if (overwriteExisting && overwriteNone) {
            exitNativeWithError(ERROR_CODE__OVERWRITE_CONFLICTS) { "Error: $ERROR_CODE__OVERWRITE_CONFLICTS: Overwrite none conflicts with force/overwrite existing" }
        }
        val inputGenomeFile = unescapeCLIPathAndQualify(inputGenomeFile, currentWorkingDirectory)
            ?: exitNativeWithError(ERROR_CODE__BAD_INPUT_FILE) {
                "Input genome must be a non-null, absolute file path"
            }
        val outputGenomeFile = outputGenomeFile?.let {unescapeCLIPathAndQualify(it, currentWorkingDirectory) }
        val roots = listOfNotNull(
            FileNameUtil.getWithoutLastPathComponent(inputGenomeFile) ?: inputGenomeFile,
            outputGenomeFile?.let { FileNameUtil.getWithoutLastPathComponent(outputGenomeFile) ?: outputGenomeFile},
            currentWorkingDirectory,
        )
        val breed = breedString?.getOrNull(0)
        val fs = roots.nullIfEmpty()?.let { ScopedFileSystem(roots) } ?: LocalFileSystem ?: UnscopedFileSystem()
        val overwriteDefault = if (overwriteExisting) {
            OverwriteDefault.ALWAYS
        } else if (overwriteNone) {
            OverwriteDefault.NEVER
        } else {
            OverwriteDefault.ASK
        }
        val opts = AlterGenomeOptions(
            fs = fs,
            inputGenomeFile = inputGenomeFile,
            outputGenomeFile = outputGenomeFile,
            outputDirectory = currentWorkingDirectory,
            alterSleepPose = alterSleepPose,
            breed = Breed(
                defaultPartGenus = genus?.let { getGenusInt(it) },
                defaultPartBreed = breed,
                outputGenomeGenus = outputGenomeGenus?.let { getGenusInt(it) },
                head = head,
                body = body,
                legs = legs,
                arms = arms,
                tail = tail,
                hair = hair,
            ),
            overwriteDefault = overwriteDefault,
            shouldWriteCallback = createOverwriteCallback(
                overwriteExisting,
                overwriteNone,
                default = true
            )

        )
        return alterGenome(opts)
    }

}

