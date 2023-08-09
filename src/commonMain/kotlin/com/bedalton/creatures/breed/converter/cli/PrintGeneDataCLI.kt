@file:Suppress("SpellCheckingInspection")

package com.bedalton.creatures.breed.converter.cli

import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.cli.Flag
import com.bedalton.cli.readInt
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.creatures.breed.converter.cli.internal.*
import com.bedalton.creatures.breed.converter.genome.GeneFilter
import com.bedalton.creatures.genetics.genome.GenomeCompiler
import com.bedalton.io.bytes.toBase64
import com.bedalton.log.Log
import com.bedalton.vfs.ERROR_CODE__FAILED_WRITE
import com.bedalton.vfs.FileSystem
import com.bedalton.vfs.ScopedFileSystem
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.coroutines.CoroutineContext

class PrintGeneDataCLI(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
) : Subcommand(name = "print-genome", "Prints gene data as json") {

    private val genome by argument(
        ArgType.String,
        description = "Genome, Export or C2 egg file to parse"
    )

    private val genomeIndex by option(
        ArgType.Int,
        "genome-index",
        description = "Index in egg or in multi-creature exports"
    ).default(0)

    private val ask by option(
        Flag,
        "guided",
        "g",
        "Enable gene types one by one with prompting"
    ).default(false)

    val default by option(
        PrintArg,
        "default",
        "x",
        "Whether to print or hide genes by default"
    ).default(true)

    private val biochemicalEmitterGenes by option(
        PrintArg,
        "emitters",
        description = "Print biochemical emitter gene information"
    )

    private val biochemicalHalfLivesGenes by option(
        PrintArg,
        "half-lives",
        description = "Print biochemical half-lives genes"
    )

    private val biochemicalInitialConcentrations by option(
        PrintArg,
        "initial-concentrations",
        description = "Print biochemical initial concentration gene information"
    )

    private val biochemicalReactions by option(
        PrintArg,
        "reactions",
        description = "Print biochemical reactions information",
    )

    private val biochemicalReceptors by option(
        PrintArg,
        "receptors",
        description = "Print biochemical reaction gene output"
    )

    private val neuroEmitterGenes by option(
        PrintArg,
        "neuro-emitters",
        description = "Print neuro emitter genes;"
    )

    private val brainOrganGenes by option(
        PrintArg,
        "bain-organs",
        description = "Print brain organ genes;"
    )

    private val brainLobeGenes by option(
        PrintArg,
        "brain-lobes",
        description = "Print brain lobe gene information;"
    )

    private val brainTractGenes by option(
        PrintArg,
        "brain-tracts",
        description = "Print brain tract gene information"
    )

    private val appearanceGenes by option(
        PrintArg,
        "appearance",
        description = "Print appearance gene information;"
    )

    private val stimulusGenes by option(
        PrintArg,
        "stimuli",
        description = "Print stimulus gene information;"
    )
    private val gaitGenes by option(
        PrintArg,
        "gaits",
        description = "Print gait gene information;"
    )
    private val genusGenes by option(
        PrintArg,
        "genus",
        description = "Print genus gene information;"
    )

    private val instinctGenes by option(
        PrintArg,
        "instincts",
        description = "Print instinct gene information;"
    )

    private val pigmentGenes by option(
        PrintArg,
        "pigments",
        description = "Print pigment gene information;"
    )

    private val pigmentBleedGenes by option(
        PrintArg,
        "pigment-bleeds",
        description = "Print pigment bleed gene information;"
    )

    private val poseGenes by option(
        PrintArg,
        "poses",
        description = "Print pose gene information;"
    )

    private val facialExpressionGenes by option(
        PrintArg,
        "facial-expressions",
        description = "Print facial expression genes;"
    )

    private val organGenes by option(
        PrintArg,
        "organs",
        description = "Print organ genes"
    )

    private val genomeVariant by option(
        ArgType.Int,
        "gene-variant",
        description = "Filter for genome gene variant 0..8, or egg genome 0..1"
    )

    private val pretty by option(
        Flag,
        "pretty",
        "p",
        "Pretty print JSON"
    ).default(false)

    val output by option(
        ArgType.String,
        "output",
        "o",
        "File to write data to"
    )

    private val outputFormat by option(
        ArgType.Choice<OutputFormat> { it.name.lowercase() },
        "output-format",
        description = "Output protobuf schema"
    ).default(OutputFormat.JSON)


    private val outputData by option(
        ArgType.Choice<OutputData> { it.commonName },
        "output-data",
        description = "Kind of data to output"
    ).default(OutputData.GENES)

    private val printColors by option(
        Flag,
        "print-colors",
        description = "Print color information"
    ).default(false)

    override fun execute() {
        jobs.add(GlobalScope.async(coroutineContext) {
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


        val fs = ScopedFileSystem(
            listOfNotNull(
                genomePath,
                PathUtil.getWithoutLastPathComponent(genomePath),
                output?.let { PathUtil.getWithoutLastPathComponent(it) },
                output
            )
        )

        if (!fs.fileExists(genomePath)) {
            exitNativeWithError(1, "Genome does not exist at $genomePath")
        }


        val genome = try {
            getGenomeFromFile(genomePath, genomeIndex)
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
        val printColors = printColors

        if (printColors && outputData != OutputData.GENES) {
            exitNativeWithError(1) { "Cannot output color data with non `--output-data \"genes\"` option" }
        }

        val colors = if (printColors) {
            getColorsForPrint(genome, genomeVariant)
        } else {
            null
        }

        val results = PrintResults(genome.version, genes, colors)

        when (outputFormat) {
            OutputFormat.JSON -> writeJson(fs, output, results)
            OutputFormat.PROTBUF -> writeProtoBuf(fs, output, results)
        }
        return 0
    }

    private suspend fun writeJson(fs: FileSystem, output: String?, result: PrintResults) {
        val json = Json {
            ignoreUnknownKeys = true
            prettyPrint = pretty
            encodeDefaults = true
        }
        val jsonString = when (outputData) {
            OutputData.GENES -> json.encodeToString(PrintResults.serializer(), result)
            OutputData.GENE_BYTE_PAIRS -> {
                val geneBytePairs = GenomeCompiler.writeGenesBytePairs(result.genes).map { GeneBytePairBase64(it) }
                json.encodeToString<List<GeneBytePairBase64>>(geneBytePairs)
            }
        }

        if (output == null) {
            Log.i { jsonString }
            return
        }
        try {
            fs.write(output, jsonString)
        } catch (e: Exception) {
            exitNativeWithError(ERROR_CODE__FAILED_WRITE, "Failed to write json data to file; ${e.formatted()}")
        }
    }

    private suspend fun writeProtoBuf(fs: FileSystem, output: String?, results: PrintResults) {
        val protobuf = ProtoBuf {
            encodeDefaults = true
        }
        val bytes = when (outputData) {
            OutputData.GENES -> protobuf.encodeToByteArray(PrintResults.serializer(), results)
            OutputData.GENE_BYTE_PAIRS -> {
                val geneBytePairs = GenomeCompiler.writeGenesBytePairs(results.genes)
                protobuf.encodeToByteArray(GeneBytePairs.serializer(), GeneBytePairs(geneBytePairs))
            }
        }
        if (output == null) {
            Log.i { bytes.toBase64() }
            return
        }
        try {
            fs.write(output, bytes)
        } catch (e: Exception) {
            exitNativeWithError(ERROR_CODE__FAILED_WRITE, "Failed to write protobuf data to file; ${e.formatted()}")
        }
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


enum class OutputFormat {
    JSON,
    PROTBUF
}

enum class OutputData(val commonName: String) {
    GENES("genes"),
    GENE_BYTE_PAIRS("gene-byte-pairs")
}