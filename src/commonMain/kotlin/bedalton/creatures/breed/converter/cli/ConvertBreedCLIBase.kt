package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.cli.internal.SizeModArg
import com.bedalton.creatures.cli.GameArgType
import com.bedalton.creatures.sprite.util.ColorEncoding
import com.bedalton.cli.Flag
import kotlinx.cli.*

@ExperimentalCli
sealed class ConvertBreedSubcommandBase(
    name: String,
    description: String
) : Subcommand(name, description) {

    internal val fromGame by option(
        GameArgType,
        "from",
        description = "Input game for breed files"
    )

    internal val encoding: ColorEncoding? by option(
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

    abstract val outputGenus: String?

    internal open val outputBreed: String? by option(
        ArgType.String,
        "breed",
        shortName = "b",
        description = "The output breed slot for these body parts"
    )


    internal val inputGenus by option(
        ArgType.String,
        "input-genus",
        description = "The genus to filter input files by"
    )

    internal val inputBreed by option(
        ArgType.String,
        "input-breed",
        description = "The breed to filter input files by"
    )

    internal val overwriteExisting by option(
        Flag,
        "force",
        shortName = "f",
        description = "Force overwrite of existing files",
    ).default(false)


    internal val progressive by option(
        Flag,
        "progressive",
        description = "Use non-linear mapping of C1e to C2e parts to fake front facing tilt",
    ).default(false)

    internal val noAgeProgression by option(
        Flag,
        "keep-ages",
        description = "Do not shift ages to match target game",
    ).default(false)

    internal val overwriteNone by option(
        Flag,
        "skip-existing",
        shortName = "x",
        description = "Skip existing files"
    ).default(false)

    internal val noTails by option(
        ArgType.Boolean,
        "no-tail",
        description = "Do not create tail files (even if none are present)",
    ).default(false)

    internal val ignoreErrors by option(
        Flag,
        fullName = "ignore-errors",
        shortName = "e",
        description = "Ignore all compilation errors. Other errors will still cancel compile"
    ).default(false)

    internal val quiet by option(
        Flag,
        fullName = "quiet",
        shortName = "q",
        description = "Silence non-essential output"
    ).default(false)

    internal val progress by option(
        Flag,
        fullName = "progress",
        shortName = "p",
        description = "Output file conversion progress"
    ).default(false)

    internal val attDirectory by option(
        ArgType.String,
        "att-dir",
        shortName = "a",
        description = "The location of atts to convert if desired"
    )

    internal val smoothScale by option(
        Flag,
        "smooth-scale",
        description = "Smooth image while scaling"
    )


    internal val outputDirectory by option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Output folder for the converted breed files"
    )

    internal val sameSize by option(
        Flag,
        fullName = "samesize",
        shortName = "z",
        description = "Make all frames in a body part the same size"
    ).default(false)

    internal val inputGenome: String? by option(
        type = ArgType.String,
        fullName = "input-genome",
        description = "Input genome file to alter appearance genes for"
    )

    internal val outputGenome: String? by option(
        type = ArgType.String,
        fullName = "output-genome",
        description = "Altered genome output file path"
    )

    internal val noAsync by option(
        Flag,
        "no-async",
        "Process files synchronously"
    )

    internal val sameSizePadding by option(
        type = ArgType.Int,
        fullName = "same-size-padding",
        description = "The amount of padding to add around parts in a same-size image"
    )

    private val mSizeMod by option(
        type = SizeModArg,
        fullName = "scale",
        description = "The amount to scale each age group by. Format: `age=scale`\n\t-Use *=scale to set same scale for all ages"
    ).multiple()

    internal val sizeMods by lazy {
        mSizeMod
            .flatMap { it.entries }
            .associate { it.key to it.value }
    }
}
