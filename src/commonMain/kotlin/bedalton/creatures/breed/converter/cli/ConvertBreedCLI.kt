@file:OptIn(DelicateCoroutinesApi::class)

package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.cli.internal.createOverwriteCallback
import bedalton.creatures.breed.converter.bodyparts.*
import bedalton.creatures.breed.converter.breed.ConvertBreedTask
import bedalton.creatures.breed.converter.breed.convertBreed
import bedalton.creatures.breed.converter.breed.withShouldOverwriteCallback
import bedalton.creatures.cli.*
import bedalton.creatures.common.structs.*
import bedalton.creatures.common.util.*
import bedalton.creatures.sprite.util.*
import com.bedalton.app.AppRequestTermination
import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.vfs.*
import kotlinx.cli.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal val genusArg = ArgType.Choice(
    listOf("n", "norn", "g", "grendel", "e", "ettin", "s", "shee", "geat"),
    {
        it
    }
)

@ExperimentalCli
class ConvertBreedSubcommand(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
) : Subcommand("convert-breed", "Convert breed files between game formats") {
    private val toGame by argument(
        GameArgType,
        "type",
        "Target game for breed files"
    )

    private val fromGame by option(
        GameArgType,
        "from",
        description = "Input game for breed files"
    )

    private val encoding: ColorEncoding? by option(
        type = ArgType.Choice(
            listOf(ColorEncoding.X_555, ColorEncoding.X_565),
            { value ->
                ColorEncoding.fromString(value)
            }, { encoding ->
                encoding.simpleName
            }
        ),
        fullName = "encoding",
        shortName = "c",
        description = "Sprite color encoding",
    )

    @Suppress("SpellCheckingInspection")
    private val outputGenus by option(
        genusArg,
        "genus",
        shortName = "g",
        description = "The output genus: [n]orn, [g]rendel, [e]ttin, [s]hee, geat"
    ).required()

    private val outputBreed by option(
        ArgType.String,
        "breed",
        shortName = "b",
        description = "The output breed slot for these body parts"
    ).required()


    private val inputGenus by option(
        ArgType.String,
        "input-genus",
        description = "The genus to filter input files by"
    )

    private val inputBreed by option(
        ArgType.String,
        "input-breed",
        description = "The breed to filter input files by"
    )

    private val overwriteExisting by option(
        Flag,
        "force",
        shortName = "f",
        description = "Force overwrite of existing files",
    ).default(false)


    private val progressive by option(
        Flag,
        "progressive",
        description = "Use non-linear mapping of C1e to C2e parts to fake front facing tilt",
    ).default(false)

    private val noAgeProgression by option(
        Flag,
        "keep-ages",
        description = "Do not shift ages to match target game",
    ).default(false)

    private val overwriteNone by option(
        Flag,
        "skip-existing",
        shortName = "x",
        description = "Skip existing files"
    ).default(false)

    private val noTails by option(
        ArgType.Boolean,
        "no-tail",
        description = "Do not create tail files (even if none are present)",
    ).default(false)

    private val ignoreErrors by option(
        Flag,
        fullName = "ignore-errors",
        shortName = "e",
        description = "Ignore all compilation errors. Other errors will still cancel compile"
    ).default(false)

    private val quiet by option(
        Flag,
        fullName = "quiet",
        shortName = "q",
        description = "Silence non-essential output"
    ).default(false)

    private val progress by option(
        Flag,
        fullName = "progress",
        shortName = "p",
        description = "Output file conversion progress"
    ).default(false)

    private val attDirectory by option(
        ArgType.String,
        "att-dir",
        shortName = "a",
        description = "The location of atts to convert if desired"
    )


    private val outputDirectory by option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Output folder for the converted breed files"
    )

    private val sameSize by option(
        Flag,
        fullName = "samesize",
        shortName = "z",
        description = "Make all frames in a body part the same size"
    ).default(false)

    private val inputGenome: String? by option(
        type = ArgType.String,
        fullName = "input-genome",
        description = "Input genome file to alter appearance genes for"
    )

    private val outputGenome: String? by option(
        type = ArgType.String,
        fullName = "output-genome",
        description = "Altered genome output file path"
    )

    private val outputGenomeGenus: String? by option(
        genusArg,
        "output-genome-genus",
        description = "The actual genus for creatures hatched from this genome (unrelated to appearance)"
    )

    private val noAsync by option(
        Flag,
        "no-async",
        "Process files synchronously"
    )

    private val files: List<String> by argument(
        type = ArgType.String,
        fullName = "images",
        description = "Image files or folders"
    ).vararg()


    override fun execute() {
        val job = GlobalScope.async(coroutineContext) {
            val currentWorkingDirectory = getCurrentWorkingDirectory()
                ?: exitNativeWithError(
                    ERROR_CODE__BAD_INPUT_FILE,
                    "Failed to obtain current working directory"
                )
            val task = ConvertBreedTask(
                toGame.code,
                files.mapNotNull {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                }.toTypedArray()
            )
                .withFromGameVariantString(fromGame?.code)
                .withEncoding(encoding?.simpleName)
                .withOutputBreedGenus(outputGenus)
                .withOutputBreed(outputBreed)
                .withInputBreedGenus(inputGenus)
                .withInputBreed(inputBreed)
                .withProgress(progress)
                .withProgressive(progressive)
                .withAttDirectory(attDirectory?.let {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                })
                .withIgnoreErrors(ignoreErrors)
                .withNoAgeProgression(noAgeProgression)
                .withQuiet(quiet)
                .withOutputDirectory(outputDirectory?.let {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                } ?: currentWorkingDirectory)
                .withOverwriteExisting(overwriteExisting)
                .withOverwriteNone(overwriteNone)
                .withNoTails(noTails)
                .withSameSize(sameSize)
                .withInputGenome(inputGenome?.let {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                })
                .withOutputGenome(outputGenome?.let {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                })
                .withOutputGenomeGenus(outputGenomeGenus)
                .withShouldOverwriteCallback(
                    createOverwriteCallback(
                        overwriteExisting,
                        overwriteNone,
                        default = true
                    )
                )
                .withAsync(noAsync != true)

            try {
                convertBreed(task)
            } catch (_: AppRequestTermination) {
                1
            }
        }
        jobs.add(job)
    }

}
