package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.genome.GenomeFilter
import com.bedalton.app.exitNative
import com.bedalton.app.exitNativeWithError
import com.bedalton.cli.Flag
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.Deferred
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

class PrintGeneDataCLI(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
): Subcommand(name = "print-genome", "Prints gene data as json") {

    val genome by argument(
        ArgType.String,
        description = "Genome, Export or C2 egg file to parse"
    )

    val default by option(
        PrintArg,
        "default",
        "x",
        "Whether to print or hide genes by default; Value: [y]es/[no] or [p]rint/[h]ide; Default: YES"
    ).default(true)

    val biochemicalEmitterGenes by option(
        PrintArg,
        "emitter",
        description = "Print biochemical emitter gene information; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val biochemicalHalfLivesGenes by option(
        PrintArg,
        "half-lives",
        description = "Print biochemical half-lives genes; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val biochemicalInitialConcentrations by option(
        PrintArg,
        "initial-concentration",
        description = "Print biochemical initial concentration gene information; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val biochemicalReactions by option(
        PrintArg,
        "reaction",
        description = "Print biochemical reactions information; Value: [y]es/[no] or [p]rint/[h]ide",
    )

    val biochemicalReceptors by option(
        PrintArg,
        "receptor",
        description = "Print biochemical reaction gene output; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val neuroEmitterGenes by option(
        PrintArg,
        "neuro-emitter",
        description = "Print neuro emitter genes; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val brainOrganGenes by option(
        PrintArg,
        "bain-organ",
        description = "Print brain organ genes; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val brainLobeGenes by option(
        PrintArg,
        "brain-lobe",
        description = "Print brain lobe gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val brainTractGenes by option(
        PrintArg,
        "brain-tract",
        description = "Print brain tract gene information; [y]es/[n]o or [p]rint/[h]ide"
    )

    val appearanceGenes by option(
        PrintArg,
        "appearance",
        description = "Print appearance gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val stimulusGenes by option(
        PrintArg,
        "stimulus",
        description = "Print stimulus gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )
    val gaitGenes by option(
        PrintArg,
        "gait",
        description = "Print gait gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )
    val genusGenes by option(
        PrintArg,
        "genus",
        description = "Print genus gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val instinctGenes by option(
        PrintArg,
        "instinct",
        description = "Print instinct gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val pigmentGenes by option(
        PrintArg,
        "pigment",
        description = "Print pigment gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val pigmentBleedGenes by option(
        PrintArg,
        "pigment-bleed",
        description = "Print pigment bleed gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val poseGenes by option(
        PrintArg,
        "pose",
        description = "Print pose gene information; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val facialExpressionGenes by option(
        PrintArg,
        "facial-expressions",
        description = "Print facial expression genes; Value: [y]es/[no] or [p]rint/[s]how/[h]ide"
    )

    val organGenes by option(
        PrintArg,
        "organ",
        description = "Print organ genes; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val variant by option(
        PrintArg,
        "gene-variant",
        description = "Filter for genome gene variant 0..8, or egg genome 0..1"
    )

    override fun execute() {
        TODO("Not yet implemented")
    }

    private fun buildFilter(): GenomeFilter {
        val default = default
        return GenomeFilter(
            defaultTrue = default,
            appearanceGenes = appearanceGenes,
            biochemicalEmittersGenes = biochemicalEmitterGenes,
            biochemicalInitialConcentrationGenes = biochemicalInitialConcentrations,
            biochemicalHalfLivesGenes = biochemicalHalfLivesGenes,
            biochemicalReactionsGenes = biochemicalReactions,
            biochemicalReceptorGenes = biochemicalReceptors,
            brainLobeGenes = brainLobeGenes,
            brainTractGenes = brainTractGenes,
            brainOrganGenes = brainOrganGenes,
            facialExpressionGenes = facialExpressionGenes,
            gaitGenes = gaitGenes,
            genusGenes = genusGenes,
            instinctGenes = instinctGenes,
            neuroEmitterGenes = neuroEmitterGenes,
            organGenes = organGenes,
            pigmentGenes = pigmentGenes,
            pigmentBleedGenes = pigmentBleedGenes,
            poseGenes = poseGenes,
            stimulusGenes = stimulusGenes,
            variant = geneVariant
        )
    }

    companion object {
        private val json = Json{ ignoreUnknownKeys = true }
    }
}


private object PrintArg: ArgType<Boolean>(true) {
    override val description: kotlin.String
        get() = TODO("Not yet implemented")

    override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Boolean {
        return when (value.lowercase()) {
            "true","t", "yes",  "y", "print", "p", "show" -> true
            "no", "n", "false", "f", "hide", "h", "suppress" -> false
            else -> exitNativeWithError(1, "Failed to understand print option value for $name; Expected: [t]rue/[f]alse or [y]es/[n]o or [p]rint/[h]ide")
        }
    }

}