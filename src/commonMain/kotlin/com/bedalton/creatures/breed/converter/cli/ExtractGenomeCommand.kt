package com.bedalton.creatures.breed.converter.cli

import com.bedalton.app.exitNativeWithError
import com.bedalton.app.getCurrentWorkingDirectory
import com.bedalton.common.util.PathUtil
import com.bedalton.common.util.formatted
import com.bedalton.common.util.nullIfEmpty
import com.bedalton.common.util.pathSeparator
import com.bedalton.creatures.breed.converter.cli.internal.formatted
import com.bedalton.creatures.genome.extractor.extractGenomeFromBytesAsBytesThrowing
import com.bedalton.log.LOG_DEBUG
import com.bedalton.log.Log
import com.bedalton.vfs.LocalFileSystem
import kotlinx.cli.ArgType
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class ExtractGenomeCommand(
    private val coroutineContext: CoroutineContext,
    private val jobs: MutableList<Deferred<Int>>
): Subcommand(
    "extract-genome",
    "Extracts a genome from an C2 egg or export"
) {

    private val input by argument(
        ArgType.String,
        "input-file",
        "The export or C2 egg"
    )

    private val genomeIndex by option(
        ArgType.Int,
        "genome-index",
        "i",
        "The genome index in a C2 egg file. Can be 0 or 1"
    ).default(0)

    private val output by option(
        ArgType.String,
        "output",
        "o",
        "The genome output file name"
    )

    val vfs get() = LocalFileSystem
        ?: exitNativeWithError(1, "Failed to get local file system handle")

    override fun execute() {
        jobs += GlobalScope.async(coroutineContext) {
            extract()
        }
    }

    private suspend fun extract(): Int {
        val (fileName, bytes) = getInputBytes()
        val genomeBytes = try {
            extractGenomeFromBytesAsBytesThrowing(bytes, fileName)
        } catch (e: Exception) {
            exitNativeWithError(1, e.message ?: "Failed to extract genome")
        }
        writeFile(genomeBytes)
        return 0
    }

    private suspend fun getInputBytes(): Pair<String, ByteArray> {
        val inputFilePath = getInputFilePath()
        val bytes = vfs.read(inputFilePath)
        return Pair(
            PathUtil.getLastPathComponent(inputFilePath) ?: inputFilePath,
            bytes
        )
    }


    private suspend fun writeFile(genomeBytes: ByteArray) {
        if (genomeBytes.isEmpty()) {
            exitNativeWithError(1, "Genome extraction returned empty bytes")
        }
        val outputFile = getOutputFilePath()
        val directory = PathUtil.getWithoutLastPathComponent(outputFile)
        if (directory != null && vfs.fileExists(directory)) {
            vfs.makeDirectory(directory, true)
        }
        try {
            vfs.write(outputFile, genomeBytes)
        } catch (e: Exception) {
            exitNativeWithError(1, "Failed to write bytes; " + e.formatted())
        }
    }

    private suspend fun getOutputFilePath(): String {
        var outputFile = output
            ?.trim()
            ?.nullIfEmpty()
            ?: return (getInputFilePath() + ".gen")

        if (!PathUtil.isAbsolute(outputFile)) {
            val currentWorkingDirectory = getCurrentWorkingDirectory()
                ?: exitNativeWithError(
                    1,
                    "Output file was not absolute, and current working directory could not be determined"
                )
            outputFile = PathUtil.combine(currentWorkingDirectory, outputFile)
        }

        if (vfs.isDirectory(outputFile)) {
            val fileName = PathUtil.getLastPathComponent(getInputFilePath())
                ?.nullIfEmpty()
                ?: exitNativeWithError(1, "Output file was directory and output file name could not be generated")
            outputFile += pathSeparator + fileName
        }
        return outputFile
    }


    private suspend fun getInputFilePath(): String {
        var input = input
            .trim()
            .nullIfEmpty()
            ?: exitNativeWithError(1, "Input file cannot be blank")

        if (!PathUtil.isAbsolute(input)) {
            val currentWorkingDirectory = getCurrentWorkingDirectory()
                ?: exitNativeWithError(
                    1,
                    "Input file was not absolute, and current working directory could not be determined"
                )
            input = PathUtil.combine(currentWorkingDirectory, input)
        }
        if (!vfs.fileExists(input)) {
            exitNativeWithError(1, "Input file does not exist")
        }
        return input
    }


}