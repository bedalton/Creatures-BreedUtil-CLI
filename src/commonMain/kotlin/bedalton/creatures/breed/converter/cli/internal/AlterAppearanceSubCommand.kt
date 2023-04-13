@file:Suppress("unused")

package bedalton.creatures.breed.converter.cli.internal
import bedalton.creatures.breed.converter.cli.genusArg
import bedalton.creatures.breed.converter.genome.AlterGenomeOptions
import bedalton.creatures.breed.converter.genome.alterGenome
import bedalton.creatures.breed.converter.internal.Breed
import bedalton.creatures.cli.*
import bedalton.creatures.common.util.getGenusInt
import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.Flag
import com.bedalton.cli.unescapeCLIPathAndQualify
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.vfs.*
import com.bedalton.common.util.clamp
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


    private val red by option(
        ArgType.Int,
        "red",
        description = "Red tint to apply"
    )

    private val green by option(
        ArgType.Int,
        "green",
        description = "Green tint to apply"
    )

    private val blue by option(
        ArgType.Int,
        "blue",
        description = "Blue tint to apply"
    )

    private val swap by option(
        ArgType.Int,
        "swap",
        description  = "Color swap between red and blue"
    )

    private val rotation by option(
        ArgType.Int,
        "rotation",
        description = "Color rotation or shifting of red, green and blue channels"
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
            PathUtil.getWithoutLastPathComponent(inputGenomeFile) ?: inputGenomeFile,
            outputGenomeFile?.let { PathUtil.getWithoutLastPathComponent(outputGenomeFile) ?: outputGenomeFile},
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
            red = red,
            green = green,
            blue = blue,
            swap = swap?.clamp(0, 255),
            rotation = rotation,
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

