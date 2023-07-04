package bedalton.creatures.breed.converter.cli

import bedalton.creatures.breed.converter.cli.internal.formatted
import bedalton.creatures.breed.converter.cli.internal.yesNullable
import bedalton.creatures.breed.converter.genome.GeneFilter
import bedalton.creatures.genetics.gene.Gene
import bedalton.creatures.genetics.parser.GenomeParser
import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.Flag
import com.bedalton.cli.readInt
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.log.Log
import com.bedalton.vfs.ScopedFileSystem
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.coroutines.CoroutineContext

class PrintGeneDataCLI(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
) : Subcommand(name = "print-genome", "Prints gene data as json") {

    val genome by argument(
        ArgType.String,
        description = "Genome, Export or C2 egg file to parse"
    )

    val ask by option(
        Flag,
        "guided",
        "g",
        "Enable gene types one by one with prompting"
    ).default(false)

    val default by option(
        PrintArg,
        "default",
        "x",
        "Whether to print or hide genes by default; Value: [y]es/[n]o or [p]rint/[h]ide; Default: YES"
    ).default(true)

    val biochemicalEmitterGenes by option(
        PrintArg,
        "emitters",
        description = "Print biochemical emitter gene information; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val biochemicalHalfLivesGenes by option(
        PrintArg,
        "half-lives",
        description = "Print biochemical half-lives genes; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val biochemicalInitialConcentrations by option(
        PrintArg,
        "initial-concentrations",
        description = "Print biochemical initial concentration gene information; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val biochemicalReactions by option(
        PrintArg,
        "reactions",
        description = "Print biochemical reactions information; Value: [y]es/[no] or [p]rint/[h]ide",
    )

    val biochemicalReceptors by option(
        PrintArg,
        "receptors",
        description = "Print biochemical reaction gene output; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val neuroEmitterGenes by option(
        PrintArg,
        "neuro-emitters",
        description = "Print neuro emitter genes;"
    )

    val brainOrganGenes by option(
        PrintArg,
        "bain-organs",
        description = "Print brain organ genes;"
    )

    val brainLobeGenes by option(
        PrintArg,
        "brain-lobes",
        description = "Print brain lobe gene information;"
    )

    val brainTractGenes by option(
        PrintArg,
        "brain-tracts",
        description = "Print brain tract gene information; [y]es/[n]o or [p]rint/[h]ide"
    )

    val appearanceGenes by option(
        PrintArg,
        "appearance",
        description = "Print appearance gene information;"
    )

    val stimulusGenes by option(
        PrintArg,
        "stimuli",
        description = "Print stimulus gene information;"
    )
    val gaitGenes by option(
        PrintArg,
        "gaits",
        description = "Print gait gene information;"
    )
    val genusGenes by option(
        PrintArg,
        "genus",
        description = "Print genus gene information;"
    )

    val instinctGenes by option(
        PrintArg,
        "instincts",
        description = "Print instinct gene information;"
    )

    val pigmentGenes by option(
        PrintArg,
        "pigments",
        description = "Print pigment gene information;"
    )

    val pigmentBleedGenes by option(
        PrintArg,
        "pigment-bleeds",
        description = "Print pigment bleed gene information;"
    )

    val poseGenes by option(
        PrintArg,
        "poses",
        description = "Print pose gene information;"
    )

    val facialExpressionGenes by option(
        PrintArg,
        "facial-expressions",
        description = "Print facial expression genes;"
    )

    val organGenes by option(
        PrintArg,
        "organs",
        description = "Print organ genes; Value: [y]es/[no] or [p]rint/[h]ide"
    )

    val genomeVariant by option(
        ArgType.Int,
        "gene-variant",
        description = "Filter for genome gene variant 0..8, or egg genome 0..1"
    )

    val pretty by option(
        Flag,
        "pretty",
        "p",
        "Pretty print JSON"
    ).default(false)

    val output by option(
        ArgType.String,
        "output",
        "o",
        "File to write JSON data to"
    )

    override fun execute() {
        jobs.add(GlobalScope.async {
            run()
        })
    }


    private suspend fun run(): Int {
        var genomePath = genome.nullIfEmpty()
            ?: exitNativeWithError(1, "Cannot parse genome without genome argument")

        if (!PathUtil.isAbsolute(genomePath)) {
            val cwd = getCurrentWorkingDirectory()
                ?: exitNativeWithError(1, "Cannot parse genome without absolute path")
            genomePath = PathUtil.combine(cwd, genomePath)
        }

        var output = output
        if (output != null && !PathUtil.isAbsolute(output)) {
            val cwd = getCurrentWorkingDirectory()
                ?: exitNativeWithError(1, "Cannot parse genome without absolute path")
            output = PathUtil.combine(cwd, output)
        }

        val fs = ScopedFileSystem(listOfNotNull(
            PathUtil.getWithoutLastPathComponent(genomePath),
            genomePath,
            output?.let { PathUtil.getWithoutLastPathComponent(it) }
        ))
        if (!fs.fileExists(genomePath)) {
            exitNativeWithError(1, "Genome does not exist at ${genomePath}")
        }

        val genomeBytes = try {
            fs.read(genomePath)
        } catch (e: Exception) {
            exitNativeWithError(1) {
                "Failed to read genome at $genomePath;\n${e.formatted()}"
            }
        }

        val genome = try {
            GenomeParser.parseGenome(genomeBytes, true)
        } catch (e: Exception) {
            exitNativeWithError(1) {
                val genomeType = when (PathUtil.getExtension(genomePath)?.lowercase()) {
                    "exp", "creature" -> "export"
                    "egg" -> "C2 egg"
                    else -> "genome"
                }
                "Failed to parse $genomeType;\n${e.formatted()}"
            }
        }

        val filter = buildFilter()
        val genes = filter.filterGenes(genome)

        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = pretty
        }
        val jsonString = json.encodeToString<List<Gene>>(genes)
        if (output != null) {
            fs.write(output, jsonString)
        } else {
            Log.i {
                jsonString
            }
        }
        return 0
    }

    private suspend fun buildFilter(): GeneFilter {
        val default = default
        val ask = ask

        return GeneFilter(
            defaultTrue = default,
            appearanceGenes = appearanceGenes.orPrompt("Appearance Genes"),
            biochemicalEmittersGenes = biochemicalEmitterGenes.orPrompt("Biochemical Emitter Genes"),
            biochemicalInitialConcentrationGenes = biochemicalInitialConcentrations.orPrompt("Initial Concentrations"),
            biochemicalHalfLivesGenes = biochemicalHalfLivesGenes.orPrompt("Chemical Half-Lives"),
            biochemicalReactionsGenes = biochemicalReactions.orPrompt("Chemical Reaction Genes"),
            biochemicalReceptorGenes = biochemicalReceptors.orPrompt("Chemical Receptor Genes"),
            brainLobeGenes = brainLobeGenes.orPrompt("Brain Lobes"),
            brainTractGenes = brainTractGenes.orPrompt("Brain Tract Genes"),
            brainOrganGenes = brainOrganGenes.orPrompt("Brain Organ Genes"),
            facialExpressionGenes = facialExpressionGenes.orPrompt("Facial Expression Genes"),
            gaitGenes = gaitGenes.orPrompt("Gait Genes"),
            genusGenes = genusGenes.orPrompt("Genus Genes"),
            instinctGenes = instinctGenes.orPrompt("Instinct Genes"),
            neuroEmitterGenes = neuroEmitterGenes.orPrompt("Neuro-Emitter Genes"),
            organGenes = organGenes.orPrompt("Organ Genes"),
            pigmentGenes = pigmentGenes.orPrompt("Pigment Genes"),
            pigmentBleedGenes = pigmentBleedGenes.orPrompt("Pigment Bleed Genes"),
            poseGenes = poseGenes.orPrompt("Pose Genes"),
            stimulusGenes = stimulusGenes.orPrompt("Stimulus Genes"),
            genomeVariant = genomeVariant?.also {
                if (it !in 0..8) {
                    exitNativeWithError(1, "Invalid genome variant $it, expected [0-8]")
                }
            } ?: if (ask) {
                readInt("Gene Variant if C2e", true, null, 0, 8)
            } else {
                null
            }
        )
    }


    private suspend fun Boolean?.orPrompt(name: String): Boolean? {
        if (this != null || !ask) {
            return this
        }
        return yesNullable("Show $name", default, false)
    }
}

private object PrintArg : ArgType<Boolean>(true) {
    override val description: kotlin.String
        get() = "Value: [y]es/[no] or [p]rint/[s]how/[h]ide"

    override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Boolean {
        return when (value.lowercase()) {
            "true", "t", "yes", "y", "print", "p", "show" -> true
            "no", "n", "false", "f", "hide", "h", "suppress" -> false
            else -> exitNativeWithError(
                1,
                "Failed to understand print option value for $name; Expected: [t]rue/[f]alse or [y]es/[n]o or [p]rint/[h]ide"
            )
        }
    }

}