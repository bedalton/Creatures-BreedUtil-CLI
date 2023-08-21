@file:OptIn(DelicateCoroutinesApi::class)

package com.bedalton.creatures.breed.converter.cli

import com.bedalton.app.AppRequestTermination
import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.unescapeCLIPath
import com.bedalton.common.util.PathUtil
import com.bedalton.creatures.breed.converter.ERROR_CODE__GENUS_INVALID
import com.bedalton.creatures.breed.converter.ERROR_CODE__INVALID_GENUS_BREED_TUPLE
import com.bedalton.creatures.breed.converter.breed.ConvertBreedTask
import com.bedalton.creatures.breed.converter.breed.convertBreed
import com.bedalton.creatures.breed.converter.breed.withShouldOverwriteCallback
import com.bedalton.creatures.breed.converter.breed.withSizeMods
import com.bedalton.creatures.breed.converter.cli.internal.GenusArg
import com.bedalton.creatures.breed.converter.cli.internal.createOverwriteCallback
import com.bedalton.creatures.breed.converter.cli.internal.flatten
import com.bedalton.creatures.breed.converter.cli.internal.getBreedGenusPair
import com.bedalton.creatures.cli.GameArgType
import com.bedalton.creatures.common.structs.GameVariant
import com.bedalton.vfs.ERROR_CODE__BAD_INPUT_FILE
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.required
import kotlinx.cli.vararg
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext


@ExperimentalCli
class ConvertBreedSubcommand(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
) : ConvertBreedSubcommandBase("convert-breed", "Convert breed files between game formats") {


    private val toGame: GameVariant by argument(
        GameArgType,
        "type",
        "Target game for breed files"
    )

    override val outputGenus by option(
        GenusArg,
        "genus",
        shortName = "g",
        description = "The output genus: [n]orn, [g]rendel, [e]ttin, [s]hee, geat"
    )

    override val outputBreed by option(
        ArgType.String,
        "breed",
        shortName = "b",
        description = "The output breed slot for these body parts"
    ).required()


    internal val files: List<String> by argument(
        type = ArgType.String,
        fullName = "images",
        description = "Image files or folders"
    ).vararg()

    override fun execute() {
        val outputBreedGenusPair = try {
            getBreedGenusPair(outputBreed, "output breed")
        } catch (e: Exception) {
            exitNativeWithError(ERROR_CODE__INVALID_GENUS_BREED_TUPLE, e.message ?: "Invalid output breed value: $outputBreed")
        }

        val outputGenus = outputBreedGenusPair?.first
            ?: outputGenus
            ?: exitNativeWithError(ERROR_CODE__GENUS_INVALID, "Output genus must be defined; use --output-genus {norn|grendel|ettin|geat}")
        val outputBreed = outputBreedGenusPair?.second?.toString() ?: outputBreed


        val inputBreedGenusPair = try {
            getBreedGenusPair(inputBreed, "input breed")
        } catch (e: Exception) {
            exitNativeWithError(ERROR_CODE__INVALID_GENUS_BREED_TUPLE, e.message ?: "Invalid input breed value: $outputBreed")
        }
        val inputGenus = inputBreedGenusPair?.first ?: inputGenus
        val inputBreed = inputBreedGenusPair?.second?.toString() ?: inputBreed

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
                .withSameSizePadding(sameSizePadding)
                .withInputGenome(inputGenome?.let {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                })
                .withOutputGenome(outputGenome?.let {
                    PathUtil.ensureAbsolutePath(unescapeCLIPath(it), currentWorkingDirectory)
                })
                .withSmoothScaling(smoothScale)
                .withOutputGenomeGenus(outputGenus)
                .withShouldOverwriteCallback(
                    createOverwriteCallback(
                        overwriteExisting,
                        overwriteNone,
                        default = true
                    )
                )
                .withAsync(noAsync != true)
                // Add scaling mods, to scale parts differently
                .withSizeMods(sizeMods)
                .withHeadSizeMods(headSizeMod.flatten())
                .withBodySizeMods(bodySizeMod.flatten())
                .withLegSizeMods(legsSizeMod.flatten())
                .withArmSizeMods(armsSizeMod.flatten())
                .withTailSizeMods(tailSizeMod.flatten())

            try {
                convertBreed(task, coroutineContext)
            } catch (_: AppRequestTermination) {
                1
            }
        }
        jobs.add(job)
    }

}
