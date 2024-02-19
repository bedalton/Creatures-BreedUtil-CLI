@file:Suppress("unused")

package com.bedalton.creatures.breed.converter.cli.internal

import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.Flag
import com.bedalton.cli.unescapeCLIPathAndQualify
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.clamp
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.creatures.breed.converter.breed.Breed
import com.bedalton.creatures.breed.converter.genome.AlterGenomeOptions
import com.bedalton.creatures.breed.converter.genome.alterGenome
import com.bedalton.creatures.cli.PartBreedArg
import com.bedalton.creatures.common.util.getGenusInt
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

    private val outputGenomeFile: String? by argument(
        type = ArgType.String,
        fullName = "output-genome",
        description = "Altered genome output file path"
    )

    private val breed by option(
        PartBreedArg(),
        "breed",
        shortName = "b",
        description = "The default breed to use for all body parts. "
    )

    private val outputGenomeGenus by option(
        GenusArg,
        "genome-genus",
        description = "The genus of the creature, separate from appearance. Values: [n]orn, [g]rendel, [e]ttin, [s]hee, geat"
    )

    private val head by option(
        PartBreedArg(),
        "head",
        description = "Breed for head"
    )

    private val body by option(
        PartBreedArg(),
        "body",
        description = "Breed for body"
    )

    private val legs by option(
        PartBreedArg(),
        "legs",
        description = "Breed for legs"
    )

    private val arms by option(
        PartBreedArg(),
        "arms",
        description = "Breed for arms"
    )

    private val tail by option(
        PartBreedArg(),
        "tail",
        description = "Breed for tail"
    )

    private val hair by option(
        PartBreedArg(),
        "hair",
        description = "Breed for hair"
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

    private val jitterPigment by option(
        Flag,
        "jitter-pigment",
        description = "Cause children's tints to be unpredictable"
    ).default(false)

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

    private val jitterBleed by option(
        Flag,
        "jitter-bleed",
        description = "Cause offspring's bleeds to be unpredictable"
    ).default(false)

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
        if (overwriteExisting && overwriteNone) {
            exitNativeWithError(ERROR_CODE__OVERWRITE_CONFLICTS) { "Error: $ERROR_CODE__OVERWRITE_CONFLICTS: Overwrite none conflicts with force/overwrite existing" }
        }
        val inputGenomeFile = unescapeCLIPathAndQualify(inputGenomeFile, currentWorkingDirectory)
            ?: exitNativeWithError(ERROR_CODE__BAD_INPUT_FILE) {
                "Input genome must be a non-null, absolute file path"
            }
        val outputGenomeFile = outputGenomeFile?.let { unescapeCLIPathAndQualify(it, currentWorkingDirectory) }
        val roots = listOfNotNull(
            PathUtil.getWithoutLastPathComponent(inputGenomeFile) ?: inputGenomeFile,
            outputGenomeFile?.let { PathUtil.getWithoutLastPathComponent(outputGenomeFile) ?: outputGenomeFile},
            currentWorkingDirectory,
        )
        val breed = breed
        val fs = roots.nullIfEmpty()?.let { UnscopedFileSystem() } ?: LocalFileSystem ?: UnscopedFileSystem()
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
                defaultPartGenus = breed?.first,
                defaultPartBreed = breed?.second,
                outputGenomeGenus = outputGenomeGenus?.let { getGenusInt(it) },
                head = head,
                body = body,
                legs = legs,
                arms = arms,
                tail = tail,
                hair = hair,
            ),
            red = red,
            jitterRed = jitterPigment,
            green = green,
            jitterGreen = jitterPigment,
            blue = blue,
            jitterBlue = jitterPigment,
            swap = swap?.clamp(0, 255),
            jitterSwap = jitterBleed,
            rotation = rotation,
            jitterRotation = jitterBleed,
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